#!bin/bash
echo "Waiting for connect to launch on kafka-connect-service:8083..."

while ! nc -z kafka-connect-service 8083; do
  sleep 1
done

createSink() {
  res=$(curl -X POST "http://kafka-connect-service:8083/connectors" -H "Content-Type: application/json" -d @$1 -w "%{http_code}" -s -o /dev/null)
  while [[ $res = "404" ]]
  do
    echo "request"
    sleep 1
    res=$(curl -X POST "http://kafka-connect-service:8083/connectors" -H "Content-Type: application/json" -d @$1 -w "%{http_code}" -s -o /dev/null)
  done
}

createSink cassandra.json
createSink elasticsearch.json

