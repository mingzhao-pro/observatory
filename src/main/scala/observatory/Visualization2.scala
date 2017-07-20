package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.Interaction.tileLocation

/**
  * 5th milestone: value-added information visualization
  */
object Visualization2 {

  /**
    * @param x   X coordinate between 0 and 1
    * @param y   Y coordinate between 0 and 1
    * @param d00 Top-left value
    * @param d01 Bottom-left value
    * @param d10 Top-right value
    * @param d11 Bottom-right value
    * @return A guess of the value at (x, y) based on the four known values, using bilinear interpolation
    *         See https://en.wikipedia.org/wiki/Bilinear_interpolation#Unit_Square
    */
  def bilinearInterpolation(
                             x: Double, // lon
                             y: Double, // lat
                             d00: Double,
                             d01: Double,
                             d10: Double,
                             d11: Double
                           ): Double = {
    val y0 = (1 - x) * d00 + x * d10
    val y1 = (1 - x) * d01 + x * d11

    (1 - y) * y0 + y * y1
  }

  /**
    * @param grid   Grid to visualize
    * @param colors Color scale to use
    * @param zoom   Zoom level of the tile to visualize
    * @param x      X value of the tile to visualize
    * @param y      Y value of the tile to visualize
    * @return The image of the tile at (x, y, zoom) showing the grid using the given color scale
    */
  def visualizeGrid(
                     grid: (Int, Int) => Double,
                     colors: Iterable[(Double, Color)],
                     zoom: Int,
                     x: Int,
                     y: Int
                   ): Image = {

    def transform(x_index: Int, y_index: Int) = {
      val lon = math.toDegrees(x_index * math.Pi / 128 / math.pow(2, zoom) - math.Pi)
      val lat = math.toDegrees((math.atan(math.pow(math.E, math.Pi - y_index * math.Pi / 128 / math.pow(2, zoom))) - math.Pi / 4) * 2)
      //      println("lat " + lat  + " lon " + lon)
      Location(lat, lon)
    }

    val TILE_SIZE = 256
    val locationArray = new Array[Double](TILE_SIZE * TILE_SIZE)

    for (lat <- 0 until TILE_SIZE; lon <- 0 until TILE_SIZE) {
      val location = transform(x * 256 + lon, y * 256 + lat)
      val lat0 = math.floor(location.lat).toInt
      val lon0 = math.ceil(location.lon).toInt

      val x0 = math.abs(location.lon - lon0).toInt
      val y0 = math.abs(location.lat - lat0).toInt
      val temp00 = grid(lat0, lon0) // top-left
      val temp10 = grid(lat0, lon0 + 1) // top-right
      val temp01 = grid(lat0 - 1, lon0) // bottom-left
      val temp11 = grid(lat0 - 1, lon0 + 1) // bottom-right

      locationArray(lat * TILE_SIZE + lon) = bilinearInterpolation(x0, y0, temp00, temp01, temp10, temp11)
    }

    val pixelArray = locationArray.par
      .map(x => Visualization.interpolateColor(colors, x))
      .map(x => Pixel(x.red, x.green, x.blue, 127))
      .toArray

    Image(TILE_SIZE, TILE_SIZE, pixelArray, 2)
  }
}
































