package observatory

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.Checkers

@RunWith(classOf[JUnitRunner])
class Visualization2Test extends FunSuite with Checkers {


  //  val white = (60.0, Color(255, 255, 255))
  //  val red = (32.0, Color(255, 0, 0))
  //  val yellow = (12.0, Color(255, 255, 0))
  //  val sky = (0.0, Color(0, 255, 255))
  //  val blue = (-15.0, Color(0, 0, 255))
  //  val violet = (-27.0, Color(255, 0, 255))
  //  val purple = (-50.0, Color(33, 0, 107))
  //  val black = (-60.0, Color(0, 0, 0))
  //
  //  val colors_t = Array(white, red, yellow, sky, blue, violet, purple, black)
  //
  //
  //  val black_ = (7.0, Color(0, 0, 0))
  //  val red_ = (4.0, Color(255, 0, 0))
  //  val yellow_ = (2.0, Color(255, 255, 0))
  //  val white_ = (0.0, Color(255, 255, 255))
  //  val sky_ = (-2.0, Color(0, 255, 255))
  //  val blue_ = (-7.0, Color(0, 0, 255))
  //  val colors_d = Seq(black_, red_, yellow_, white_, sky_, blue_)
  //

  val white = (60.0, Color(255, 255, 255))
  val red = (32.0, Color(255, 0, 0))
  val yellow = (12.0, Color(255, 255, 0))
  val sky = (0.0, Color(0, 255, 255))
  val blue = (-15.0, Color(0, 0, 255))
  val violet = (-27.0, Color(255, 0, 255))
  val purple = (-50.0, Color(33, 0, 107))
  val black = (-60.0, Color(0, 0, 0))

  test("bilinear") {
    val colors = Array(white, red, yellow, sky, blue, violet, purple, black)

    val referenceTemps = for(year <- 1975 to 1990) yield {
      Extraction.locationYearlyAverageRecords(Extraction.locateTemperatures(year, "/stations.csv", "/" + year + ".csv"))
    }

    val normals = Manipulation.average(referenceTemps)

    for(year <- 1991 to 2015) {
      val temps = Extraction.locationYearlyAverageRecords(Extraction.locateTemperatures(year, "/stations.csv", "/" + year + ".csv"))
      val deviationGrid = Manipulation.deviation(temps, normals)

    }

  }

}
