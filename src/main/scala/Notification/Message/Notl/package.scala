package Notification.Message

import Notification.Message.Notl.NotlType.NotlType
import io.vertx.lang.scala.json.{Json, JsonObject}

import scala.util.{Failure, Success, Try}

package object Notl {
    case class NotificationMessage(
                                      var globalMessageUID: String = "",
                                      var message: String = "",
                                      var detail: String = "",
                                      var source: String = "",
                                      var typ: NotlType = NotlType.WARNING
                                  ) extends EventMessage[NotificationMessage] {
        override def toJson(): JsonObject = {
            val ret = Json.emptyObj()
            ret.put("messageUID", globalMessageUID)
                .put("type", typ.id)
                .put("message", message)
            if (detail.length != 0)
                ret.put("detail", detail)
            if (source.length != 0)
                ret.put("source", source)
            ret
        }

        override def fromJson(obj: JsonObject): NotificationMessage = {
            if (obj.containsKey("source")) {
                source = Try(obj.getString("source")) match {
                    case Success(x) =>
                        x
                    case Failure(_: java.lang.ClassCastException) => {
                        throw Exception.MessageConvertException("NotificationMessage:source should be a string")
                    }
                }
            }
            if (obj.containsKey("detail")) {
                detail = Try(obj.getString("detail")) match {
                    case Success(x) => x
                    case Failure(_: java.lang.ClassCastException) => {
                        throw Exception.MessageConvertException("NotificationMessage:source should be a string")
                    }
                }
            }
            message = Try(obj.getString("message")) match {
                case Success(null) =>
                    throw Exception.MessageConvertException("NotificationMessage:message does not exist")
                case Success(msg) => msg
                case Failure(_: java.lang.ClassCastException) => {
                    throw Exception.MessageConvertException("NotificationMessage:message should be a string")
                }
            }
            globalMessageUID = Try(obj.getString("messageUID")) match {
                case Success(null) =>
                    throw Exception.MessageConvertException("NotificationMessage:messageUID does not exist")
                case Success(uid) => uid
                case Failure(_: java.lang.ClassCastException) => {
                    throw Exception.MessageConvertException("NotificationMessage:messageUID should be a string")
                }
            }
            Try(typ = NotlType(obj.getInteger("type"))) match {
                case Failure(_: java.lang.ClassCastException) => {
                    throw Exception.MessageConvertException("NotificationMessage:type should be a int")
                }
                case Failure(_: java.util.NoSuchElementException) => {
                    throw Exception.MessageConvertException(s"NotificationMessage:type should be a int <= $NotlType.maxId")
                }
            }
        }
    }

    object NotlType extends scala.Enumeration {
        type NotlType = Value
        val WARNING, NORMAL, CRITICAL, DRAWBACK = Value
    }
}
