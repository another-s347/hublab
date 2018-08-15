package Core.API

import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.json.Json
import io.vertx.scala.ext.web.RoutingContext
import io.vertx.scala.ext.web.handler.sockjs.SockJSSocket

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object Handler{
    val Login:Handler[RoutingContext]=(routingContext:RoutingContext)=>{
        routingContext.response().setChunked(true)
        Try(User.UserManager.JsonToLoginInfo(routingContext.getBodyAsJson().get)) match {
            case Success(loginInfo)=>
                    User.UserManager.Signin(loginInfo) onComplete {
                        case Success(value)=>
                            routingContext.response().write(s"success:$value").end()
                        case Failure(exception)=>
                            routingContext.response().write(s"fail:${exception.getLocalizedMessage}").end()
                    }
            case Failure(exception)=>
                routingContext.response().write(s"fail:${exception.getLocalizedMessage}").end()
        }
    }

    val registerNotification:Handler[SockJSSocket]=(socket:SockJSSocket)=>{
        socket.handler((data:Buffer)=>{
            val json=Json.fromObjectString(data.toString)
            val sessionId=json.getString("session")
            User.Session.RegisterDeviceConnection(socket,sessionId) onComplete {
                case Success(true)=>
                    socket.write("register notification success")
                case Success(false)=>
                    socket.write("register notification with different address?")
                case Failure(exception)=>
                    socket.write(s"register notification fail:${exception.getLocalizedMessage}")
            }
        })
    }
}