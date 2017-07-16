package observatory

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.Checkers

@RunWith(classOf[JUnitRunner])
class Visualization2Test extends FunSuite with Checkers {

  test("bilinear") {
    //d00 = -30.068763770206363, d01 = -26.34948772335314, d10 = 0.0, d11 = 0.0, x = 0.0, y = 0.0

    println(Visualization2.bilinearInterpolation(0.0, 0.0, -30.068763770206363, -26.34948772335314, 0.0, 0.0))
  }

}
