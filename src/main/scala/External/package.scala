import Uri.Hub
import User.Session
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.json.{Json, JsonObject}
import io.vertx.scala.ext.web.handler.sockjs.SockJSSocket

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

        def RedirectUriRequest(action: String, target: String, query: Map[String, Vector[String]], body: Option[JsonObject], identity: Session.Identity):Future[JsonObject]={
            val p=Promise[JsonObject]()
            val j=Json.emptyObj()
                .put("index",index)
                .put("request","uri")
                .put("action",action)
                .put("target",target)
                .put("body",body.orNull)
                .put("identity",identity)//TODO: Identity to Json
            //TODO: query to Json
            index=index+1
            promiseMap+=(index->p)
            socket.write(j.toBuffer)
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

    def ExternalSocketHandlerCreator(e:Externals):Handler[io.vertx.core.buffer.Buffer]=(data:Buffer)=>{
        val json=Json.fromObjectString(data.toString)
        val index=json.getInteger("index")
        if(json.containsKey("response")){
            e.CompletePromise(index,json)
        }
        else if(json.containsKey("request")){
            json.getString("request") match {
                case "uri"=>
                    Try(json.getString("uri")) match {
                        case Success(null)=> e.socket.write(Json.emptyObj().put("index",index).put("response","error").put("error","uri is null").toBuffer)
                        case Success(value)=>
                            Try(json.getJsonObject("body")) {
                                case Success(null)=>
                                    Uri.apply(value,None)
                                case Success(value)=>
                                    Some(value)
                                case Failure(exception)=>
                                    e.socket.write(Json.emptyObj().put("index",index).put("response","error").put("error","body is not object").toBuffer)
                            }
                    }
                case unkown=>
                    e.socket.write(Json.emptyObj().put("index",index).put("response",unkown).put("error","unknwon request").toBuffer)
            }
        }
    }
}
