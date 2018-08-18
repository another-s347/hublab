import User.Session
import io.vertx.lang.scala.json.{Json, JsonObject}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

package object Data {
    case class File(fileName:String, prefix:String, data:JsonObject, user:String)

    class FileHub extends Uri.Hub.HubTrait{
        override def apply(action: String, target: String, query: Map[String, Vector[String]], body: Option[JsonObject], identity: Session.Identity): Future[JsonObject] = {
            val queryInDB=Json.emptyObj()
            query.view.filter(b=>b._1!="user").filter(b=>b._2.nonEmpty).foreach(b=>{
                queryInDB.put(b._1,b._2.head)
            })
            action match {
                case "get"=>get(queryInDB,target,identity.User.baseUserInfo.name)
                case unknown=>Future.failed(new Exception(s"unknwon action:$unknown"))
            }
        }

        private def get(query:JsonObject,target:String,user: String):Future[JsonObject]={
            target match {
                case "meta"=>
                    Global.DB.Data.GetFileWithQuery(user,query) transform {
                        case Success(value)=>
                            value.putNull("data")
                            Success(value)
                    }
                case "all"=>
                    Global.DB.Data.GetFileWithQuery(user,query)
                case "data"=>
                    Global.DB.Data.GetFileWithQuery(user,query) transform {
                        case Success(value)=>
                            Success(Json.emptyObj().put("data",value.getJsonObject("data")))
                    }
            }
        }
    }
}
