#!/bin/bash
FLINK_CMD="flink run -d -m flink-jobmanager:8081 -c $FLINK_APP_MAIN_CLASS /jobs/flink_pipeline-assembly-0.1.jar"
echo $FLINK_CMD
exec $FLINK_CMD

