package flink_pipeline

import java.io.InputStream
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId, ZonedDateTime}
import java.util.Locale

import cats.implicits._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.parser.decode
import io.circe.{Decoder, Encoder, Json}

// This is Twitter's format.
case class BoundingBox(coordinates: List[List[(Double, Double)]])
case class RawPlace(bounding_box: BoundingBox) {
  def c(i: Int): (Double, Double) = bounding_box.coordinates.head(i)
}

// This is our internal format.
case class Coord(lon: Double, lat: Double)
case class Place(p1: Coord, p2: Coord, p3: Coord, p4: Coord) {
  def average(seq: Seq[Double]): Double =
    seq.foldLeft((0.0, 1)) { case ((avg, idx), next) => (avg + (next - avg) / idx, idx + 1) }._1
  def lon: Double = average(List(p1.lon, p2.lon, p3.lon, p4.lon))
  def lat: Double = average(List(p1.lat, p2.lat, p3.lat, p4.lat))
}

case class Tweet(
  id: Long,
  created_at: Instant,
  text: String,
  place: Option[Place],
  sentiment: Option[String],
  words: Option[Seq[String]],
)

object model {

  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy")

  def parseInstant(str: String): Either[String, Instant] = {
    Either.catchNonFatal(ZonedDateTime.parse(str, formatter).toInstant).leftMap(_ => "invalid")
  }

  implicit val instantDecoder: Decoder[Instant] = Decoder.decodeString.emap(parseInstant)

  implicit val bbDecoder: Decoder[BoundingBox] = deriveDecoder[BoundingBox]
  implicit val rawPlaceDecoder: Decoder[RawPlace] = deriveDecoder[RawPlace]

  def c(c: (Double, Double)): Coord = Coord(c._1, c._2)
  def parsePlace(obj: Json): Either[String, Place] = rawPlaceDecoder.decodeJson(obj) match {
      case Left(l) => Left(l.toString)
      case Right(r) => Right(Place(c(r.c(0)), c(r.c(0)), c(r.c(0)), c(r.c(0))))
    }
  implicit val placeDecoder: Decoder[Place] = Decoder.decodeJson.emap(parsePlace)
  implicit val tweetDecoder: Decoder[Tweet] = deriveDecoder[Tweet]

  val formatter2: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS zzz")
    .withLocale(Locale.UK)
    .withZone(ZoneId.of("UTC"))
  implicit val encodeInstant: Encoder[Instant] = (a: Instant) => Json.fromString(formatter2.format(a))
  implicit val coordEncoder: Encoder[Coord] = deriveEncoder[Coord]
  implicit val placeEncoder: Encoder[Place] = deriveEncoder[Place]
  implicit val tweetEncoder: Encoder[Tweet] = deriveEncoder[Tweet]


  def loadTweetsRaw(): List[String] = {
    val stream: InputStream = getClass.getResourceAsStream("/tweets_raw.json")
    scala.io.Source.fromInputStream(stream).getLines.toList
  }

  def loadTweets(): List[Tweet] = {
    val stream: InputStream = getClass.getResourceAsStream("/tweets.json")
    val lines: String = scala.io.Source.fromInputStream(stream).getLines.mkString("\n")

    decode[List[Tweet]](lines) match {
      case Left(l) => throw new Error(s"Failed to produce twits: $l")
      case Right(tweets) => tweets
    }
  }
}

object Place extends App {
  import model._
  val tweet = decode[Tweet]("{\"created_at\":\"Thu Sep 26 02:30:34 +0000 2019\",\"id\":1177047796538474496,\"text\":\"foo\",\"place\":{\"bounding_box\":{\"coordinates\":[[[-118.668404,33.704538],[-118.668404,34.337041],[-118.155409,34.337041],[-118.155409,33.704538]]]}}}")
  import io.circe.syntax._
  val t = tweet.toOption.get.copy(sentiment = Some("positive"), words = Some(List("1", "2", "3")))
  println(t.asJson.noSpaces)
  // {"id":1177047796538474496,"created_at":"2019-09-26 02:30:34.000 UTC","text":"foo","place":{"p1":{"lon":-118.668404,"lat":33.704538},"p2":{"lon":-118.668404,"lat":33.704538},"p3":{"lon":-118.668404,"lat":33.704538},"p4":{"lon":-118.668404,"lat":33.704538}},"sentiment":"positive","words":["1","2","3"]}
}

