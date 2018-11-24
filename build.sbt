name := "akka-http-websocket-kafka-example"

version := "0.1"

scalaVersion := "2.12.7"

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % "2.0.0",
  "com.typesafe.akka" %% "akka-stream" % "2.5.18",
  "com.typesafe.akka" %% "akka-http" % "10.1.5",
  "org.json4s" %% "json4s-native" % "3.6.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.5" % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.18" % Test
)

