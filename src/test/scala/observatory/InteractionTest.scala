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

    val locationsTemperatures = List(
      (new Location(45.0, -90.0), 20.0)
      , (new Location(45.0, 90.0), 0.0)
      , (new Location(0.0, 0.0), 10.0)
      , (new Location(-45.0, -90.0), 0.0)
      , (new Location(-45.0, 90.0), 20.0)
    )

    val colorMap = List(
      (0.0, Color(255, 0, 0))
      , (10.0, Color(0, 255, 0))
      , (20.0, Color(0, 0, 255))
    )


    val z = 0
    val n = math.pow(2, z).toInt
    for (x <- 0 until n; y <- 0 until n) {
      val image = Interaction.tile(locationsTemperatures, colorMap, z, x, y)
      image.output(new File("C:\\Users\\xxtu716\\IdeaProjects\\observatory\\tiles\\temperatures\\" + z + "-" + x + "-" + y + ".png"))
    }
  }

}
