package flink_pipeline.ml

import java.util.zip.GZIPInputStream

import io.circe.parser.decode


class Word2Vec(w2vMap: Map[String, Seq[Double]]) {

  val dim = 50

  def transform(words: Seq[String]): Option[Seq[Double]] = {
    words.foldLeft((List.fill(dim)(0.0), 0))((acc, word) => {
      if (w2vMap.contains(word)) {
        (
          acc._1.zip(w2vMap(word)) map { case (a, b) => a + b },
          acc._2 + 1
        )
      } else {
        acc
      }
    }) match {
      case (_, 0) => None
      case (list, n) => Some(list.map(_ / n))
    }
  }
}


object Word2Vec {

  def apply(): Word2Vec = {
    val filename = "/word2vec.json.gz"
    val stream = new GZIPInputStream(getClass.getResourceAsStream(filename))
    val lines: String = scala.io.Source.fromInputStream(stream).mkString

    decode[Map[String, Seq[Double]]](lines) match {
      case Right(m) => new Word2Vec(m)
      case _ => throw new Error(s"failed to parse $filename")
    }
  }
}

