package flink_pipeline

import java.util.Properties

import flink_pipeline.model._
import flink_pipeline.utils.pipelines._
import flink_pipeline.utils.{AggregateEvent, BoundedOutOfOrdernessGenerator}
import io.circe.parser.decode
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.time.Time
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer010, FlinkKafkaProducer010}

object TwitterGeoApp {
  def main(args: Array[String]): Unit = {
    val params: ParameterTool = ParameterTool.fromArgs(args)

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.enableCheckpointing(Time.seconds(params.getInt("ct", 5)).toMilliseconds)

    def getCoordinate(tweet: Tweet): (Double, Double) = {
      val p = tweet.place.get
      (p.lon, p.lat)
    }

    val source = env
//      .fromCollection(loadTweetsRaw())
      .addSource(getKafkaConsumer())

    val pipeline = source
      .map(decode[Tweet](_).toTry)
      .filter(_.isSuccess)
      .map(_.get)
      .assignTimestampsAndWatermarks(new BoundedOutOfOrdernessGenerator[Tweet](3500) {
        override def elementTimestamp(element: Tweet): Long = element.created_at.toEpochMilli
      })
      .filter(_.place.isDefined)
      .map(getCoordinate _)
      .locationAggregation()

    pipeline
      .print()

    pipeline
      .addSink(getKafkaSink())

    env.execute("Twitter Geo App")
  }

  def getKafkaConsumer(): FlinkKafkaConsumer010[String] = {
    val props = new Properties()
    props.setProperty("bootstrap.servers", "my-kafka-headless:9092")
    props.setProperty("group.id", "twitter-geo-app")

    val cons = new FlinkKafkaConsumer010(
      "twitter-source",
      new SimpleStringSchema(),
      props,
    )
    cons.setStartFromEarliest()
    cons
  }

  def getKafkaSink(): FlinkKafkaProducer010[AggregateEvent] =
    new FlinkKafkaProducer010[AggregateEvent](
      "my-kafka-headless:9092",
      "twitter-geo-app",
      new OutgoingEventSerializationSchema)
}

