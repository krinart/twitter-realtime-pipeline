ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.12.9"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/contents/repositories/snapshots"
resolvers += "mvnrepository" at "http://mvnrepository.com/artifact/"
resolvers += "central" at "http://repo1.maven.org/maven2/"

val flinkVersion = "1.9.0"
lazy val flinkDependencies = Seq(
  "org.apache.flink" %% "flink-scala" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided",
  "org.apache.flink" % "flink-core" % flinkVersion,
  "org.apache.flink" %% "flink-connector-kafka-base" % flinkVersion,
  "org.apache.flink" %% "flink-connector-kafka-0.10" % flinkVersion,
  "org.apache.flink" %% "flink-connector-elasticsearch6" % flinkVersion,
  "org.apache.flink" %% "flink-connector-gcp-pubsub" % flinkVersion,
)

lazy val logDependencies = Seq(
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "log4j" % "log4j" % "1.2.17",
  "org.slf4j" % "slf4j-log4j12" % "2.0.0-alpha1",
)

lazy val circeVersion = "0.11.1"
lazy val circeDependencies = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

lazy val kafkaDependencies = Seq(
  "org.apache.kafka" %% "kafka" % "2.0.0"
)

lazy val flink_pipeline = project
  .settings(
    name := "flink_pipeline",
    libraryDependencies ++= flinkDependencies ++ circeDependencies ++ logDependencies,
    assemblyMergeStrategy in assembly := {
      case "META-INF/io.netty.versions.properties" => MergeStrategy.first
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    assemblyOutputPath in assembly := baseDirectory.value / "../docker/flink-pipeline/flink_pipeline-assembly-0.1.jar"
  )

lazy val twitter_source = project
  .settings(
    name := "twitter_source",
    libraryDependencies ++= kafkaDependencies ++ Seq(
      "com.google.cloud" % "google-cloud-pubsub" % "1.62.0",
      "com.twitter" % "hbc-core" % "2.2.0"),
    assemblyOutputPath in assembly := baseDirectory.value / "../docker/twitter-source/twitter_source-assembly-0.1.jar"
  )

// make run command include the provided dependencies
Compile / run  := Defaults.runTask(Compile / fullClasspath,
  Compile / run / mainClass,
  Compile / run / runner
).evaluated

// exclude Scala library from assembly
assembly / assemblyOption  := (assembly / assemblyOption).value.copy(includeScala = false)

