package main

import (
	"encoding/json"
	"fmt"
	"math/rand"
	"os"
	"sort"

	"github.com/cockroachdb/pebble/sstable"
	fuzz "github.com/google/gofuzz"
	nanoid "github.com/matoous/go-nanoid/v2"
)

const (
	DefaultKeySizeBytes   = 100
	DefaultValueSizeBytes = 5
	KiBToBytes            = 1 << 10
	MiBToBytes            = KiBToBytes << 10
	FuzzerSeed            = 50
	// DefaultCommittedPermanentMaxRangeSizeBytes - the max file size that can be written by graveler (pkg/config/config.go)
	DefaultCommittedPermanentMaxRangeSizeBytes = 20 * MiBToBytes
	DefaultSstSizeBytes                        = 50 * KiBToBytes
	DefaultTwoLevelSstSizeBytes                = 10 * KiBToBytes
	DefaultMaxSizeKib                          = 100
)

var DefaultUserProperties = map[string]string{
	"user_prop_1": "val1",
	"user_prop_2": "val2",
}

type Entry struct {
	Key   string
	Value string
}

func main() {
	writeTwoLevelIdxSst()
	writeMultiSizedSstsWithContentsFuzzing()
	writeSstsWithWriterOptionsFuzzing()
	writeSstsWithUnsupportedWriterOptions()
	writeLargeSsts()
}

func generateNanoid() (string, error) {
	return nanoid.New(DefaultValueSizeBytes)
}

func newGenerateFuzz() func() (string, error) {
	f := fuzz.NewWithSeed(FuzzerSeed)
	return func() (string, error) {
		var val string
		f.Fuzz(&val)
		return val, nil
	}
}

func writeSstsWithUnsupportedWriterOptions() {
	sizeBytes := DefaultSstSizeBytes
	keys := prepareSortedSliceWithNanoid(sizeBytes)

	// Checksum specifies which checksum to use. lakeFS supports parsing sstables with crc checksum.
	// ChecksumTypeXXHash and ChecksumTypeNone are unsupported and therefore not tested.
	writerOptions := sstable.WriterOptions{
		Compression: sstable.SnappyCompression,
		Checksum:    sstable.ChecksumTypeXXHash64,
	}
	createTestInputFiles(keys, generateNanoid, sizeBytes,
		"checksum.type.xxHash64", writerOptions)

	// TableFormat specifies the format version for writing sstables. The default
	// is TableFormatRocksDBv2 which creates RocksDB compatible sstables. this is the only format supported by lakeFS.
	writerOptions = sstable.WriterOptions{
		Compression: sstable.SnappyCompression,
		TableFormat: sstable.TableFormatLevelDB,
	}
	createTestInputFiles(keys, generateNanoid, sizeBytes, "table.format.leveldb",
		writerOptions)
}

