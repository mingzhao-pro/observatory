package observatory

import java.io.File

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.Checkers

@RunWith(classOf[JUnitRunner])
class VisualizationTest extends FunSuite with Checkers {

  val white = (60.0, Color(255, 255, 255))
  val red = (32.0, Color(255, 0, 0))
  val yellow = (12.0, Color(255, 255, 0))
  val sky = (0.0, Color(0, 255, 255))
  val blue = (-15.0, Color(0, 0, 255))
  val violet = (-27.0, Color(255, 0, 255))
  val purple = (-50.0, Color(33, 0, 107))
  val black = (-60.0, Color(0, 0, 0))


  test("creation of image") {
    val color = Array(white, red, yellow, sky, blue, violet, purple, black)
    val locTemps = Extraction.locationYearlyAverageRecords(Extraction.locateTemperatures(2000, "/stations.csv", "/2000.csv"))
    val image = Visualization.visualize(locTemps, color.toIterable)
    image.output(new File("/home/saga/IdeaProjects/observatory/target/mage.png"))
  }
}



































