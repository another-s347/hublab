package Notification.Message

import Notification.Message
import io.vertx.lang.scala.json.{Json, JsonObject}
import Notification.Message.Destination._
import Notification.Message.Push.PushType.PushType

import scala.util.{Failure, Success, Try}

package object Push {
    case class PushMessage(
                              var dst: NotlDestination = NotlDestination(),
                              var msg: Notl.NotificationMessage = Notl.NotificationMessage(),
                              var typ: PushType = PushType.NORMAL,
                              var src: String = ""
                          ) extends EventMessage[PushMessage] {
        def toNotlMessage(hubSource: String): Notl.NotificationMessage = {
            src = "%s:%s".format(hubSource, src)
            val uuid = Message.generateMessageUUID().toString
            Notl.NotificationMessage(
                source = src,
                typ = msg.typ,
                message = msg.message,
                detail = msg.detail,
                globalMessageUID = uuid
            )
        }

        override def toJson(): JsonObject = Json.emptyObj()
            .put("source", src)
            .put("type", typ.id)
            .put("message", msg.toJson())
            .put("destination", dst.toJson())

        override def fromJson(obj: JsonObject): PushMessage = {
            src = Try(obj.getString("source")) match {
                case Success(null) =>
                    throw Exception.NotlTaskException("PushMessage:source does not exist")
                case Success(x) => x
                case Failure(_: java.lang.ClassCastException) =>
                    throw Exception.NotlTaskException("PushMessage:source should be a string")
            }

            Try(typ = PushType(obj.getInteger("type"))) recover {
                case _: java.lang.ClassCastException =>
                    throw Exception.NotlTaskException("PushMessage:type should be a int")
                case _: java.util.NoSuchElementException =>
                    throw Exception.NotlTaskException(s"PushMessage:type should be a int < $PushType.maxId")
            }

            msg = Notl.NotificationMessage().fromJson(Try(obj.getJsonObject("message")) match {
                case Success(null) =>
                    throw Exception.NotlTaskException("PushMessage:message does not exist")
                case Success(x) => x
                case Failure(_: java.lang.ClassCastException) =>
                    throw Exception.NotlTaskException("PushMessage:message should be a json object")
            })

            dst = NotlDestination().fromJson(Try(obj.getJsonObject("destination")) match {
                case Success(null) =>
                    throw Exception.NotlTaskException("PushMessage:destination does not exist")
                case Success(x) => x
                case Failure(_: java.lang.ClassCastException) =>
                    throw Exception.MessageConvertException("PushMessage:destination should be a json object")
            })
            this
        }
    }

    object PushType extends Enumeration {
        type PushType = Value
        val TEMP, NORMAL = Value
    }
}
