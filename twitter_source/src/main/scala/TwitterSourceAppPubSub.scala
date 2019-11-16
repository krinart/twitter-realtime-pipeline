object TwitterSourceAppPubSub {

  def main(args: Array[String]): Unit = {

    val keys = List("CONSUMER_KEY", "CONSUMER_SECRET", "TOKEN", "TOKEN_SECRET")

    keys.foreach { key =>
      if (!sys.env.contains(key))
      throw new Error(s"Environment variable $key is required") }

    val auth = TwitterAuth(sys.env("CONSUMER_KEY"), sys.env("CONSUMER_SECRET"), sys.env("TOKEN"), sys.env("TOKEN_SECRET"))
    val twitterClient = new TwitterClient(args.toSeq, auth)

    val producer = new PubSubProducer("pragmatic-zoo-253123", "twitter-source")

    twitterClient.start()
    while(true) {
        val tweet = twitterClient.readTweet()
        println(tweet)
        producer.processMessage(tweet)
    }
  }
}
