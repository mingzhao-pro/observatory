package observatory

import java.io.File

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.Checkers

import scala.collection.concurrent.TrieMap

@RunWith(classOf[JUnitRunner])
class InteractionTest extends FunSuite with Checkers {

  val white = (60.0, Color(255, 255, 255))
  val red = (32.0, Color(255, 0, 0))
  val yellow = (12.0, Color(255, 255, 0))
  val sky = (0.0, Color(0, 255, 255))
  val blue = (-15.0, Color(0, 0, 255))
  val violet = (-27.0, Color(255, 0, 255))
  val purple = (-50.0, Color(33, 0, 107))
  val black = (-60.0, Color(0, 0, 0))


//  test("creation of tile") {
//
//    val colors = Array(white, red, yellow, sky, blue, violet, purple, black)
//    val locTemps = Extraction.locationYearlyAverageRecords(Extraction.locateTemperatures(2000, "/stations.csv", "/2000.csv"))
//
//    Interaction.tile(locTemps, colors, 0, 0, 0)
//  }

  test("creation of tile list") {

    val colors = Array(white, red, yellow, sky, blue, violet, purple, black)
    val year = 1975
    val locTemps = Extraction.locateTemperatures(year, "/stations.csv", "/" + year.toString + ".csv")
    val yearlyData = Extraction.locationYearlyAverageRecords(locTemps)
    val datum = Array((1975, yearlyData))

    def calculate(year: Int, zoom: Int, x: Int, y: Int, data: Iterable[(Location, Double)]) = {
      val image = Interaction.tile(data, colors, zoom, x, y)
      image.output(new File("/home/user/IdeaProjects/observatory/tiles/temperatures/"+ year + "/" + zoom + "/" + x + "-" + y + ".png"))
    }

    Interaction.generateTiles(datum, calculate)
  }

}
