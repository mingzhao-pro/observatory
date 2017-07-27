package observatory

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._
import org.slf4j.LoggerFactory

object Main extends App {

  val logger = LoggerFactory.getLogger(Main.getClass)
  val filePath = "src/main/resources/"

  val year = 1975

  // Inner usage methods
  private def fToC(f: Double) = math.round((f - 32) * 5 / 9 * 10) / 10.0

  // Spark Context creation
  val conf = new SparkConf().setMaster("local[8]").setAppName("widipedia")
  val spark = SparkSession.builder().config(conf).getOrCreate
  import spark.implicits._

  // Case classes and encoders
  trait Info
  case class Station(stn: String, wban: String, lat: Integer, lon: Integer) extends Info
  case class LocTemp(stn: String, wban: String, month: Integer, day: Integer, temp: Double) extends Info
  val stationDSEncoder = Seq(Station("", "", 0, 0)).toDS
  val tempDSEncoder = Seq(LocTemp("", "", 0, 0, 0)).toDS

  def predicte(info: Info): Boolean = info match {
    case Station(_, _, lat, lon) => lat != null && lon != null
    case LocTemp(_, _, _, _, temp) => temp != 9999.9
  }


  // Creation of stationDS and temperatureDS
  val stationDS = spark.read.option("header", "true").csv(filePath + "stations.csv")
    .withColumn("lat", 'lat.cast(IntegerType))
    .withColumn("lon", 'lon.cast(IntegerType))
    .as[Station]
  println(stationDS.count)
  val validStations = stationDS.filter(predicte _)
  println(validStations.count)

  val temperatureDS = spark.read.option("header", "true").csv(filePath + year + ".csv")
    .withColumn("month", 'month.cast(IntegerType))
    .withColumn("day", 'day.cast(IntegerType))
    .withColumn("temp", 'temp.cast(DoubleType))
    .as[LocTemp]
  println(temperatureDS.count)
  val validateTemps = temperatureDS.filter(predicte _)
  println(validateTemps.count)

}
