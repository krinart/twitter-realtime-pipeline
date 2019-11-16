FROM flink:latest
RUN mkdir /jobs
ADD /flink-pipeline-entrypoint.sh /jobs/
RUN chmod +x /jobs/flink-pipeline-entrypoint.sh

ADD /flink_pipeline-assembly-0.1.jar /jobs/
ENTRYPOINT ["/jobs/flink-pipeline-entrypoint.sh"]

