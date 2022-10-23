package io.treeverse.clients

import org.apache.hadoop.conf.Configuration

import scala.util.control.Breaks._
import scala.math._
import org.apache.hadoop.tools.{DistCp, DistCpConstants}
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import java.net.URI

class GCBackupAndRestore {}

object GCBackupAndRestore {
  val S3AccessKeyName = "fs.s3a.access.key"
  val S3SecretKeyName = "fs.s3a.secret.key"
  val AzureStorageAccountKeyName = "fs.azure.account.key"
  val DistCpMaxNumListStatusThreads =
    40 // max by distCp, https://docs.cloudera.com/HDPDocuments/HDP3/HDP-3.0.1/bk_cloud-data-access/content/distcp-perf-file-listing.html

  lazy val spark = SparkSession.builder().appName("GCBackupAndRestore").getOrCreate()

  import spark.implicits._

  def constructAbsoluteObjectPaths(
      objectsRelativePathsDF: DataFrame,
      srcNamespace: String,
      storageType: String
  ): Dataset[String] = {
    var storageNSForFS = ApiClient
      .translateURI(URI.create(srcNamespace), storageType)
      .normalize()
      .toString
    if (!storageNSForFS.endsWith("/")) {
      storageNSForFS += "/"
    }

    objectsRelativePathsDF
      .select("address")
      .as[String]
      .flatMap(x => StorageUtils.concatKeysToStorageNamespace(Seq(x), storageNSForFS, storageType))
  }

  def validateAndParseHadoopConfig(
      hc: Configuration,
      storageType: String
  ): Array[(String, String)] = {
    storageType match {
      case StorageUtils.StorageTypeS3 =>
        val hadoopProps =
          HadoopUtils.getHadoopConfigurationValues(hc, S3AccessKeyName, S3SecretKeyName)
        if (
          hadoopProps.iterator
            .filter(x => S3AccessKeyName.equals(x._1))
            .length != 1
        ) {
          Console.err.println(
            "missing required hadoop property. " + S3AccessKeyName
          )
          System.exit(1)
        }
        if (
          hadoopProps.iterator
            .filter(x => S3SecretKeyName.equals(x._1))
            .length != 1
        ) {
          Console.err.println(
            "missing required hadoop property. " + S3SecretKeyName
          )
          System.exit(1)
        }
        hadoopProps
      case StorageUtils.StorageTypeAzure =>
        val hadoopProps = HadoopUtils.getHadoopConfigurationValues(hc, AzureStorageAccountKeyName)
        if (hadoopProps == null || hadoopProps.length != 1) {
          Console.err.println(
            "missing required hadoop property. " + AzureStorageAccountKeyName
          )
          System.exit(1)
        }
        hadoopProps
    }
  }

  // Construct a DistCp command of the form:
  // `hadoop distcp -D<per storage credentials> -f absoluteAddressesTextFilePath dstNamespaceForHadoopFs`
  // example for command format https://docs.lakefs.io/integrations/distcp.html
  // with distCp options from https://hadoop.apache.org/docs/r3.2.1/hadoop-distcp/DistCp.html#Command_Line_Options
  def constructDistCpCommand(
      hadoopProps: Array[(String, String)],
      absoluteAddressesTextFilePath: String,
      dstNamespaceForHadoopFs: String,
      hc: Configuration,
      numObjectsToCopy: Long
  ): Array[String] = {

    val distCpLogsPath =
      hc.get(DistCpConstants.CONF_LABEL_LOG_PATH, s"${dstNamespaceForHadoopFs.stripSuffix("/")}/_distCp/logs/")
    // Tune distCp options that control the speed of the file-to-copy list building stage
    val numListstatusThreads =
      hc.get(DistCpConstants.CONF_LABEL_LISTSTATUS_THREADS, DistCpMaxNumListStatusThreads.toString)
    // Tune distCp options that control the speed of the copy stage
    // https://docs.cloudera.com/HDPDocuments/HDP3/HDP-3.1.4/bk_cloud-data-access/content/distcp-perf-mappers.html
    val maxMaps = hc.get(DistCpConstants.CONF_LABEL_MAX_MAPS,
                         max(DistCpConstants.DEFAULT_MAPS, numObjectsToCopy / 1000).toString
                        ) //TODO: what is a reasonable number of maps
    val mapsBandwidth =
      hc.get(DistCpConstants.CONF_LABEL_BANDWIDTH_MB, DistCpConstants.DEFAULT_BANDWIDTH_MB.toString)
    println(
      s"distCpLogsPath: ${distCpLogsPath}, numListstatusThreads: ${numListstatusThreads}, maxMaps: ${maxMaps}, mapsBandwidth: ${mapsBandwidth}"
    )

    hadoopProps.map((prop) => s"-D${prop._1}=${prop._2}") ++
      Seq(
        // enable verbose logging, that log additional info (path, size) in the SKIP/COPY log
        "-v",
        "-log",
        distCpLogsPath,
        "-numListstatusThreads",
        numListstatusThreads,
        "-m",
        maxMaps,
        "-bandwidth",
        mapsBandwidth,
        "-direct", // force using the direct writing option, which is recommended while using distCp with objects storages, and is supported from hadoop 3.1.3
        "-strategy",
        "dynamic", // force using the dynamic strategy, which is always recommended for improved distCp performance
        // -f option copies the files listed in the file after the -f option
        "-f",
        absoluteAddressesTextFilePath,
        dstNamespaceForHadoopFs
      )
  }

