package io.treeverse.clients

import com.amazonaws.services.s3.AmazonS3
import com.google.protobuf.timestamp.Timestamp
import io.lakefs.clients.api.model.GarbageCollectionPrepareResponse
import io.treeverse.clients.LakeFSContext._
import io.treeverse.parquet.RepositoryConverter
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StructType
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.native.JsonMethods._

import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

import collection.JavaConverters._

/** Interface to build an S3 client.  The object
 *  io.treeverse.clients.conditional.S3ClientBuilder -- conditionally
 *  defined in a separate file according to the supported Hadoop version --
 *  implements this trait.  (Scala requires companion objects to be defined
 *  in the same file, so it cannot be a companion.)
 */
trait S3ClientBuilder extends Serializable {

  /** Return a configured Amazon S3 client similar to the one S3A would use.
   *  On Hadoop versions >=3, S3A can assume a role, and the returned S3
   *  client will similarly assume that role.
   *
   *  @param hc         (partial) Hadoop configuration of fs.s3a.
   *  @param bucket     that this client will access.
   *  @param region     to find this bucket.
   *  @param numRetries number of times to retry on AWS.
   */
  def build(hc: Configuration, bucket: String, region: String, numRetries: Int): AmazonS3
}

object GarbageCollector {

  type ConfMap = List[(String, String)]

  /** @return a serializable summary of values in hc starting with prefix.
   */
  def getHadoopConfigurationValues(hc: Configuration, prefixes: String*): ConfMap =
    hc.iterator.asScala
      .filter(c => prefixes.exists(c.getKey.startsWith))
      .map(entry => (entry.getKey, entry.getValue))
      .toList
      .asInstanceOf[ConfMap]

  def configurationFromValues(v: Broadcast[ConfMap]) = {
    val hc = new Configuration()
    v.value.foreach({ case (k, v) => hc.set(k, v) })
    hc
  }

  def getCommitsDF(runID: String, commitDFLocation: String, spark: SparkSession): Dataset[Row] = {
    spark.read
      .option("header", value = true)
      .option("inferSchema", value = true)
      .csv(commitDFLocation)
  }

  private def getRangeTuples(
      commitID: String,
      repo: String,
      apiConf: APIConfigurations,
      hcValues: Broadcast[ConfMap]
  ): Set[(String, Array[Byte], Array[Byte])] = {
    val conf = configurationFromValues(hcValues)
    val apiClient = ApiClient.get(apiConf)
    val commit = apiClient.getCommit(repo, commitID)
    val maxCommitEpochSeconds = conf.getLong(LAKEFS_CONF_DEBUG_GC_MAX_COMMIT_EPOCH_SECONDS_KEY, -1)
    if (maxCommitEpochSeconds > 0 && commit.getCreationDate > maxCommitEpochSeconds) {
      return Set()
    }
    val location = apiClient.getMetaRangeURL(repo, commit)
    // continue on empty location, empty location is a result of a commit with no metaRangeID (e.g 'Repository created' commit)
    if (location == "") Set()
    else
      SSTableReader
        .forMetaRange(conf, location)
        .newIterator()
        .map(range =>
          (new String(range.id), range.message.minKey.toByteArray, range.message.maxKey.toByteArray)
        )
        .toSet
  }

  def getRangesDFFromCommits(
      commits: Dataset[Row],
      repo: String,
      apiConf: APIConfigurations,
      hcValues: Broadcast[ConfMap]
  ): Dataset[Row] = {
    val get_range_tuples = udf((commitID: String) => {
      getRangeTuples(commitID, repo, apiConf, hcValues).toSeq
    })

    commits.distinct
      .select(col("expired"), explode(get_range_tuples(col("commit_id"))).as("range_data"))
      .select(
        col("expired"),
        col("range_data._1").as("range_id"),
        col("range_data._2").as("min_key"),
        col("range_data._3").as("max_key")
      )
      .distinct
  }

  def getRangeAddresses(
      rangeID: String,
      apiConf: APIConfigurations,
      repo: String,
      hcValues: Broadcast[ConfMap]
  ): Iterator[String] = {
    val location = ApiClient
      .get(apiConf)
      .getRangeURL(repo, rangeID)
    SSTableReader
      .forRange(configurationFromValues(hcValues), location)
      .newIterator()
      .map(a => a.message.address)
  }

  def getEntryTuples(
      rangeID: String,
      apiConf: APIConfigurations,
      repo: String,
      hcValues: Broadcast[ConfMap]
  ): Iterator[(String, String, Boolean, Long)] = {
    def getSeconds(ts: Option[Timestamp]): Long = {
      ts.getOrElse(0).asInstanceOf[Timestamp].seconds
    }

    val location = ApiClient
      .get(apiConf)
      .getRangeURL(repo, rangeID)
    SSTableReader
      .forRange(configurationFromValues(hcValues), location)
      .newIterator()
      .map(a =>
        (
          new String(a.key),
          new String(a.message.address),
          a.message.addressType.isRelative,
          getSeconds(a.message.lastModified)
        )
      )
  }

