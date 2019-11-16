package flink_pipeline.utils

import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.watermark.Watermark

import scala.math.max


abstract class BoundedOutOfOrdernessGenerator[A](maxOutOfOrderness: Long = 0) extends AssignerWithPeriodicWatermarks[A] {
  var currentMaxTimestamp: Long = _

  override def extractTimestamp(element: A, previousElementTimestamp: Long): Long = {
    val timestamp = elementTimestamp(element)
    currentMaxTimestamp = max(timestamp, currentMaxTimestamp)
    timestamp
  }

  override def getCurrentWatermark: Watermark = {
    new Watermark(currentMaxTimestamp - maxOutOfOrderness)
  }

  def elementTimestamp(element: A): Long
}

