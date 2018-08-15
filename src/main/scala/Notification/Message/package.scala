package Notification

import java.util.UUID

import io.vertx.lang.scala.json.{Json, JsonObject}

import scala.util.{Failure, Success, Try}

package object Message {
    trait EventMessage[A] {
        def toJson(): JsonObject

        def fromJson(obj: JsonObject): A
    }

    def generateMessageUUID(): UUID = UUID.randomUUID()

    case class HubRequest(var session: String = "", var content: JsonObject = Json.emptyObj()) extends EventMessage[HubRequest] {
        override def toJson(): JsonObject = {
            Json.emptyObj()
                .put("sessionId", session)
                .put("content", content)
        }

        override def fromJson(obj: JsonObject): HubRequest = {
            session = Try(obj.getString("sessionId")) match {
                case Success(null) =>
                    throw Exception.NotlTaskException("HubRequest:sessionId does not exist")
                case Success(x) => x
                case Failure(_: java.lang.ClassCastException) =>
                    throw Exception.NotlTaskException("HubRequest:sessionId should be a string")
            }
            content = Try(obj.getJsonObject("content")) match {
                case Success(null) =>
                    throw Exception.NotlTaskException("HubRequest:content does not exist")
                case Success(x) => x
                case Failure(_: java.lang.ClassCastException) =>
                    throw Exception.NotlTaskException("HubRequest:content should be a string")
            }
            this
        }
    }
}
