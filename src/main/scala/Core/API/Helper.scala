package Core.API

import User.Session.SessionId
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.ext.web.{Cookie, RoutingContext}

import scala.util._

object Helper{
    def TryGetSessionID(routingContext: RoutingContext):Option[SessionId]={
        lazy val body=Try(routingContext.getBodyAsJson()).toOption.flatten
        lazy val bodySession=body.flatMap(b=>{
            Try(b.getString("session")) match {
                case Success(null)=>None
                case Success(value)=>Some(value)
                case Failure(_)=>None
            }
        })
        routingContext.getCookie("session") map (b=>{
            b.getValue()
        }) orElse bodySession
    }
}