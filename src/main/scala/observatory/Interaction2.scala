package observatory

import observatory.LayerName.{Deviations, Temperatures}

/**
  * 6th (and last) milestone: user interface polishing
  */
object Interaction2 {

  /**
    * @return The available layers of the application
    */
  def availableLayers: Seq[Layer] = {
    val white = (60.0, Color(255, 255, 255))
    val red = (32.0, Color(255, 0, 0))
    val yellow = (12.0, Color(255, 255, 0))
    val sky = (0.0, Color(0, 255, 255))
    val blue = (-15.0, Color(0, 0, 255))
    val violet = (-27.0, Color(255, 0, 255))
    val purple = (-50.0, Color(33, 0, 107))
    val black = (-60.0, Color(0, 0, 0))
    val colorScaleForTemp = Seq(white, red, yellow, sky, blue, violet, purple, black)
    val tempLayer = Layer(Temperatures, colorScaleForTemp, Range(1975, 1989, 1))

    val black_ = (7.0, Color(0, 0, 0))
    val red_ = (4.0, Color(255, 0, 0))
    val yellow_ = (2.0, Color(255, 255, 0))
    val white_ = (0.0, Color(255, 255, 255))
    val sky_ = (-2.0, Color(0, 255, 255))
    val blue_ = (-7.0, Color(0, 0, 255))
    val colorScaleForDev = Seq(black_, red_, yellow_, white_, sky_, blue_)
    val devLayer = Layer(Deviations, colorScaleForDev, Range(1990, 2015, 1))

    Seq(tempLayer, devLayer)
  }

  /**
    * @param selectedLayer A signal carrying the layer selected by the user
    * @return A signal containing the year bounds corresponding to the selected layer
    */
  def yearBounds(selectedLayer: Signal[Layer]): Signal[Range] = {
    Signal(selectedLayer().bounds)
  }

  /**
    * @param selectedLayer The selected layer
    * @param sliderValue The value of the year slider
    * @return The value of the selected year, so that it never goes out of the layer bounds.
    *         If the value of `sliderValue` is out of the `selectedLayer` bounds,
    *         this method should return the closest value that is included
    *         in the `selectedLayer` bounds.
    */
  def yearSelection(selectedLayer: Signal[Layer], sliderValue: Signal[Int]): Signal[Int] = {
    val range = yearBounds(selectedLayer)
    Signal(if(range().contains(sliderValue())) sliderValue()
    else if(range().start > sliderValue()) range().start
    else range().end)
  }

  /**
    * @param selectedLayer The selected layer
    * @param selectedYear The selected year
    * @return The URL pattern to retrieve tiles
    */
  def layerUrlPattern(selectedLayer: Signal[Layer], selectedYear: Signal[Int]): Signal[String] = {
    Signal("target/" + selectedLayer().layerName.id + "/" + selectedYear())
  }

  /**
    * @param selectedLayer The selected layer
    * @param selectedYear The selected year
    * @return The caption to show
    */
  def caption(selectedLayer: Signal[Layer], selectedYear: Signal[Int]): Signal[String] = {
    val name = Signal(selectedLayer().layerName.id)
    Signal(name().substring(0, 1).toUpperCase + name().substring(1) + " (" + selectedYear() + ")")
  }

}

sealed abstract class LayerName(val id: String)
object LayerName {
  case object Temperatures extends LayerName("temperatures")
  case object Deviations extends LayerName("deviations")
}

/**
  * @param layerName Name of the layer
  * @param colorScale Color scale used by the layer
  * @param bounds Minimum and maximum year supported by the layer
  */
case class Layer(layerName: LayerName, colorScale: Seq[(Double, Color)], bounds: Range)

