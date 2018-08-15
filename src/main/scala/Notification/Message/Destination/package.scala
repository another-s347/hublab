package Notification.Message

import Notification.Message.Destination.DstType.DstType
import io.vertx.lang.scala.json.{Json, JsonObject}

import scala.util.{Failure, Success, Try}

package object Destination {
    case class NotlDestination(
                                  var user: String = "",
                                  var devices: Array[String] = Array.empty[String],
                                  typ: DstType = DstType.USERDEVICE
                              ) extends EventMessage[NotlDestination] {
        override def toJson(): JsonObject = {
            import ArrayHelper._
            Json.emptyObj()
                .put("user", user)
                .put("devices", devices.toJson())
        }

        override def fromJson(obj: JsonObject): NotlDestination = {
            user = Try(obj.getString("user")) match {
                case Success(null) =>
                    throw Exception.MessageConvertException("NotlDestination:user does not exist")
                case Success(x) => x
                case Failure(_: java.lang.ClassCastException) =>
                    throw Exception.MessageConvertException("NotlDestination:user should be a string")
            }
            val devicesJson = Try(obj.getJsonArray("devices")) match {
                case Success(null) =>
                    throw Exception.MessageConvertException("NotlDestination:devices does not exist")
                case Success(x) => x
                case Failure(_: java.lang.ClassCastException) =>
                    throw Exception.MessageConvertException("NotlDestination:devices should be a json array")
            }
            Try(for (i <- 0 until devicesJson.size()) {
                devices :+ devicesJson.getString(i)
            }) match {
                case Failure(_: java.lang.ClassCastException) =>
                    throw Exception.MessageConvertException("NotlDestination:devices should be a json array of string")
            }
            this
        }
    }

    type Destinations = Array[NotlDestination]

    object DstType extends Enumeration {
        type DstType = Value
        val BROADCAST, USER, DEVICES, USERDEVICE = Value
    }
}
