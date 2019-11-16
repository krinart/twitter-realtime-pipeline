package flink_pipeline

import org.apache.flink.api.common.serialization.{DeserializationSchema, SerializationSchema}
import org.apache.flink.api.common.typeinfo.TypeInformation
import flink_pipeline.model._
import flink_pipeline.utils.AggregateEvent
import io.circe.syntax._

class StringDeserializationSchema extends DeserializationSchema[String] {
  override def deserialize(message: Array[Byte]): String = message.map(_.toChar).mkString

  override def isEndOfStream(nextElement: String): Boolean = false

  override def getProducedType: TypeInformation[String] = TypeInformation.of(classOf[String])
}

class StringSerializationSchema extends SerializationSchema[String] {
  override def serialize(element: String): Array[Byte] =
    element.getBytes()
}

class TweetSerializationSchema extends SerializationSchema[Tweet] {
  override def serialize(element: Tweet): Array[Byte] =
    element.asJson.noSpaces.getBytes()
}

class OutgoingEventSerializationSchema extends SerializationSchema[AggregateEvent] {
  override def serialize(element: AggregateEvent): Array[Byte] =
    element.asJson.noSpaces.getBytes()
}

