package observatory

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.DoubleType

object Extraction_SparkSQL {

  private val filePath = "src/main/resources/"

  // Spark Context creation
  private val conf = new SparkConf().setMaster("local[8]").setAppName("temperature")
  private val spark = SparkSession.builder().config(conf).getOrCreate

  import spark.implicits._

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
      .groupBy("_1").avg("_2")
      .toDF("stationId", "temp")
  }

  def extraction(year: Int) = {
    val validStations = loadValidStations()
    val validTemps = getValidLocTemp(1975)
    validTemps.join(validStations,
      validTemps("stationId") === validStations("stationId"), "inner")
  }
}