  /** @param leftRangeIDs
   *  @param rightRangeIDs
   *  @param apiConf
   *  @return tuples of type (key, address, isRelative, lastModified) for every address existing in leftRanges and not in rightRanges
   */
  def leftAntiJoinAddresses(
      leftRangeIDs: Set[String],
      rightRangeIDs: Set[String],
      apiConf: APIConfigurations,
      repo: String,
      hcValues: Broadcast[ConfMap]
  ): Set[(String, String, Boolean, Long)] = {
    val leftTuples = distinctEntryTuples(leftRangeIDs, apiConf, repo, hcValues)
    val rightTuples = distinctEntryTuples(rightRangeIDs, apiConf, repo, hcValues)
    leftTuples -- rightTuples
  }

  private def distinctEntryTuples(
      rangeIDs: Set[String],
      apiConf: APIConfigurations,
      repo: String,
      hcValues: Broadcast[ConfMap]
  ) = {
    // Process rangeIDs using mutation to ensure complete control over when
    // each range is read.
    var tuples = collection.mutable.Set[(String, String, Boolean, Long)]()
    rangeIDs.foreach((rangeID: String) =>
      tuples ++= getEntryTuples(rangeID, apiConf, repo, hcValues)
    )
    tuples.toSet
  }

  /** receives a dataframe containing active and expired ranges and returns entries contained only in expired ranges
   *
   *  @param ranges dataframe of type   rangeID:String | expired: Boolean
   *  @return dataframe of type  key:String | address:String | relative:Boolean | last_modified:Long
   */
  def getExpiredEntriesFromRanges(
      ranges: Dataset[Row],
      allEntries: Dataset[Row],
      apiConf: APIConfigurations,
      repo: String,
      hcValues: Broadcast[ConfMap]
  ): Dataset[Row] = {
    val expiredRangesDF = ranges.where("expired")
    val activeRangesDF = ranges.where("!expired")

    // ranges existing in expired and not in active
    val uniqueExpiredRangesDF = expiredRangesDF.join(
      activeRangesDF,
      expiredRangesDF("range_id") === activeRangesDF("range_id"),
      "leftanti"
    )
    allEntries
      .as("all_entries")
      .join(uniqueExpiredRangesDF.as("expired"), "range_id")
      .select("all_entries.*")
      .join(activeRangesDF.as("active"),
            allEntries("address") === activeRangesDF("address"),
            "leftanti"
           )
      .select("all_entries.*")
  }

  def getExpiredAddresses(
      repo: String,
      runID: String,
      commitDFLocation: String,
      allEntries: Dataset[Row],
      spark: SparkSession,
      apiConf: APIConfigurations,
      hcValues: Broadcast[ConfMap]
  ): Dataset[Row] = {
    val commitsDF = getCommitsDF(runID, commitDFLocation, spark)
    val rangesDF = getRangesDFFromCommits(commitsDF, repo, apiConf, hcValues)
    getExpiredEntriesFromRanges(rangesDF, allEntries, apiConf, repo, hcValues)
  }

  private def subtractDeduplications(
      expired: Dataset[Row],
      activeRangesDF: Dataset[Row],
      apiConf: APIConfigurations,
      repo: String,
      spark: SparkSession,
      hcValues: Broadcast[ConfMap]
  ): Dataset[Row] = {
    val activeRangesRDD: RDD[String] =
      activeRangesDF.select("range_id").rdd.distinct().map(x => x.getString(0))
    val activeAddresses: RDD[String] = activeRangesRDD
      .flatMap(range => {
        getRangeAddresses(range, apiConf, repo, hcValues)
      })
      .distinct()
    val activeAddressesRows: RDD[Row] = activeAddresses.map(x => Row(x))
    val schema = new StructType().add(StructField("address", StringType, true))
    val activeDF = spark.createDataFrame(activeAddressesRows, schema)
    // remove active addresses from delete candidates
    expired.join(
      activeDF,
      expired("address") === activeDF("address"),
      "leftanti"
    )
  }