  // Find the path of the first txt file under a prefix.
  def getTextFileLocation(prefix: String, hc: Configuration): String = {
    val fs = FileSystem.get(URI.create(prefix), hc)
    val dirIterator = fs.listFiles(new Path(prefix), false)
    var textFilePath = ""
    breakable(
      while (dirIterator.hasNext) {
        val curFile = dirIterator.next()
        val curPath = curFile.getPath.toString
        if (curPath.endsWith("txt")) {
          textFilePath = curPath
          break
        }
      }
    )
    textFilePath
  }

  /** Eliminate objects that don't exist on the underlying object store from the path list.
   *
   *  @param absolutePathsDF a data frame containing object absolute paths
   *  @param hc              hadoop configurations
   *  @return a dataset only including absolute paths of objects that exist on the underlying object store
   */
  def eliminatePathsOfNonExistingObjects(
      absolutePathsDF: Dataset[String],
      hc: Configuration
  ): Dataset[String] = {
    // Spark operators will need to generate configured FileSystems to check if objects exist.
    // They will not have a JobContext to let them do that. Transmit (all) Hadoop filesystem configuration values to
    // let them generate a (close-enough) Hadoop configuration to build the
    // needed FileSystems.
    val hcValues =
      spark.sparkContext.broadcast(HadoopUtils.getHadoopConfigurationValues(hc, "fs."))
    val configMapper = new ConfigMapper(hcValues)
    absolutePathsDF
      .filter(x => {
        val path = new Path(x)
        path.getFileSystem(configMapper.configuration).exists(path)
      })
  }

  /** Required arguments are the following:
   *  1. address of parquet that includes the relative paths of files to backup or restore, created by a gc run
   *  2. src: the namespace to backup or restore objects from/to (i.e. repo storage namespace, or an external location compatibly)
   *  3. backup/restore destination: the namespace to backup or restore objects from/to (i.e. an external location or repo storage namespace compatibly)
   *  4. Object storage type: "s3" or "azure"
   */
  def main(args: Array[String]): Unit = {
    if (args.length != 4) {
      Console.err.println(
        "Usage: ... <objects list location> <src namespace> <dst namespace> <storage type>"
      )
      System.exit(1)
    }
    val relativeAddressesLocation = args(0)
    val srcNamespace = args(1)
    val dstNamespace = args(2)
    val storageType = args(3)

    val hc = spark.sparkContext.hadoopConfiguration
    val hadoopProps = validateAndParseHadoopConfig(hc, storageType)

    val relativeAddressesLocationForHadoopFs =
      ApiClient.translateURI(URI.create(relativeAddressesLocation), storageType).toString
    val dstNamespaceForHadoopFs =
      ApiClient.translateURI(URI.create(dstNamespace), storageType).toString
    println("translated dstNamespace: " + dstNamespaceForHadoopFs)

    val objectsRelativePathsDF = spark.read.parquet(relativeAddressesLocationForHadoopFs)
    val objectsAbsolutePathsDF =
      constructAbsoluteObjectPaths(objectsRelativePathsDF, srcNamespace, storageType)
    // Keep only paths to existing objects, otherwise, distCp will fail copying.
    val existingAbsolutePaths = eliminatePathsOfNonExistingObjects(objectsAbsolutePathsDF, hc)
    val numExistingObjects = existingAbsolutePaths.count()
    println("count: " + numExistingObjects)
    if (numExistingObjects == 0) {
      println("There are no objects to copy. process will finish without copying objects")
      System.exit(0)
    }

    // We assume that there are write permissions to the dst namespace and therefore creating intermediate output there.
    val absoluteAddressesLocation = s"${dstNamespaceForHadoopFs.stripSuffix("/")}/_gc-backup-restore/absolute_addresses/"
    println("absoluteAddressesLocation: " + absoluteAddressesLocation)
    // This application uses distCp to copy files. distCp can copy a list of files in a given input text file. therefore,
    // we write the absolute file paths into a text file rather than a parquet.
    existingAbsolutePaths
      .repartition(1)
      .write
      .text(absoluteAddressesLocation)

    // Spark writes two files under absoluteAddressesLocation, a _SUCCESS file and the actual txt file that has dynamic name.
    val absoluteAddressesTextFilePath = getTextFileLocation(absoluteAddressesLocation, hc)
    println("txtFilePath: " + absoluteAddressesTextFilePaths)

    val distCpCommand =
      constructDistCpCommand(hadoopProps,
                             absoluteAddressesTextFilePath,
                             dstNamespaceForHadoopFs,
                             hc,
                             numExistingObjects
                            )
    DistCp.main(distCpCommand)
  }
}