func writeSstsWithWriterOptionsFuzzing() {
	sizeBytes := DefaultSstSizeBytes
	keys := prepareSortedSliceWithNanoid(sizeBytes)

	// Use fuzzing to define sstable user properties.
	generateFuzz := newGenerateFuzz()
	userProps := map[string]string{}
	const numOfUserDefinedProps = 20
	for i := 1; i <= numOfUserDefinedProps; i++ {
		propKey, _ := generateFuzz()
		propVal, _ := generateFuzz()
		userProps[propKey] = propVal
		fmt.Printf("user property %d, key=%s, val=%s\n", i, propKey, propVal)
	}
	writerOptions := sstable.WriterOptions{
		Compression:             sstable.SnappyCompression,
		TablePropertyCollectors: []func() sstable.TablePropertyCollector{NewStaticCollector(userProps)},
	}
	createTestInputFiles(keys, generateNanoid, sizeBytes, "fuzz.table.properties", writerOptions)

	// BlockRestartInterval is the number of keys between restart points
	// for delta encoding of keys.
	// The default value is 16.
	src1 := rand.NewSource(5092021)
	blockRestartInterval := 1 + rand.New(src1).Intn(100)
	writerOptions = sstable.WriterOptions{
		Compression:             sstable.SnappyCompression,
		TablePropertyCollectors: []func() sstable.TablePropertyCollector{NewStaticCollector(DefaultUserProperties)},
		BlockRestartInterval:    blockRestartInterval,
	}
	fmt.Printf("BlockRestartInterval %d...\n", blockRestartInterval)
	createTestInputFiles(keys, generateNanoid, sizeBytes, "fuzz.block.restart.interval", writerOptions)

	// BlockSize is the target uncompressed size in bytes of each table block.
	// The default value is 4096.
	src2 := rand.NewSource(881989)
	blockSize := 1 + rand.New(src2).Intn(9000)
	writerOptions = sstable.WriterOptions{
		Compression:             sstable.SnappyCompression,
		TablePropertyCollectors: []func() sstable.TablePropertyCollector{NewStaticCollector(DefaultUserProperties)},
		BlockSize:               blockSize,
	}
	fmt.Printf("BlockSize %d...\n", blockSize)
	createTestInputFiles(keys, generateNanoid, sizeBytes, "fuzz.block.size", writerOptions)

	// BlockSizeThreshold finishes a block if the block size is larger than the
	// specified percentage of the target block size and adding the next entry
	// would cause the block to be larger than the target block size.
	//
	// The default value is 90
	src3 := rand.NewSource(123432)
	blockSizeThreshold := 1 + rand.New(src3).Intn(100)
	writerOptions = sstable.WriterOptions{
		Compression:             sstable.SnappyCompression,
		TablePropertyCollectors: getDefaultTablePropertyCollectors(DefaultUserProperties),
		BlockSizeThreshold:      blockSizeThreshold,
	}
	fmt.Printf("BlockSizeThreshold %d...\n", blockSizeThreshold)
	createTestInputFiles(keys, generateNanoid, sizeBytes, "fuzz.block.size.threshold", writerOptions)
}

func writeLargeSsts() {
	sizeBytes := DefaultCommittedPermanentMaxRangeSizeBytes
	keys := prepareSortedSliceWithNanoid(sizeBytes)
	writerOptions := sstable.WriterOptions{
		Compression:             sstable.SnappyCompression,
		TablePropertyCollectors: getDefaultTablePropertyCollectors(DefaultUserProperties),
	}
	createTestInputFiles(keys, generateNanoid, sizeBytes, "max.size.lakefs.file", writerOptions)
}

func writeMultiSizedSstsWithContentsFuzzing() {
	const numOfFilesToGenerate = 6
	src := rand.NewSource(980433)
	r := rand.New(src)
	for i := 0; i < numOfFilesToGenerate; i++ {
		var curSize int
		if i%2 == 0 {
			curSize = 1 + r.Intn(DefaultCommittedPermanentMaxRangeSizeBytes/MiBToBytes)*MiBToBytes
		} else {
			curSize = 1 + r.Intn(DefaultMaxSizeKib)*KiBToBytes
		}

		testFileName := fmt.Sprintf("fuzz.contents.%d", i)
		keys := prepareSortedSliceWithFuzzing(curSize)

		writerOptions := sstable.WriterOptions{
			Compression:             sstable.SnappyCompression,
			TablePropertyCollectors: getDefaultTablePropertyCollectors(DefaultUserProperties),
		}
		createTestInputFiles(keys, newGenerateFuzz(), curSize, testFileName, writerOptions)
	}
}

func prepareSortedSliceWithFuzzing(size int) []string {
	var keySumBytes int
	var slice []string
	generateFuzz := newGenerateFuzz()
	// Keep generating keys until we reach the desired slice size. This guarantees that the sstable writer will have
	// enough data to write an sstable of the desired size.
	for keySumBytes < size {
		key, _ := generateFuzz()
		if key == "" { // TODO(Tals): remove after resolving https://github.com/treeverse/lakeFS/issues/2419
			continue
		}
		keySumBytes += len(key)
		slice = append(slice, key)
	}
	sort.Strings(slice)
	return slice
}

