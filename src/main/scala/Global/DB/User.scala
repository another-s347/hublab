package Global.DB

import _root_.User.BaseUserInfo
import io.vertx.lang.scala.json.Json
import io.vertx.scala.ext.mongo.UpdateOptions

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object User{
    def GetUserInfo(username:String):Future[BaseUserInfo]={
        go (connection=>{
            connection.findOneFuture("user",Json.emptyObj().put("Username",username),None)
        }) transform {
            case Success(value)=> Try(BaseUserInfo.fromJson(value))
            case scala.util.Failure(exception) => Failure(exception)
        }
    }

    def SetUserInfo(baseUserInfo: BaseUserInfo):Future[String]={
        go (connection=>{
            connection.replaceWithOptionsFuture("user",Json.emptyObj().put("username",baseUserInfo.name),baseUserInfo.toJson,UpdateOptions().setUpsert(true))
        }) transform {
            case Success(value)=>Success(baseUserInfo.name)
            case scala.util.Failure(exception) => Failure(exception)
        }
    }

    def IsUserExist(username:String):Future[Boolean]={
        go (connection=>{
            connection.findOneFuture("user",Json.emptyObj().put("Username",username),Some(Json.emptyObj().put("_id","")))
        }) transform {
            case Success(null)=> Success(false)
            case Success(_)=> Success(true)
            case scala.util.Failure(exception) => Failure(exception)
        }
    }
}