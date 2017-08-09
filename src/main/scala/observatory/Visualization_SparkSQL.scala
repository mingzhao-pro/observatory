package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.Extraction_SparkSQL.YearlyTemp
import org.apache.spark.sql.Dataset

object Visualization_SparkSQL {

  private val white = (60.0, Color(255, 255, 255))
  private val red = (32.0, Color(255, 0, 0))
  private val yellow = (12.0, Color(255, 255, 0))
  private val sky = (0.0, Color(0, 255, 255))
  private val blue = (-15.0, Color(0, 0, 255))
  private val violet = (-27.0, Color(255, 0, 255))
  private val purple = (-50.0, Color(33, 0, 107))
  private val black = (-60.0, Color(0, 0, 0))
  private val colors = Array(white, red, yellow, sky, blue, violet, purple, black)

  // distance: (knowStation, unknownLocation)
  var distanceMap: Map[(Location, Location), Double] = Map()
  // math
  var mathMap: Map[Location, LocMath] = Map()

  def scaledDouble(value: Double, scale: Int): Double = BigDecimal(value)
    .setScale(scale, BigDecimal.RoundingMode.HALF_UP).toDouble

  case class LocMath(location: Location) {
    val ratio = 57.2958

    import org.apache.commons.math3.util.FastMath._

    val sin_lat: Double = scaledDouble(sin(location.lat / ratio), 2)
    val cos_lat: Double = scaledDouble(cos(location.lat / ratio), 2)
    val sin_lon: Double = scaledDouble(sin(location.lon / ratio), 2)
    val cos_lon: Double = scaledDouble(cos(location.lon / ratio), 2)
  }

  /**
    *
    * @param location
    * @return
    */
  def computeLocationMath(location: Location) = mathMap.get(location) match {
    case Some(math) => math
    case None => {
      val math = LocMath(location)
      mathMap += (location -> math)
      math
    }
  }

  def predicate(yearlyTemp: YearlyTemp) = mathMap += (yearlyTemp.location -> LocMath(yearlyTemp.location))

  /**
    *
    * @param temperatures
    */
  def computeKnownMathMap(temperatures: Dataset[YearlyTemp]) =
    temperatures.foreach(predicate _)

  /**
    *
    * @param knownStation
    * @param unknownLocation
    * @return distance between two location
    */
  def computeDistance(knownStation: Location, unknownLocation: Location): Double = {
    distanceMap.get((knownStation, unknownLocation)) match {
      case Some(distance) => distance
      case None => {
        val knownMath = computeLocationMath(knownStation)
        val unknownMath = computeLocationMath(unknownLocation)
        val x = knownMath.cos_lat * knownMath.cos_lon - unknownMath.cos_lat * unknownMath.cos_lon
        val y = knownMath.cos_lat * knownMath.sin_lat - unknownMath.cos_lat * unknownMath.sin_lon
        val z = knownMath.sin_lat - unknownMath.sin_lat

        import org.apache.commons.math3.util.FastMath._
        val bigC = sqrt(pow(x, 2) + pow(y, 2) + pow(z, 2))
        val distance = asin(bigC / 2) * 2 * 6371
        distanceMap += ((knownStation, unknownLocation) -> distance)
        distance
      }
    }
  }
}
