package com.techmonad.kafka

import java.time.Duration
import java.util.Properties

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.util.control.NonFatal

class Consumer(topics: List[String], servers: List[String]) {

  val logger: Logger = LoggerFactory.getLogger(this.getClass())

  private val props: Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", servers.mkString(","))
    props.put("group.id", "test")
    props.put("enable.auto.commit", "true")
    props.put("auto.commit.interval.ms", "1000")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props
  }


  private val consumer = new KafkaConsumer[String, String](props)

  consumer.subscribe(topics.asJava)

  def read(): List[String] =
    try {
      consumer.poll(Duration.ofMillis(100))
        .asScala.map { record => record.value() }
        .toList
    } catch {
      case NonFatal(th) =>
        logger.error("Error on reading consumer ", th)
        Nil
    }

  def close(): Unit =
    try {
      consumer.close()
    } catch {
      case NonFatal(th) =>
        logger.error("Error on closing consumer ", th)
    }

}