  def main(args: Array[String]) {

    val spark = SparkSession.builder().appName("GarbageCollector").getOrCreate()
    val hc = spark.sparkContext.hadoopConfiguration

    val apiURL = hc.get(LAKEFS_CONF_API_URL_KEY)
    val accessKey = hc.get(LAKEFS_CONF_API_ACCESS_KEY_KEY)
    val secretKey = hc.get(LAKEFS_CONF_API_SECRET_KEY_KEY)
    val connectionTimeout = hc.get(LAKEFS_CONF_API_CONNECTION_TIMEOUT_SEC_KEY)
    val readTimeout = hc.get(LAKEFS_CONF_API_READ_TIMEOUT_SEC_KEY)
    val maxCommitIsoDatetime = hc.get(LAKEFS_CONF_DEBUG_GC_MAX_COMMIT_ISO_DATETIME_KEY, "")
    val runIDToReproduce = hc.get(LAKEFS_CONF_DEBUG_GC_REPRODUCE_RUN_ID_KEY, "")
    if (!maxCommitIsoDatetime.isEmpty) {
      hc.setLong(
        LAKEFS_CONF_DEBUG_GC_MAX_COMMIT_EPOCH_SECONDS_KEY,
        LocalDateTime
          .parse(hc.get(LAKEFS_CONF_DEBUG_GC_MAX_COMMIT_ISO_DATETIME_KEY),
                 DateTimeFormatter.ISO_DATE_TIME
                )
          .toEpochSecond(ZoneOffset.UTC)
      )
    }
    val apiClient = ApiClient.get(
      APIConfigurations(apiURL, accessKey, secretKey, connectionTimeout, readTimeout)
    )
    val storageType = apiClient.getBlockstoreType()

    if (storageType == StorageUtils.StorageTypeS3 && args.length != 2) {
      Console.err.println(
        "Usage: ... <repo_name> <region>"
      )
      System.exit(1)
    } else if (storageType == StorageUtils.StorageTypeAzure && args.length != 1) {
      Console.err.println(
        "Usage: ... <repo_name>"
      )
    }

    val repo = args(0)
    val region = if (args.length == 2) args(1) else null
    val previousRunID =
      "" //args(2) // TODO(Guys): get previous runID from arguments or from storage

    // Spark operators will need to generate configured FileSystems to read
    // ranges and metaranges.  They will not have a JobContext to let them
    // do that.  Transmit (all) Hadoop filesystem configuration values to
    // let them generate a (close-enough) Hadoop configuration to build the
    // needed FileSystems.
    val hcValues = spark.sparkContext.broadcast(getHadoopConfigurationValues(hc, "fs.", "lakefs."))

    var gcRules: String = ""
    try {
      gcRules = apiClient.getGarbageCollectionRules(repo)
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        println("No GC rules found for repository: " + repo)
        // Exiting with a failure status code because users should not really run gc on repos without GC rules.
        System.exit(2)
    }
    var storageNSForHadoopFS = apiClient.getStorageNamespace(repo, StorageClientType.HadoopFS)
    if (!storageNSForHadoopFS.endsWith("/")) {
      storageNSForHadoopFS += "/"
    }
    val allEntriesPath = new Path(storageNSForHadoopFS + "_lakefs/retention/gc/all_ranges/")
    new RepositoryConverter(spark, hc, repo, allEntriesPath).write()
    val allEntries = spark.read.parquet(allEntriesPath.toString)
    var prepareResult: GarbageCollectionPrepareResponse = null
    var runID = ""
    var gcCommitsLocation = ""
    var gcAddressesLocation = ""
    if (runIDToReproduce == "") {
      prepareResult = apiClient.prepareGarbageCollectionCommits(repo, previousRunID)
      runID = prepareResult.getRunId
      gcCommitsLocation =
        ApiClient.translateURI(new URI(prepareResult.getGcCommitsLocation), storageType).toString
      println("gcCommitsLocation: " + gcCommitsLocation)
      gcAddressesLocation =
        ApiClient.translateURI(new URI(prepareResult.getGcAddressesLocation), storageType).toString
      println("gcAddressesLocation: " + gcAddressesLocation)
    } else {
      // reproducing a previous run
      // TODO(johnnyaug): the server should generate these paths
      runID = UUID.randomUUID().toString
      gcCommitsLocation =
        s"${storageNSForHadoopFS.stripSuffix("/")}/_lakefs/retention/gc/commits/run_id=$runIDToReproduce/commits.csv"
      gcAddressesLocation =
        s"${storageNSForHadoopFS.stripSuffix("/")}/_lakefs/retention/gc/addresses/"
    }
    println("apiURL: " + apiURL)

    val expiredAddresses =
      getExpiredAddresses(
        repo,
        runID,
        gcCommitsLocation,
        allEntries,
        spark,
        APIConfigurations(apiURL, accessKey, secretKey, connectionTimeout, readTimeout),
        hcValues
      ).withColumn("run_id", lit(runID))
    spark.conf.set("spark.sql.sources.partitionOverwriteMode", "dynamic")
    expiredAddresses.write
      .partitionBy("run_id")
      .mode(SaveMode.Overwrite)
      .parquet(gcAddressesLocation)

    println("Expired addresses:")
    expiredAddresses.show()

    // The remove operation uses an SDK client to directly access the underlying storage, and therefore does not need
    // a translated storage namespace that triggers processing by Hadoop FileSystems.
    var storageNSForSdkClient = apiClient.getStorageNamespace(repo, StorageClientType.SDKClient)
    if (!storageNSForSdkClient.endsWith("/")) {
      storageNSForSdkClient += "/"
    }

    var removed =
      if (hc.getBoolean(LAKEFS_CONF_DEBUG_GC_NO_DELETE_KEY, false))
        spark.emptyDataFrame
      else
        remove(storageNSForSdkClient,
               gcAddressesLocation,
               expiredAddresses,
               runID,
               region,
               hcValues,
               storageType
              )

    val commitsDF = getCommitsDF(runID, gcCommitsLocation, spark)
    val reportLogsDst = concatToGCLogsPrefix(storageNSForHadoopFS, "summary")
    val reportExpiredDst = concatToGCLogsPrefix(storageNSForHadoopFS, "expired_addresses")

    val time = DateTimeFormatter.ISO_INSTANT.format(java.time.Clock.systemUTC.instant())
    writeParquetReport(commitsDF, reportLogsDst, time, "commits.parquet")
    writeParquetReport(expiredAddresses, reportExpiredDst, time)
    writeJsonSummary(reportLogsDst, removed.count(), gcRules, hcValues, time)

    removed
      .withColumn("run_id", lit(runID))
      .write
      .partitionBy("run_id")
      .mode(SaveMode.Overwrite)
      .parquet(
        concatToGCLogsPrefix(storageNSForHadoopFS, s"deleted_objects/$time/deleted.parquet")
      )

    spark.close()
  }

