package observatory

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{DoubleType, IntegerType}

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
  trait Info

  case class Station(stn: String, wban: String, lat: BoxedDouble, lon: BoxedDouble) extends Info

  case class LocTemp(stn: String, wban: String, month: Integer, day: Integer, temp: Double) extends Info

  private val stationDSEncoder = Seq(Station("", "", 0d, 0d)).toDS
  private val tempDSEncoder = Seq(LocTemp("", "", 0, 0, 0)).toDS

  private def predicte(info: Info): Boolean = info match {
    case Station(_, _, lat, lon) => lat != null && lon != null
    case LocTemp(_, _, _, _, temp) => temp != 9999.9
  }

  /**
    * Convert Fahrenheit to Celsius
    *
    * @param f temperature in degree Fahrenheit
    * @return temperature in degree Celsius (round to one scale)
    */
  private def fToC(f: Double) = math.round((f - 32) * 5 / 9 * 10) / 10.0

  private def getValidStations() = {
    dfReader.csv(filePath + "stations.csv")
      .withColumn("lat", 'lat.cast(DoubleType))
      .withColumn("lon", 'lon.cast(DoubleType))
      .as[Station]
      .filter(predicte _)
  }

  def extraction(year: Int) = {
    val validStations = getValidStations()
    println("valid stations " + validStations.count)

    val temperatureDS = dfReader.csv(filePath + year + ".csv")
      .withColumn("month", 'month.cast(IntegerType))
      .withColumn("day", 'day.cast(IntegerType))
      .withColumn("temp", 'temp.cast(DoubleType))
      .as[LocTemp]
    val validateTemps = temperatureDS.filter(predicte _)
    println("valid tem " + validateTemps.count)

    val locTempInfo = validateTemps.join(validStations,
      'stn && 'wban
      , "leftouter")

    println(locTempInfo.show(150))
    println(locTempInfo.count)
    val a = locTempInfo.select('lat, 'lon, 'temp, 'stn, 'wban).groupBy('stn, 'wban).avg("temp")
    println(a.show(30))
    println(a.count)
    val b = a.map(row => (Location(row.getAs[Double]("lat"), row.getAs[Double]("lon")), row.getAs[Double]("avg(temp)")))

    println(" -" + b.count + "-")
  }
}
