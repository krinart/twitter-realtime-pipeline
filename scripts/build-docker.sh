echo "--> Building Docker images..."
cd docker/flink-pipeline
docker build -t flink_pipeline .
docker tag flink_pipeline krinart/flink_pipeline
docker push krinart/flink_pipeline

cd ../twitter-source/
docker build -t twitter_source .
docker tag twitter_source krinart/twitter_source
docker push krinart/twitter_source

cd ../ui-app/
rm -rfd ui/
cp -r ../../ui .
docker build -t ui_app .
docker tag ui_app krinart/ui_app
docker push krinart/ui_app

cd ../kafka-connect
if [ ! -f kafka-connect-cassandra-1.2.3-2.1.0-all.jar ]; then
    if [ ! -f kafka-connect-cassandra-1.2.3-2.1.0-all.tar.gz ]; then
      wget https://github.com/lensesio/stream-reactor/releases/download/1.2.3/kafka-connect-cassandra-1.2.3-2.1.0-all.tar.gz
    fi
    tar xzvf kafka-connect-cassandra-1.2.3-2.1.0-all.tar.gz
fi
docker build -t kafka-connect .
docker tag kafka-connect krinart/kafka-connect
docker push krinart/kafka-connect

cd ../busybox
rm -rfd kafka-connect-sinks
cp -r ../../k8s/kafka-connect-sinks .
docker build -t bbox .
docker tag bbox krinart/bbox
docker push krinart/bbox

