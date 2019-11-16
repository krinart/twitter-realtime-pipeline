package flink_pipeline.utils

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import java.util.Locale

import flink_pipeline.model._
import flink_pipeline.{ScoreTweetMap, Tweet}
import io.circe.generic.semiauto.deriveEncoder
import io.circe.parser.decode
import io.circe.{Encoder, Json, ObjectEncoder}
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector

// Represents results of computation.
case class Coordinates(lon: Double, lat: Double)

case class AggregateEvent(created: Instant, coordinates: Coordinates, numberOfEvents: Int)

object AggregateEvent {

  val formatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd HH:mm:ss.SSS zzz")
    .withLocale(Locale.UK)
    .withZone(ZoneId.of("UTC"))

  implicit val encodeInstant: Encoder[Instant] = (a: Instant) => Json.fromString(formatter.format(a))
  implicit val coordinatesEncoder: ObjectEncoder[Coordinates] = deriveEncoder[Coordinates]
  implicit val outgoingEventEncoder: ObjectEncoder[AggregateEvent] = deriveEncoder[AggregateEvent]
}

object pipelines {

  implicit class RichLocationStream(stream: DataStream[(Double, Double)]) {
    def locationAggregation(windowSize: Time = Time.seconds(5)): DataStream[AggregateEvent] =
      pipelines.locationAggregation(stream, windowSize)
  }

  implicit class RichStringStream(stream: DataStream[String]) {
    def tweetsSentimentAnalysis(): DataStream[Tweet] =
      pipelines.tweetsSentimentAnalysis(stream)
  }

  def locationAggregation(stream: DataStream[(Double, Double)], windowSize: Time = Time.seconds(5)): DataStream[AggregateEvent] =
    stream
      // Translate every coordinate into cell to group them together
      .map { el => (GeoUtils.coordinatesToCell(el._1, el._2), 1) }
      // Key all entries by cell
      .keyBy(_._1)
      // Apply window
      .timeWindow(windowSize)
      // Calculate sum of 1s - which will give us total number of entries
      .sum(1)
      // Build OutgoingEvent for every record
      .process((value: ((Int, Int), Int), ctx: ProcessFunction[((Int, Int), Int), AggregateEvent]#Context, out: Collector[AggregateEvent]) => {
        val coord = GeoUtils.cellToCoordinates(value._1)
        out.collect(
          AggregateEvent(
            Instant.ofEpochMilli(ctx.timestamp()),
            Coordinates(coord._1, coord._2),
            value._2
          ))
      })

  def tweetsSentimentAnalysis(stream: DataStream[String]): DataStream[Tweet] = {
    stream
      .map(decode[Tweet](_).toTry)
      .filter(_.isSuccess)
      .map(_.get)
      .map(new ScoreTweetMap)
  }
}

