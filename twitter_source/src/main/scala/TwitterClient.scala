import java.util.concurrent.LinkedBlockingQueue

import collection.JavaConverters._
import com.twitter.hbc.core.endpoint.{Location, StatusesFilterEndpoint}
import com.twitter.hbc.httpclient.auth.OAuth1
import com.twitter.hbc.ClientBuilder
import com.twitter.hbc.core.Constants
import com.twitter.hbc.core.processor.StringDelimitedProcessor
import com.twitter.hbc.httpclient.BasicClient

case class TwitterAuth(
  consumerKey: String,
  consumerSecret: String,
  accessToken: String,
  accessTokenSecret: String,
)

class TwitterClient(keywords: Seq[String], auth: TwitterAuth) {

  val queue = new LinkedBlockingQueue[String](100000)
  val endPoint = new StatusesFilterEndpoint()
//  endPoint.trackTerms(keywords.asJava)
  endPoint.addPostParameter("lang","en")
  val southwest = new Location.Coordinate(-170.542649, -70)
  val northeast = new Location.Coordinate(170, 70)
  val location = new Location(southwest, northeast)
  endPoint.locations(List(location).asJava)

  val client: BasicClient = new ClientBuilder()
    .name("twitter")
    .hosts(Constants.STREAM_HOST)
    .endpoint(endPoint)
    .authentication(new OAuth1(auth.consumerKey, auth.consumerSecret, auth.accessToken, auth.accessTokenSecret))
    .processor(new StringDelimitedProcessor(queue))
    .build()

  def start(): Unit =
    client.connect()

  def readTweet(): String =
    queue.take()
}
