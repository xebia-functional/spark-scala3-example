package tsp

import pureconfig._

case class Config(cities: List[City])

object Config:

  // we can't use PureConfig's generic derivation because it uses Scala 2 macros
  given (using cityReader: ConfigReader[City]): ConfigReader[Config] =
    ConfigReader.forProduct1[Config, List[City]]("cities")(Config.apply)
