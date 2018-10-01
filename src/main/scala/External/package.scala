import java.nio.charset.StandardCharsets

import External.Message.RegisterMessage
import User.Session
import com.google.protobuf.ByteString
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.json.{Json, JsonObject}
import io.vertx.scala.ext.web.handler.sockjs.SockJSSocket
import io.protoless.generic.auto._
import io.protoless.syntax._

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

package object External {
    val externHubs=new mutable.HashMap[String,Externals]()

    class Externals(name:String,val socket:SockJSSocket){
        var index=1
        val promiseMap=new mutable.HashMap[Int,Promise[JsonObject]]()

        def CompletePromise(i:Int,result:JsonObject):Unit={
            promiseMap.remove(i).foreach(p=>{
                p.success(result)
            })
        }

        def RedirectUriRequest(hubName: String, action: String, target: String, query: Map[String, Vector[String]], body: Option[JsonObject], sessionId: String): Future[JsonObject] = {
            val p = Promise[JsonObject]()
            val uriMessage = Message.UriMessage(hubName, action, target, Some(QueryToJson(query).toString), body.map(_.toString))
            val requestBytes = Message.ExternalMessage(index, response = None, request = Some("uri"), Some(ByteString.copyFrom(uriMessage.asProtobufBytes)), sessionId).asProtobufBytes
            promiseMap += (index -> p)
            index = index + 1
            socket.write(Buffer.buffer(requestBytes))
            p.future
        }
    }

    def Register(request:JsonObject,socket:SockJSSocket):Future[Handler[io.vertx.core.buffer.Buffer]]={
        val name=request.getString("name")
        if(externHubs.contains(name))
            Future.failed(new Exception(s"external service [$name] exist"))
        else{
            val e=new Externals(name,socket)
            externHubs+=(name->e)
            Future.successful(ExternalSocketHandlerCreator(e))
        }
    }

    class ExternalHub extends Uri.Hub.HubTrait{
        override def apply(hubName:String,action: String, target: String, query: Map[String, Vector[String]], body: Option[JsonObject], identity: Session.Identity): Future[JsonObject] = {
            if(!externHubs.contains(hubName)){
                Future.successful(Json.emptyObj().put("error",s"hub $hubName do not exist"))
            }
            else
                externHubs(hubName).RedirectUriRequest(action,target,query,body,identity)
        }
    }

    def ExternalSocketHandlerCreator(e: Externals): Handler[io.vertx.core.buffer.Buffer] = (data: Buffer) =>
        data.getBytes.as[Message.ExternalMessage] match {
            case Left(e) =>
            case Right(Message.ExternalMessage(index, Some("error"), _, bodyOpt, sessionID)) =>
                println(s"complete index $index")
                e.FailPromise(index, bodyOpt.get.toStringUtf8)
            case Right(Message.ExternalMessage(index, Some(response), _, bodyOpt, sessionID)) =>
                val body = bodyOpt.map(bs => Json.fromObjectString(new String(bs.toByteArray, StandardCharsets.UTF_8)))
                    .getOrElse(Json.emptyObj)
                println(s"complete index $index with ${body.toString}")
                e.CompletePromise(index, body)
            case Right(Message.ExternalMessage(index, None, Some("uri"), bodyOpt, sessionID)) =>
                bodyOpt.flatMap(bs => bs.toByteArray.as[Message.UriMessage].toOption) match {
                    case Some(Message.UriMessage(hubName, action, target, query, body)) =>
                        val bodyJson = body.map(Json.fromObjectString)
                        val queryObj = JsonToQuery(query.map(s => Json.fromObjectString(s)).getOrElse(Json.emptyObj()))
                        Uri.apply(hubName, action, target, queryObj, bodyJson, sessionID) onComplete {
                            case Success(value) =>
                                val responseBytes = Message.ExternalMessage(index, Some("uri"), None, Some(ByteString.copyFrom(value.toBuffer.getBytes)), sessionID).asProtobufBytes
                                e.socket.write(Buffer.buffer(responseBytes))
                            case Failure(exception) =>
                                val responseBytes = Message.ExternalMessage(index, Some("error"), None, Some(ByteString.copyFromUtf8(exception.getMessage)), sessionID).asProtobufBytes
                                e.socket.write(Buffer.buffer(responseBytes))
                        }
                    case None =>
                        val responseBytes = Message.ExternalMessage(index, Some("error"), None, Some(ByteString.copyFromUtf8("body is not object")), sessionID).asProtobufBytes
                        e.socket.write(Buffer.buffer(responseBytes))
            }
            case Right(Message.ExternalMessage(index, _, _, _, sessionID)) =>
                val responseBytes = Message.ExternalMessage(index, Some("error"), None, Some(ByteString.copyFromUtf8("bad message")), sessionID).asProtobufBytes
                e.socket.write(Buffer.buffer(responseBytes))
            case _ =>
                val responseBytes = Message.ExternalMessage(-1, Some("error"), None, Some(ByteString.copyFromUtf8("bad message")), "").asProtobufBytes
                e.socket.write(Buffer.buffer(responseBytes))
    }
}
