FROM ubuntu:latest
RUN apt-get update -y
RUN apt-get install -y netcat curl
ADD /busybox-entry.sh /busybox-entry.sh
ADD /kafka-connect-sinks /
RUN chmod +x busybox-entry.sh
ENTRYPOINT ["/busybox-entry.sh"]

