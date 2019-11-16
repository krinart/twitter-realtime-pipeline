package flink_pipeline.pubsub

import java.time.{Instant, ZoneId, ZonedDateTime}

import com.google.cloud.pubsub.v1.{AckReplyConsumer, MessageReceiver, Subscriber}
import com.google.pubsub.v1.{ProjectSubscriptionName, PubsubMessage}

object Receive extends App { // use the default project id
  private val PROJECT_ID = "pragmatic-zoo-253123"

  class MessageReceiverExample extends MessageReceiver {
    override def receiveMessage(message: PubsubMessage, consumer: AckReplyConsumer): Unit = {

      val publishedTS = message.getPublishTime
      val time: ZonedDateTime = Instant
        .ofEpochSecond(publishedTS.getSeconds, publishedTS.getNanos())
        .atZone(ZoneId.of("America/Los_Angeles"))


      println("Message Id: " + message.getMessageId + " Date: " + time + " Data: " + message.getData.toStringUtf8)
      // Ack only after all work for the message is complete.
      consumer.ack()
    }
  }

  val subscriptionId = "twitter-source-sentiment-analysis-pipeline"
//  val subscriptionId = "twitter-source-geo-aggregation-pipeline"
//  val subscriptionId = "twitter-aggregate-events-ui"
  val subscriptionName = ProjectSubscriptionName.of(PROJECT_ID, subscriptionId)
  var subscriber: Subscriber = null
  try { // create a subscriber bound to the asynchronous message receiver
    subscriber = Subscriber.newBuilder(subscriptionName, new MessageReceiverExample).build
    subscriber.startAsync.awaitRunning()
    // Allow the subscriber to run indefinitely unless an unrecoverable error occurs.
    subscriber.awaitTerminated()
  } catch {
    case e: IllegalStateException =>
      System.out.println("Subscriber unexpectedly stopped: " + e)
  }
}

