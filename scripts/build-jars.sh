echo "--> Building JAR files..."
sbt flink_pipeline/assembly
sbt twitter_source/assembly
echo "--> Done building JAR files"
