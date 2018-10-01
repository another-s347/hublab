import User.Session
import io.vertx.lang.scala.json.{Json, JsonObject}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

package object Data {
    case class File(fileName:String, prefix:String, data:JsonObject, user:String)
    object File{
        def fromJson(j:JsonObject,user:String):File={
            val filename = Try(j.getString("filename")) match {
                case Success(null) =>
                    throw Exception.MessageConvertException("filename do not exist")
                case Success(x) => x
                case Failure(_: java.lang.ClassCastException) =>
                    throw Exception.MessageConvertException("filename should be a string")
            }
            val prefix = Try(j.getString("prefix")) match {
                case Success(null) =>
                    throw Exception.MessageConvertException("prefix do not exist")
                case Success(x) => x
                case Failure(_: java.lang.ClassCastException) =>
                    throw Exception.MessageConvertException("prefix should be a string")
            }
            val data = Try(j.getJsonObject("data")) match {
                case Success(null) =>
                    throw Exception.MessageConvertException("data do not exist")
                case Success(x) => x
                case Failure(_: java.lang.ClassCastException) =>
                    throw Exception.MessageConvertException("data should be a obj")
            }
            File(filename,prefix,data,user)
        }
    }

    class FileHub extends Uri.Hub.HubTrait{
        override def apply(hubName: String, action: String, target: String, query: Map[String, Vector[String]], body: Option[JsonObject], identity: Session.Identity, sessionId: String): Future[JsonObject] = {
            action match {
                case "get" =>
                    val queryInDB = Json.emptyObj()
                    query.view.filter(b => b._1 != "user").filter(b => b._2.nonEmpty).foreach(b => {
                        queryInDB.put(b._1, b._2.head)
                    })
                    get(queryInDB, target, identity.User.baseUserInfo.name)
                case "set"=>set(target,body,identity.User.baseUserInfo.name)
                case unknown=>Future.failed(new Exception(s"unknwon action:$unknown"))
            }
        }

        private def set(target:String,body:Option[JsonObject],user:String):Future[JsonObject]={
            if(body.isEmpty)
                Future.failed(new Exception("body is empty"))
            Global.DB.Data.SetFile(File.fromJson(body.get,user)) transform {
                case Success(value)=>
                    Success(Json.emptyObj().put("result","success"))
                case scala.util.Failure(exception) => Failure(exception)
            }
        }

        private def get(query:JsonObject,target:String,user: String):Future[JsonObject]={
            target match {
                case "meta"=>
                    Global.DB.Data.GetFileWithQuery(user,query) transform {
                        case Success(value)=>
                            value.putNull("data")
                            Success(value)
                        case scala.util.Failure(exception) => Failure(exception)
                    }
                case "all"=>
                    Global.DB.Data.GetFileWithQuery(user,query)
                case "data"=>
                    Global.DB.Data.GetFileWithQuery(user,query) transform {
                        case Success(value)=>
                            Success(Json.emptyObj().put("data",value.getJsonObject("data")))
                        case scala.util.Failure(exception) => Failure(exception)
                    }
            }
        }
    }
}
