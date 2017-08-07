package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import org.apache.spark.sql.DataFrame

object Visualization_SparkSQL {

  /**
    *
    * @param temperatures schema: (Location(), temp)
    * @param colors
    * @return
    */
  def visualize(temperatures: DataFrame, colors: Iterable[(Double, Color)]): Image = {
    val Lat_MAX = 180
    val Lon_MAX = 360

    val latRange = Range(90, -90, -1)
    val lonRange = Range(-180, 180, 1)

    val pixelArrays = Array.ofDim[Pixel](latRange.size, lonRange.size)

    for(x <- latRange; y <- lonRange) {
      pixelArrays(x)(y) =
    }
  }
}
