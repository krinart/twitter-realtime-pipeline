package flink_pipeline

import java.util.Properties

import flink_pipeline.model._
import flink_pipeline.utils.pipelines._
import io.circe.syntax._
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.time.Time
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer010, FlinkKafkaProducer010}  // do not delete

object TwitterSentimentAnalysisAppKafka {

  def main(args: Array[String]): Unit = {
    val params: ParameterTool = ParameterTool.fromArgs(args)

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.enableCheckpointing(Time.seconds(params.getInt("ct", 5)).toMilliseconds)

    // Sources
    val input = env
//      .fromCollection(loadTweetsRaw())
      .addSource(getKafkaConsumer())

    // Actual pipeline
    val pipeline = input
      .tweetsSentimentAnalysis()

    // Sinks
    pipeline
      .addSink(getKafkaSink())

    pipeline
      .map(_.asJson.noSpaces)
      .print()

    env.execute("Twitter Sentiment Analysis App Kafka")
  }

  def getKafkaConsumer(): FlinkKafkaConsumer010[String] = {
    val props = new Properties()
    props.setProperty("bootstrap.servers", "my-kafka-headless:9092")
    props.setProperty("group.id", "twitter-sentiment-analysis")

    val cons = new FlinkKafkaConsumer010(
      "twitter-source",
      new SimpleStringSchema(),
      props,
    )
    cons.setStartFromEarliest()
    cons
  }

  def getKafkaSink(): FlinkKafkaProducer010[Tweet] =
    new FlinkKafkaProducer010[Tweet](
      "my-kafka-headless:9092",
      "twitter-sentiment-analysis",
      new TweetSerializationSchema)
}

