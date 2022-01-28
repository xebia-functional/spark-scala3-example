#!/bin/sh

# Create the directory where we will put the jar we want to submit to the cluster
mkdir -p ./spark-apps

# Start a master node and 2 workers
docker compose up -d --scale spark-worker=2
