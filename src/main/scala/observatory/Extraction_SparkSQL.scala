package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.Visualization_SparkSQL.{computeDistance, computeKnownMathMap, scaledDouble}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.sql.types.DoubleType

object Extraction_SparkSQL {

  case class YearlyTemp(location: Location, temp: Double)
  private val filePath = "src/main/resources/"

  // Spark Context creation
  private val conf = new SparkConf().setMaster("local[8]").setAppName("temperature")
  private val spark = SparkSession.builder().config(conf).getOrCreate

  import spark.implicits._
  val yearlyTempEncoder: Dataset[YearlyTemp] = Seq(YearlyTemp(Location(0, 0), 0)).toDS

  /**
    * Convert Fahrenheit to Celsius
    *
    * @param f temperature in degree Fahrenheit
    * @return temperature in degree Celsius (round to one scale)
    */
  private def fToC(f: Double) = math.round((f - 32) * 5 / 9 * 10) / 10.0

  private def loadValidStations() = {
    spark.read.csv(filePath + "stations.csv")
      .toDF("stn", "wban", "lat", "lon")
      .withColumn("lat", 'lat.cast(DoubleType))
      .withColumn("lon", 'lon.cast(DoubleType))
      .filter('lat.isNotNull && 'lon.isNotNull && !('stn.isNull && 'wban.isNull))
      .map(row => {
        val stn = row.getAs[String]("stn")
        val modifiedStn = if (stn == null) "_" else stn
        val wban = row.getAs[String]("wban")
        val modifiedWban = if (wban == null) "_" else wban
        (modifiedStn + "_" + modifiedWban, Location(row.getAs[Double]("lat"), row.getAs[Double]("lon")))
      }).toDF("stationId", "location")
  }

  private def getValidLocTemp(year: Int) = {
    spark.read.csv(filePath + year + ".csv")
      .toDF("stn", "wban", "month", "day", "temp")
      .withColumn("temp", 'temp.cast(DoubleType))
      .filter(!('stn.isNull && 'wban.isNull))
      .map(row => {
        val stn = row.getAs[String]("stn")
        val modifiedStn = if (stn == null) "_" else stn
        val wban = row.getAs[String]("wban")
        val modifiedWban = if (wban == null) "_" else wban
        (modifiedStn + "_" + modifiedWban, fToC(row.getAs[Double]("temp")))
      })
      .groupBy("_1").avg("_2")//_1 is stationId, _2 is temperature
      .toDF("stationId", "temp")
  }

  def extraction(year: Int) = {
    val validStations = loadValidStations()
    val validTemps = getValidLocTemp(year)
    validTemps.join(validStations,
      validTemps("stationId") === validStations("stationId"), "inner").select('location, 'temp)
      .as[YearlyTemp]
  }

  def reduceDistance(a: (Double, Double), b: (Double, Double)) = (a._1 + b._1, a._2 + b._2)

  /**
    *
    * @param stations
    * @param colors
    * @return
    */
  def visualize(stations: Dataset[YearlyTemp], colors: Iterable[(Double, Color)]): Image = {
    val LAT_MAX = 180
    val LON_MAX = 360
    import org.apache.commons.math3.util.FastMath._

    // one time per file
    val scaledStations = stations.map(station => {
      val location = station.location
      val scaledLat = scaledDouble(location.lat, 0)
      val scaledLon = scaledDouble(location.lon, 0)
      val temp = station.temp
      val scaledTemp = scaledDouble(temp, 2)
      (Location(scaledLat, scaledLon), scaledTemp)
    })

    val latRange = Range(90, -90, -1)
    val lonRange = Range(-180, 180, 1)

    computeKnownMathMap(stations)

    val pixelArray = for (lat <- latRange; lon <- lonRange) yield {
      val unknownLocation = Location(lat, lon)
      val distanceInfo = scaledStations
        .map(station => YearlyTemp(station._1, station._2))
        .map(station =>
          (station.temp, computeDistance(station.location, unknownLocation)))
        .map({ case (temp, distance) => (pow(1 / distance, 4), temp) })
        .map({ case (distance, temp) => (distance * temp, distance) })
        .reduce((a, b) => reduceDistance(a, b))

      val temperature = scaledDouble(distanceInfo._1 / distanceInfo._2, 2)
      val color = Visualization.interpolateColor(colors, temperature)
      Pixel(color.red, color.green, color.blue, 127)
    }

    Image(LON_MAX, LAT_MAX, pixelArray.toArray, 2)
  }
}
