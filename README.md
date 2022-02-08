# Spark + Scala 3 example

This is a companion repo for [this
blogpost](https://www.47deg.com/blog/using-scala-3-with-spark/)

It contains two Spark applications: a hello-world and a Travelling Salesman
Problem solver.

## Running the apps on a Spark cluster in Docker

1. Run `./start-spark-cluster.sh`. This will build the Spark Docker image and
   then start a 3-node Spark cluster using docker-compose.
2. Run `sbt assembly` to build the uber-jar containing both apps.
3. Run `./open-spark-UIs.sh` to open the various Spark UIs in your browser, so
   you can see what's going on.
4. Run either `./run-hello-world.sh` or `./run-travelling-salesman.sh` and
   inspect the terminal output.
