package observatory

/**
  * 4th milestone: value-added information
  */
object Manipulation {

  /**
    * @param temperatures Known temperatures
    * @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
    *         returns the predicted temperature at this location
    */
  def makeGrid(temperatures: Iterable[(Location, Double)]): (Int, Int) => Double = {
    def f(a: Int, b: Int): Double = Visualization.predictTemperature(temperatures, Location(a, b))
    f

  }

  /**
    * @param temperaturess Sequence of known temperatures over the years (each element of the collection
    *                      is a collection of pairs of location and temperature)
    * @return A function that, given a latitude and a longitude, returns the average temperature at this location
    */
  def average(temperaturess: Iterable[Iterable[(Location, Double)]]): (Int, Int) => Double = {
    def f(a: Int, b: Int): Double = {
      val aa = temperaturess.map(x => (makeGrid(x)(a, b), 1)).foldLeft((0d, 0))((acc, input) => (acc._1 + input._1, acc._2 + input._2))
      aa._1 / aa._2
    }
    f

  }

  /**
    * @param temperatures Known temperatures
    * @param normals      A grid containing the “normal” temperatures
    * @return A sequence of grids containing the deviations compared to the normal temperatures
    */
  def deviation(temperatures: Iterable[(Location, Double)], normals: (Int, Int) => Double): (Int, Int) => Double = {
    def f(a: Int, b: Int): Double = {
      val temp = makeGrid(temperatures)(a, b)
      temp - normals(a, b)
    }
    f

  }
}

