package observatory

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.Checkers

@RunWith(classOf[JUnitRunner])
class Interaction2Test extends FunSuite with Checkers {

  test("") {
    import observatory.{Interaction2, Signal}

    val layers = Interaction2.availableLayers

    for(x <- layers)
      println(Interaction2.yearSelection(Signal(x), Signal(2010))())
  }
}