func writeTwoLevelIdxSst() {
	sizeBytes := DefaultTwoLevelSstSizeBytes
	keys := prepareSortedSliceWithNanoid(sizeBytes)

	writerOptions := sstable.WriterOptions{
		Compression:             sstable.SnappyCompression,
		IndexBlockSize:          5, // setting the index block size target to a small number to make 2-level index get enabled
		TablePropertyCollectors: getDefaultTablePropertyCollectors(DefaultUserProperties),
	}
	createTestInputFiles(keys, generateNanoid, sizeBytes, "two.level.idx", writerOptions)
}

func prepareSortedSliceWithNanoid(size int) []string {
	numLines := size / DefaultKeySizeBytes
	slice := make([]string, 0, numLines)
	for i := 0; i < numLines; i++ {
		key, err := nanoid.New(DefaultKeySizeBytes)
		if err != nil {
			panic(err)
		}
		slice = append(slice, key)
	}
	sort.Strings(slice)
	return slice
}

// create sst and json files that will be used to unit test the sst block parser (clients/spark/core/src/main/scala/io/treeverse/jpebble/BlockParser.scala)
// The sst file is the test input, and the json file encapsulates the expected parser output.
func createTestInputFiles(keys []string, genValue func() (string, error), size int, name string,
	writerOptions sstable.WriterOptions) {
	fmt.Printf("Generate %s of size %d bytes\n", name, size)
	sstFileName := name + ".sst"
	sstFile, err := os.Create(sstFileName)
	if err != nil {
		panic(err)
	}
	writer := sstable.NewWriter(sstFile, writerOptions)
	defer func() {
		_ = writer.Close()
	}()

	expectedContents := make([]Entry, 0, len(keys))
	for _, k := range keys {
		if writer.EstimatedSize() >= uint64(size) {
			break
		}
		v, err := genValue()
		if err != nil {
			panic(err)
		}
		if err := writer.Set([]byte(k), []byte(v)); err != nil {
			panic(fmt.Errorf("setting key and value: %w, into %w", err, sstFileName))
		}
		expectedContents = append(expectedContents, Entry{Key: k, Value: v})
	}

	// save the expected contents as json
	jsonFileName := name + ".json"
	jsonWriter, err := os.Create(jsonFileName)
	if err != nil {
		panic(err)
	}
	defer func() {
		jsonWriter.Close()
	}()
	err = json.NewEncoder(jsonWriter).Encode(expectedContents)
	if err != nil {
		panic(err)
	}
}

// staticCollector - Copied from pkg/graveler/sstable/collectors.go. lakeFS populates the following user properties for each sstable
// graveler writes: type (range/metarange), min_key, max_key, count, estimated_size_bytes
// staticCollector is an sstable.TablePropertyCollector that adds a map's values to the user
// property map.
type staticCollector struct {
	m map[string]string
}

func (*staticCollector) Add(key sstable.InternalKey, value []byte) error {
	return nil
}

func (*staticCollector) Name() string {
	return "static"
}

func (s *staticCollector) Finish(userProps map[string]string) error {
	for k, v := range s.m {
		userProps[k] = v
	}
	return nil
}

// NewStaticCollector returns an SSTable collector that will add the properties in m when
// writing ends.
func NewStaticCollector(m map[string]string) func() sstable.TablePropertyCollector {
	return func() sstable.TablePropertyCollector { return &staticCollector{m} }
}

func getDefaultTablePropertyCollectors(m map[string]string) []func() sstable.TablePropertyCollector {
	return []func() sstable.TablePropertyCollector{NewStaticCollector(m)}
}
