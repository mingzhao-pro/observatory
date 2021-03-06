package observatory

import com.sksamuel.scrimage.{Image, Pixel}

/**
  * 3rd milestone: interactive visualization
  */
object Interaction {

  /**
    * @param zoom Zoom level
    * @param x    X coordinate
    * @param y    Y coordinate
    * @return The latitude and longitude of the top-left corner of the tile, as per http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
    */
  def tileLocation(zoom: Int, x: Int, y: Int): Location = {
    val n = math.pow(2, zoom)
    val lon = x / n * 360 - 180
    import org.apache.commons.math3.util.FastMath._
    val lat = atan(sinh(math.Pi * (1 - y * 2 / n))) * 180 / math.Pi
    Location(lat, lon)
  }

  /**
    * @param temperatures Known temperatures
    * @param colors       Color scale
    * @param zoom         Zoom level
    * @param x            X coordinate
    * @param y            Y coordinate
    * @return A 256×256 image showing the contents of the tile defined by `x`, `y` and `zooms`
    */
  def tile(temperatures: Iterable[(Location, Double)], colors: Iterable[(Double, Color)], zoom: Int, x: Int, y: Int): Image = {

    def transform(x_index: Int, y_index: Int) = {
      val lon = math.toDegrees(x_index * math.Pi / 128 / math.pow(2, zoom) - math.Pi)
      val lat = math.toDegrees((math.atan(math.pow(math.E, math.Pi - y_index * math.Pi / 128 / math.pow(2, zoom))) - math.Pi / 4) * 2)
//      println("lat " + lat  + " lon " + lon)
      Location(lat, lon)
    }
    val TILE_SIZE = 256

    val locationArray = new Array[Location](TILE_SIZE * TILE_SIZE)

    for (lat <- 0 until TILE_SIZE; lon <- 0 until TILE_SIZE) {
      locationArray(lat * TILE_SIZE + lon) = transform(x * 256 + lon, y * 256 + lat)
    }

    val pixelArray = locationArray
      .par.map(x => Visualization.predictTemperature(temperatures, x))
      .map(x => Visualization.interpolateColor(colors, x))
      .map(x => Pixel(x.red, x.green, x.blue, 127))
      .toArray

    Image(TILE_SIZE, TILE_SIZE, pixelArray, 2)
  }

  /**
    * Generates all the tiles for zoom levels 0 to 3 (included), for all the given years.
    *
    * @param yearlyData    Sequence of (year, data), where `data` is some data associated with
    *                      `year`. The type of `data` can be anything.
    * @param generateImage Function that generates an image given a year, a zoom level, the x and
    *                      y coordinates of the tile and the data to build the image from
    */
  def generateTiles[Data](
                           yearlyData: Iterable[(Int, Data)],
                           generateImage: (Int, Int, Int, Int, Data) => Unit
                         ): Unit = {
    yearlyData.par.foreach(d => {
      val year = d._1
      val data = d._2

      for (zoom <- 0 to 3) {
        val level = math.pow(2, zoom).toInt
        for (x <- 0 until level; y <- 0 until level)
          generateImage(year, zoom, x, y, data)
      }
    })
  }


}
