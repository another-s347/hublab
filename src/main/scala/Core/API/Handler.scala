package Core.API

import External.Message
import External.Message.ExternalMessage
import com.google.protobuf.ByteString
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.json.Json
import io.vertx.scala.core.MultiMap
import io.vertx.scala.ext.web.{Cookie, RoutingContext}
import io.vertx.scala.ext.web.handler.sockjs.SockJSSocket
import io.protoless.syntax._
import io.protoless.generic.auto._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object Handler{
    val Login:Handler[RoutingContext]=(routingContext:RoutingContext)=>{
        routingContext.response().setChunked(true)
        Try(User.UserManager.JsonToLoginInfo(routingContext.getBodyAsJson().get)) match {
            case Success(loginInfo)=>
                    User.UserManager.Signin(loginInfo) onComplete {
                        case Success(value)=>
                            routingContext.addCookie(Cookie.cookie("session", value))
                            routingContext.response().write(value).end()
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

    val externalRegister:Handler[SockJSSocket]= (socket:SockJSSocket)=>{
        socket.handler((data:Buffer)=>{
            data.getBytes.as[Message.RegisterMessage] match {
                case Left(_) =>
                    val response = ExternalMessage(0, Some("error"), None, Some(ByteString.copyFromUtf8("bad message")), "")
                    socket.write(Buffer.buffer(response.asProtobufBytes))
                case Right(msg) =>
                    External.Register(msg, socket) onComplete {
                        case Success(value) =>
                            val response = ExternalMessage(0, Some("register"), None, Some(ByteString.copyFromUtf8("external service register success")), "")
                            socket.write(Buffer.buffer(response.asProtobufBytes))
                            socket.handler(value)
                        case Failure(exception) =>
                            ???
                            println(exception.getLocalizedMessage)
                            socket.close()
                    }
            }
        })
    }

    val externalAccess: Handler[RoutingContext] = (routingContext: RoutingContext) => {
        routingContext.response().setChunked(true)
        Helper.TryGetSessionID(routingContext) match {
            case Some(sessionId) =>
                val hubname = routingContext.pathParam("hubname").get
                val action = routingContext.pathParam("action").get
                val target = routingContext.pathParam("target").get
                val query: MultiMap = routingContext.queryParams()
                val f = Uri.apply(hubname, action, target, query, routingContext.getBodyAsJson(), sessionId)
                f onComplete {
                    case Success(value) =>
                        println(value.toString)
                        routingContext.response().end(value.toBuffer)
                    case Failure(exception) =>
                        routingContext.response().end(Json.emptyObj().put("error", exception.getMessage).toBuffer)
                }
            case None =>
                ???
        }
        //        Try(User.UserManager.JsonToLoginInfo(routingContext.getBodyAsJson().get)) match {
        //            case Success(loginInfo)=>
        //                User.UserManager.Signin(loginInfo) onComplete {
        //                    case Success(value)=>
        //                        routingContext.response().write(value).end()
        //                    case Failure(exception)=>
        //                        routingContext.response().write(s"fail:${exception.getLocalizedMessage}").end()
        //                }
        //            case Failure(exception)=>
        //                routingContext.response().write(s"fail:${exception.getLocalizedMessage}").end()
        //        }
    }
}