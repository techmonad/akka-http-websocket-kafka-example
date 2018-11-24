package com.techmonad.json

import org.json4s._
import org.json4s.native.{JsonMethods, Serialization}

object JsonHelper {

  implicit val formats: DefaultFormats = DefaultFormats

  def write[T <: AnyRef](value: T): String = Serialization.write(value)

  def parse(value: String): JValue = JsonMethods.parse(value)

}