  private def concatToGCLogsPrefix(storageNameSpace: String, key: String): String = {
    val strippedKey = key.stripPrefix("/")
    s"${storageNameSpace}_lakefs/logs/gc/$strippedKey"
  }

  private def repartitionBySize(df: DataFrame, maxSize: Int, column: String): DataFrame = {
    val nRows = df.count()
    val nPartitions = math.max(1, math.ceil(nRows / maxSize)).toInt
    df.repartitionByRange(nPartitions, col(column))
  }

  def bulkRemove(
      readKeysDF: DataFrame,
      storageNamespace: String,
      region: String,
      hcValues: Broadcast[ConfMap],
      storageType: String
  ): Dataset[String] = {
    val bulkRemover =
      BulkRemoverFactory(storageType, configurationFromValues(hcValues), storageNamespace, region)
    val bulkSize = bulkRemover.getMaxBulkSize()
    val spark = org.apache.spark.sql.SparkSession.active
    import spark.implicits._
    val repartitionedKeys = repartitionBySize(readKeysDF, bulkSize, "address")
    val bulkedKeyStrings = repartitionedKeys
      .select("address")
      .map(_.getString(0)) // get address as string (address is in index 0 of row)

    bulkedKeyStrings
      .mapPartitions(iter => {
        // mapPartitions lambda executions are sent over to Spark executors, the executors don't have access to the
        // bulkRemover created above because it was created on the driver and it is not a serializeable object. Therefore,
        // we create new bulkRemovers.
        val bulkRemover = BulkRemoverFactory(storageType,
                                             configurationFromValues(hcValues),
                                             storageNamespace,
                                             region
                                            )
        iter
          .grouped(bulkSize)
          .flatMap(bulkRemover.deleteObjects(_, storageNamespace))
      })
  }

  def remove(
      storageNamespace: String,
      addressDFLocation: String,
      expiredAddresses: Dataset[Row],
      runID: String,
      region: String,
      hcValues: Broadcast[ConfMap],
      storageType: String
  ) = {
    println("addressDFLocation: " + addressDFLocation)

    val df = expiredAddresses
      .where(col("run_id") === runID)
      .where(col("relative") === true)

    bulkRemove(df, storageNamespace, region, hcValues, storageType).toDF("addresses")
  }

  private def writeParquetReport(
      df: DataFrame,
      dstRoot: String,
      time: String,
      suffix: String = ""
  ) = {
    val dstPath = s"${dstRoot}/dt=${time}/${suffix}"
    df.write.parquet(dstPath)
  }

  private def writeJsonSummary(
      dstRoot: String,
      numDeletedObjects: Long,
      gcRules: String,
      hcValues: Broadcast[ConfMap],
      time: String
  ) = {
    val dstPath = new Path(s"${dstRoot}/dt=${time}/summary.json")
    val dstFS = dstPath.getFileSystem(configurationFromValues(hcValues))
    val jsonSummary = JObject("gc_rules" -> gcRules, "num_deleted_objects" -> numDeletedObjects)

    val stream = dstFS.create(dstPath)
    try {
      stream.writeChars(compact(render(jsonSummary)))
    } finally {
      stream.close()
    }
  }
}
