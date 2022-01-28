package tsp

import pureconfig._

case class City(name: String, lat: Double, lon: Double)

object City:

  // we can't use PureConfig's generic derivation because it uses Scala 2 macros
  given ConfigReader[City] =
    ConfigReader.forProduct3[City, String, Double, Double]("name", "lat", "lon")(City.apply)
