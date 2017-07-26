package observatory

import java.time.LocalDate

import org.apache.spark.SparkConf
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types._
import org.slf4j.LoggerFactory

object Main extends App {
  val logger = LoggerFactory.getLogger(Main.getClass)
  val filePath = "src/main/resources/"
  val fieldSTN = StructField("stn", StringType, nullable = true)
  val fieldWBAN = StructField("wban", StringType, nullable = true)
  val fieldLAT = StructField("lat", DoubleType, nullable = false)
  val fieldLON = StructField("lon", DoubleType, nullable = false)
  val fieldMonth = StructField("month", IntegerType, nullable = false)
  val fieldDay = StructField("day", IntegerType, nullable = false)
  val fieldTemp = StructField("temp", DoubleType, nullable = false)
  val year = 1975

  case class Station(stn: String, wban: String, lat: Int, lon: Int)
  case class Temperature(stn: String, wban: String, month: String, day: String, temperature: String)

  val conf = new SparkConf().setMaster("local[8]").setAppName("widipedia")
  val spark = SparkSession.builder().config(conf).getOrCreate

  // Create station data frame
  val stationRDD = spark.sparkContext.textFile(filePath + "stations.csv")
    .map(_.split(","))
    .filter(_.length == 4)
    .map(attr => Row(attr(0), attr(1),attr(2).toDouble,attr(3).toDouble))
  val fields = Seq(fieldSTN, fieldWBAN, fieldLAT, fieldLON)
  val schema = StructType(fields)
  val stationDF = spark.createDataFrame(stationRDD, schema)
  stationDF.createOrReplaceTempView("stations")

  // Create temperature data frame
  val tempRDD = spark.sparkContext.textFile(filePath + year + ".csv")
    .map(_.split(","))
    .filter(_.last != "9999.9")
    .map(attr => Row(attr(0), attr(1), attr(2).toInt, attr(3).toInt, attr(4).toDouble))
  val fields_t = Seq(fieldSTN, fieldWBAN, fieldMonth, fieldDay, fieldTemp)
  val schema_t = StructType(fields_t)
  val tempDF = spark.createDataFrame(tempRDD, schema_t)
  tempDF.createOrReplaceTempView("temperatures")

  val infoLinesDF = spark.sql("SELECT month, day, temp FROM temperatures INNER JOIN stations " +
    "ON temperatures.stn = stations.stn AND temperatures.wban = stations.wban")
  println(infoLinesDF.count)//2177190



}
