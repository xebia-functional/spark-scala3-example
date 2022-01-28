package helloworld

import org.apache.spark.sql.SparkSession

object HelloWorld:

  @main def run =
    val spark = SparkSession.builder
      .appName("HelloWorld")
      .master(sys.env.getOrElse("SPARK_MASTER_URL", "local[*]"))
      .getOrCreate()
    import spark.implicits._

    val df = List("hello", "world").toDF
    df.show()

    spark.stop
