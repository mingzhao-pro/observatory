package observatory

import java.time.LocalDate

import org.apache.spark.sql.SparkSession

import scala.io.Source

/**
  * 1st milestone: data extraction
  */
object Extraction {

//  val spark = SparkSession.builder.appName("Coursera Project").getOrCreate

  // For implicit conversions like converting RDDs to DataFrames
//  import spark.implicits._
  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Int, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Double)] = {
    ???
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Double)]): Iterable[(Location, Double)] = {
    val locTemGroup = records.map(x => (x._2, x._3, 1)).groupBy(_._1)
    val sumTemp = locTemGroup.mapValues(_.reduce((a, b)=>(a._1, a._2 + b._2, a._3 + b._3)))
    sumTemp.map(x => (x._1, math.round((x._2._2 / x._2._3) * 10.0) / 10.0)).toIterator.toIterable
  }
}
