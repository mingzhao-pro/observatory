package observatory

import org.apache.spark.SparkConf
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types.DoubleType

object Extraction_SparkSQL {

  type BoxedDouble = java.lang.Double
  //  private val logger = LoggerFactory.getLogger(Main.getClass)
  private val filePath = "src/main/resources/"

  // Spark Context creation
  private val conf = new SparkConf().setMaster("local[8]").setAppName("temperature")
  private val spark = SparkSession.builder().config(conf).getOrCreate

  import spark.implicits._

  private val dfReader = spark.read.option("header", "true")

  // Case classes and encoders
  trait WeatherInfo

  case class Station(var stn: String, var wban: String, lat: String, lon: String) extends WeatherInfo

  case class LocTemp(stn: String, wban: String, month: String, day: String, temp: Double) extends WeatherInfo
  case class Tu(stn: String, wban: String, temp: Double)

  private val stationDSEncoder = Seq(Station("", "", "", "")).toDS
  private val tempDSEncoder = Seq(LocTemp("", "", "", "", 0)).toDS

  private def predicte(info: WeatherInfo): Boolean = info match {
    case Station(stn, wban, lat, lon) => lat != null && lon != null && !(stn == null && wban == null)
    case LocTemp(stn, wban, _, _, temp) => temp != 9999.9 && !(stn == null && wban == null)
  }

  /**
    * Convert Fahrenheit to Celsius
    *
    * @param f temperature in degree Fahrenheit
    * @return temperature in degree Celsius (round to one scale)
    */
  private def fToC(f: Double) = math.round((f - 32) * 5 / 9 * 10) / 10.0

  private def transformStation(s: Station) = s match {
    case Station(null, wban, lat, lon) => Station("_", wban, lat, lon)
    case Station(stn, null, lat, lon) => Station(stn, "_", lat, lon)
    case _ => s
  }

  private def transformTemp(t: LocTemp) = t match {
    case LocTemp(null, wban, month, day, temp) => LocTemp("_", wban, month, day, temp)
    case LocTemp(stn, null, month, day, temp) => LocTemp(stn, "_", month, day, temp)
    case _ => t
  }

  private def getValidStations() = {
    dfReader.csv(filePath + "stations.csv")
      .as[Station]
      .filter(predicte _)
      .map(transformStation)
  }

  private def getValidLocTemp(year: Int) = {
    dfReader.csv(filePath + year + ".csv")
      .withColumn("temp", 'temp.cast(DoubleType))
      .as[LocTemp]
      .filter(predicte _)
      .map(transformTemp)
  }

  def extraction(year: Int) = {
    val validStations = getValidStations()
    val validTemps = getValidLocTemp(1975).groupBy('stn, 'wban).avg("temp").toDF("stn", "wban", "temp").as[Tu]

    val locTempInfo = validTemps.join(validStations,
      validTemps("stn") === validStations("stn") && validTemps("wban") === validStations("wban"), "inner")

    println(locTempInfo.count)
  }
}
