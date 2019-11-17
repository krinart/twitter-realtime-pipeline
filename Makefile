buildj:
	scripts/build-jars.sh

buildd:
	scripts/build-docker.sh

build: buildj buildd

cluster:
	gcloud beta container --project "pragmatic-zoo-253123" clusters create "standard-cluster-1" --zone "us-central1-a" --no-enable-basic-auth --cluster-version "1.13.11-gke.14" --machine-type "n1-standard-4" --image-type "COS" --disk-type "pd-standard" --disk-size "100" --scopes "https://www.googleapis.com/auth/devstorage.read_only","https://www.googleapis.com/auth/logging.write","https://www.googleapis.com/auth/monitoring","https://www.googleapis.com/auth/servicecontrol","https://www.googleapis.com/auth/service.management.readonly","https://www.googleapis.com/auth/trace.append" --num-nodes "3" --enable-cloud-logging --enable-cloud-monitoring --enable-ip-alias --network "projects/pragmatic-zoo-253123/global/networks/default" --subnetwork "projects/pragmatic-zoo-253123/regions/us-central1/subnetworks/default" --default-max-pods-per-node "110" --addons HorizontalPodAutoscaling,HttpLoadBalancing --enable-autoupgrade --enable-autorepair
	gcloud container clusters get-credentials standard-cluster-1 --zone us-central1-a --project pragmatic-zoo-253123

helm:
	scripts/init-helm.sh

secret:
	kubectl create secret generic twitter-auth --from-file=TOKEN=k8s/.env/TOKEN --from-file=TOKEN_SECRET=k8s/.env/TOKEN_SECRET --from-file=CONSUMER_KEY=k8s/.env/CONSUMER_KEY --from-file=k8s/.env/CONSUMER_SECRET

kafka:
	helm install --name my-kafka incubator/kafka

cassandra:
	helm install --name cassandra incubator/cassandra

clients:
	kubectl apply -f k8s/infra/kafka-client.yaml
	kubectl apply -f k8s/infra/cassandra-client.yaml

flink:
	kubectl apply -f k8s/infra/flink.yaml

es:
	kubectl apply -f k8s/infra/es.yaml
	kubectl apply -f k8s/infra/kibana.yaml

kafka-connect:
	kubectl apply -f k8s/infra/kafka-connect.yaml

cassandrat:
	echo "CREATE KEYSPACE "ks" WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1}; exit" | kubectl exec -i cassandra-client cqlsh cassandra
	echo "CREATE TABLE ks.twitter_sentiment_analysis (id bigint PRIMARY KEY, created_at text, text text, place frozen<map<text, map<text, double>>>, sentiment text, words frozen<list<text>>); exit" | kubectl exec -i cassandra-client cqlsh cassandra

initialize: helm secret

infra: kafka cassandra clients flink es

start: cassandrat kafka-connect
	kubectl apply -f k8s/twitter-source-job-kafka.yaml
	kubectl apply -f k8s/flink-twitter-geo-app.yaml
	kubectl apply -f k8s/flink-twitter-sentiment-analysis-app-kafka.yaml
	kubectl apply -f k8s/ui-app.yaml

