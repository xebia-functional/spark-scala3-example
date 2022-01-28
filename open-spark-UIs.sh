#!/bin/sh

# the master node Spark UI
open http://localhost:8080

# when you submit a job, the detailed Spark UI for that job will appear here while the job is running
open http://localhost:4040

# workers
open http://$(docker port spark-scala3-example-spark-worker-1 8080)
open http://$(docker port spark-scala3-example-spark-worker-2 8080)
