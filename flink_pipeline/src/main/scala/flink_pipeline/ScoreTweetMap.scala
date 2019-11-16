package flink_pipeline

import flink_pipeline.ml.{SentimentPredictor, Tokenizer, Word2Vec}
import org.apache.flink.api.common.functions.RichMapFunction

class ScoreTweetMap extends RichMapFunction[Tweet, Tweet] {

  lazy val tokenizer = new Tokenizer
  lazy val w2vec = Word2Vec()

  override def map(tweet: Tweet): Tweet = {
    val words = tokenizer.transform(tweet.text)

    val sentiment = w2vec.transform(words) map { v =>
      predictSentiment(SentimentPredictor.score(v.toArray)(1))
    }

    tweet.copy(words = Some(words), sentiment = sentiment)
  }

  def predictSentiment(positiveProbability: Double, tolerance: Double = 0.01): String =
    if (positiveProbability > 0.5 + tolerance) "positive"
    else if (positiveProbability < 0.5 - tolerance) "negative"
    else "undecided"
}
