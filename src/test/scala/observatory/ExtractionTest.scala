package observatory

import org.apache.spark.SparkConf
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class ExtractionTest extends FunSuite {

  test("locateTemperatures") {

    val t = Extraction.locateTemperatures(1975, "/stations.csv", "/1975.csv")
    println(t.size)

//    val filePath = "src/main/resources"
//    case class Station(stn: String, wban: String, lat: Int, lon: Int)
//    case class Temperature(stn: String, wban: String, month: String, day: String, temperature: String)
//
//    val conf = new SparkConf().setMaster("local[8]").setAppName("widipedia")
//    val spark = SparkSession.builder().config(conf).getOrCreate
//
//    val stationRDD = spark.sparkContext.textFile(filePath + "stations.csv")
//                        .map(_.split(","))
//      .map(attr => Row(attr(0),attr(1),attr(2),attr(3)))
//
//    val fieldSTN = StructField("stn", StringType, nullable = true)
//    val fieldWBAN = StructField("wban", StringType, nullable = true)
//    val fieldLAT = StructField("lat", IntegerType, nullable = false)
//    val fieldLON = StructField("lon", IntegerType, nullable = false)
//    val fields = Seq(fieldSTN, fieldWBAN, fieldLAT, fieldLON)
//    val schema = StructType(fields)
//
//    val stationDF = spark.createDataFrame(stationRDD, schema)
//    stationDF.printSchema()
//
//
//
//    assert(true)
  }

}