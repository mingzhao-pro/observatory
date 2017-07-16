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
                             x: Double,
                             y: Double,
                             d00: Double,
                             d01: Double,
                             d10: Double,
                             d11: Double
                           ): Double = {
    val x_y1 = (1 - x) * d00 + x * d10
    val x_y2 = (1 - x) * d01 + x * d11

    (1 - y) * x_y1 + y * x_y2
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
    val TILE_SIZE = 256
    import org.apache.commons.math3.util.FastMath._
    val zoomRatio = pow(2, zoom)
    val LatStep = 170.1022 / zoomRatio / TILE_SIZE
    val LonStep = 360 / zoomRatio / TILE_SIZE

    val locationArray = new Array[Location](TILE_SIZE * TILE_SIZE)
    val topLeftLocation = tileLocation(zoom, x, y)

    for (lat <- 0 until TILE_SIZE; lon <- 0 until TILE_SIZE) {
      locationArray(lat * TILE_SIZE + lon) =
        Location(topLeftLocation.lat - LatStep * lat, topLeftLocation.lon + LonStep * lon)
    }

    val pixelArray =
      locationArray.map(location => (location.lat, location.lon))
        .map(x => (x._1, x._2, math.floor(x._1), math.ceil(x._1), math.floor(x._2), math.ceil(x._2)))        //x0,x1,y0,y1,
        .map(x => bilinearInterpolation(x._1,x._2,x._3,x._4,x._5,x._6))
        .map(Visualization.interpolateColor(colors, _))
        .map(x => Pixel(x.red, x.green, x.blue, 127))

    Image(TILE_SIZE, TILE_SIZE, pixelArray, 2)
  }

}
































