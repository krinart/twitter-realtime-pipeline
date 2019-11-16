package flink_pipeline.pubsub

import java.util

import com.google.api.core.{ApiFuture, ApiFutures}
import com.google.api.gax.batching.BatchingSettings
import com.google.cloud.pubsub.v1.Publisher
import com.google.protobuf.ByteString
import com.google.pubsub.v1.{ProjectTopicName, PubsubMessage}
import flink_pipeline.model._
import io.circe.syntax._
import org.threeten.bp.Duration


object PublishEvents extends App {
  // use the default project id// use the default project id
  val PROJECT_ID = "pragmatic-zoo-253123"

  def time(a: => Unit): Unit = {
    val s = System.currentTimeMillis()
    a
    println(s"time: ${System.currentTimeMillis() - s}")
  }

  val topicId = "twitter-source"
  val topicName = ProjectTopicName.of(PROJECT_ID, topicId)
  var publisher: Publisher = null
  val futures = new util.ArrayList[ApiFuture[String]]

  val tweets = loadTweets()

  val batchingSettings: BatchingSettings =
    BatchingSettings.newBuilder()
      .setElementCountThreshold(1L)
      .setRequestByteThreshold(50000L)
      .setDelayThreshold(Duration.ofMillis(1000))
      .build();

  {

    try { // Create a publisher instance with default settings bound to the topic
      publisher = Publisher.newBuilder(topicName).setBatchingSettings(batchingSettings).build

      var i = 0

      while (true) {
        for (event <- tweets) {
          val data: ByteString = ByteString.copyFromUtf8(event.asJson.noSpaces)
          val pubsubMessage = PubsubMessage.newBuilder.setData(data).build
          val future = publisher.publish(pubsubMessage)

          val l = new util.ArrayList[ApiFuture[String]]
          l.add(future)

          Thread.sleep(10)

          //          val messageIds = ApiFutures.allAsList(l).get
          //        for (messageId <- messageIds) {
          //          println(messageId)
          //        }

          println(i)
          i += 1

          futures.add(future)
        }


      }
    } finally {
      // Wait on any pending requests
      val messageIds = ApiFutures.allAsList(futures).get
      //    for (messageId <- messageIds) {
      //      println(messageId)
      //    }
      if (publisher != null) { // When finished with the publisher, shutdown to free up resources.
        publisher.shutdown()
      }
    }
  }
}
