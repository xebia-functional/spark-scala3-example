package tsp

import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions._
import org.apache.spark.sql.api.java.UDF4
import org.apache.spark.sql.types.DataTypes
import org.apache.spark.sql.expressions.scalalang.typed
import pureconfig._
import scala3encoders.given
import java.lang.{Double => JDouble}

object TravellingSalesman:

  case class Journey(id: String, cities: List[City])

  /** A stop at a given city as part of a given journey */
  case class JourneyStop(journeyID: String, index: Int, city: City)

  /**
    * A leg of a journey, from one city to the next.
    * For the first leg of the journey, previousCity will be null.
    */
  case class JourneyLeg(journeyID: String, index: Int, city: City, previousCity: City)

  /** The distance in km of a given journey leg */
  case class JourneyLegDistance(journeyID: String, index: Int, distance: Double)

  // Load the cities from the configuration file (src/main/resources/application.conf)
  val config: Config = ConfigSource.default.loadOrThrow[Config]

  // The number of cities we want to visit
  val N = 10

  val cities: List[City] = config.cities.take(N)

  // Arbitrarily pick the first city as the start/end point of the journey
  val (home, stops) = (cities.head, cities.tail)

  // Enumerate every possible round-trip through the N cities.
  // This results in (N-1)! journeys, so very much brute force.
  val journeys: List[Journey] = stops.permutations.toList.map { xs =>
    val loop = home +: xs :+ home // start at home, visit all the other cities and return home
    val id = loop.map(_.name).mkString("->")
    Journey(id, loop)
  }

  // I wanted to lift journeys into a DataFrame and do everything in Spark,
  // but ran into https://github.com/vincenzobaz/spark-scala3/issues/14.
  // Worked around by doing the explosion of journeys into journey stops in plain Scala.
  val journeyStops: List[JourneyStop] =
    for
      journey <- journeys
      (city, i) <- journey.cities.zipWithIndex
    yield
      JourneyStop(journey.id, i, city)

  @main def run =
    val spark = SparkSession.builder
      .appName("TravellingSalesman")
      .master(sys.env.getOrElse("SPARK_MASTER_URL", "local[*]"))
      .getOrCreate()
    import spark.implicits._

    // Lift the list of journey stops into a Dataset
    val journeyStopsDs: Dataset[JourneyStop] = spark.sparkContext.parallelize(journeyStops, 50).toDS

    // Convert to journey legs, i.e. pairs of cities
    val journeyLegs: Dataset[JourneyLeg] = journeyStopsDs
      .withColumn("previousCity", lag("city", 1).over(Window.partitionBy("journeyID").orderBy("index")))
      .as[JourneyLeg]

    // Convert the Haversine distance function into a UDF.
    // Had to use the Java API here because the usual `udf(<scala closure goes here>` method
    // relies on TypeTag, which doesn't work in Scala 3.
    val haversineJavaUDF: UDF4[JDouble, JDouble, JDouble, JDouble, JDouble] = new UDF4[JDouble, JDouble, JDouble, JDouble, JDouble] {
      def call(lat1: JDouble, lon1: JDouble, lat2: JDouble, lon2: JDouble): JDouble =
        JDouble.valueOf(Haversine.distance(lat1, lon1, lat2, lon2))
    }
    val haversineUDF: UserDefinedFunction = udf(haversineJavaUDF, DataTypes.DoubleType)

    // Use the UDF to calculate the distances of the journey legs
    val journeyLegDistances: Dataset[JourneyLegDistance] = journeyLegs
      .withColumn(
        "distance",
        when(isnull($"previousCity"), 0.0).otherwise(haversineUDF($"city.lat", $"city.lon", $"previousCity.lat", $"previousCity.lon"))
      )
      .drop("city", "previousCity")
      .as[JourneyLegDistance]

    // A simpler and safer alternative that doesn't require a UDF
    val journeyLegDistancesWithoutUDF: Dataset[JourneyLegDistance] = journeyLegs.map { leg =>
      val distance = Option(leg.previousCity) match {
        case Some(City(_, prevLat, prevLon)) =>
          Haversine.distance(prevLat, prevLon, leg.city.lat, leg.city.lon)
        case None =>
          0.0
      }
      JourneyLegDistance(leg.journeyID, leg.index, distance)
    }

    // Calculate the total distance of each journey
    val journeyDistances: Dataset[(String, Double)] = journeyLegDistancesWithoutUDF
      .groupByKey(_.journeyID)
      .agg(typed.sum[JourneyLegDistance](_.distance).name("totalDistance"))
      .orderBy($"totalDistance")

    // We've found the shortest journey. Job done!
    val (shortestJourney, shortestDistance) = journeyDistances.take(1).head

    println(f"The shortest journey is $shortestJourney with a total distance of $shortestDistance%.2f km")

    spark.stop
