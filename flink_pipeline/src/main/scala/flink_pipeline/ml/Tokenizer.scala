package flink_pipeline.ml

import scala.io.Source

case class Tokenizer() {

  lazy val stopWords = Source
    .fromInputStream(getClass.getResourceAsStream("/stopwords.txt"))
    .getLines
    .map(_.toString)
    .toSet

  def transform(text: String): Seq[String] = {
    text
      .split("\\W+")
      .map(_.toLowerCase)
      .filter(_.length() > 2)
      .filter(!stopWords(_))
      .toSeq
  }
}
