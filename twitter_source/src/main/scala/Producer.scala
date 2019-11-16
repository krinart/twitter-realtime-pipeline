import java.util.Properties
import java.util.concurrent.Future

import com.google.cloud.pubsub.v1.Publisher
import com.google.protobuf.ByteString
import com.google.pubsub.v1.{ProjectTopicName, PubsubMessage}
import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata, KafkaProducer => KProducer}

trait Producer[A] {
  def processMessage(message: String): Future[A]
}

class KafkaProducer(topic: String, brokers: String) extends Producer[RecordMetadata] {
  val producer = new KProducer[String, String](getProps(brokers))

  override def processMessage(message: String): Future[RecordMetadata] = {
    val record = new ProducerRecord[String, String] (topic, message)
    val f = producer.send(record)
    producer.flush()  // TODO: remove it
    f
  }

  def getProps(brokers: String): Properties = {
    val properties = new Properties()
    properties.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    properties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    properties.setProperty("client.id", "KafkaTwitterProducer")
    properties.setProperty("bootstrap.servers", brokers)
    properties.setProperty("acks","all")
    properties.setProperty("retries","0")
    properties
  }
}

class PubSubProducer(projectId: String, topicId: String) extends Producer[String] {
  val publisher: Publisher = Publisher
    .newBuilder(ProjectTopicName.of(projectId, topicId))
    .build

  override def processMessage(message: String): Future[String] = {
    val pubsubMessage = PubsubMessage.newBuilder.setData(ByteString.copyFromUtf8(message)).build
    publisher.publish(pubsubMessage)
  }
}
