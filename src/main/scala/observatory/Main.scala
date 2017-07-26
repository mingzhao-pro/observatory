package observatory

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._
import org.slf4j.LoggerFactory

object Main extends App {

  case class Person(name: String, age: Long)

  val logger = LoggerFactory.getLogger(Main.getClass)
  val filePath = "src/main/resources/"

  val year = 1975


  // Inner usage methods
  private def fToC(f: Double) = math.round((f - 32) * 5 / 9 * 10) / 10.0
  private def predicte(s: Station) = s.lat != null && s.lon != null

  // Spark Context creation
  val conf = new SparkConf().setMaster("local[8]").setAppName("widipedia")
  val spark = SparkSession.builder().config(conf).getOrCreate
  import spark.implicits._

  // Case classes and encoders
  case class Station(stn: String, wban: String, lat: Integer, lon: Integer)
  case class Temperature(stn: String, wban: String, month: Integer, day: Integer, temperature: Double)
  val stationDSEncoder = Seq(Station("", "", 0, 0)).toDS
  val tempDSEncoder = Seq(Temperature("", "", 0, 0, 0)).toDS

  // Creation of stationDS and temperatureDS
  val stationDS = spark.read.option("header", "true").csv(filePath + "stations.csv")
    .withColumn("lat", 'lat.cast(IntegerType))
    .withColumn("lon", 'lon.cast(IntegerType))
    .as[Station]
  println(stationDS.count)
  val validStations =  stationDS.filter(predicte _)
  println(validStations.count)

  val temperatureDS = spark.read.option("header", "true").csv(filePath + year + ".csv")
    .withColumn("month", 'month.cast(IntegerType))
      .withColumn("day", 'day.cast(IntegerType))
    .withColumn("temp", 'temp.cast(DoubleType))
    .as[Temperature]
  println(temperatureDS.count)

//  stationDS.filter('lat.isNotNull || 'lon.isNotNull)
//  println(stationDS.count)
  //  // Create station data frame
  //  val stationRDD = spark.sparkContext.textFile(filePath + "stations.csv")
  //    .map(_.split(","))
  //    .filter(_.length == 4)
  //    .map(attr => Row(attr(0), attr(1), attr(2).toDouble, attr(3).toDouble))
  //  val fields = Seq(fieldSTN, fieldWBAN, fieldLAT, fieldLON)
  //  val schema = StructType(fields)
  //  val stationDF = spark.createDataFrame(stationRDD, schema)
  //  stationDF.createOrReplaceTempView("stations")
  //
  //  // Create temperature data frame
  //  val tempRDD = spark.sparkContext.textFile(filePath + year + ".csv")
  //    .map(_.split(","))
  //    .filter(_.last != "9999.9")
  //    .map(attr => Row(attr(0), attr(1), attr(2).toInt, attr(3).toInt, attr(4).toDouble))
  //  val fields_t = Seq(fieldSTN, fieldWBAN, fieldMonth, fieldDay, fieldTemp)
  //  val schema_t = StructType(fields_t)
  //  val tempDF = spark.createDataFrame(tempRDD, schema_t)
  //  tempDF.createOrReplaceTempView("temperatures")
  //
  //  val infoLinesDF = spark.sql("SELECT month as month, day as day, temp as temp, lat as lat, " +
  //    "lon as lon FROM temperatures, stations " +
  //    "WHERE temperatures.stn = stations.stn AND temperatures.wban = stations.wban")
  //  println(infoLinesDF.count)//2177190
  //
  //  val localTemps = infoLinesDF.map(attr => (LocalDate.of(year, attr.getAs[Int]("month"),
  //    attr.getAs[Int]("day")),
  //    Location(attr.getAs[Double]("lat"), attr.getAs[Double]("lon")), fToC(attr.getAs[Double]("temp"))))
  //  println(localTemps.count)

}
