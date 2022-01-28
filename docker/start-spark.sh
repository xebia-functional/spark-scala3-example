#!/bin/bash
#
# This script is based on https://github.com/mvillarrealb/docker-spark-cluster/blob/master/start-spark.sh

. "/opt/spark/bin/load-spark-env.sh"

cd /opt/spark/bin

if [ "$SPARK_MODE" == "master" ];
then

export SPARK_MASTER_HOST=`hostname`

./spark-class org.apache.spark.deploy.master.Master \
  --ip $SPARK_MASTER_HOST \
  --port $SPARK_MASTER_PORT \
  --webui-port $SPARK_MASTER_WEBUI_PORT >> $SPARK_MASTER_LOG

elif [ "$SPARK_MODE" == "worker" ];
then

./spark-class org.apache.spark.deploy.worker.Worker \
  --webui-port $SPARK_WORKER_WEBUI_PORT \
  $SPARK_MASTER_URL >> $SPARK_WORKER_LOG

else
    echo "Unsupported Spark mode $SPARK_MODE"
fi
