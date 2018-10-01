package Global.DB

import _root_.Data._
import io.vertx.lang.scala.json.{Json, JsonObject}
import io.vertx.scala.ext.mongo.UpdateOptions

import scala.collection.script.Update
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object Data{
    def GetFile(username:String,prefix:String,fileName:String):Future[File]={
        go (connection=>{
            connection.findOneFuture("file",Json.emptyObj().put("user",username).put("prefix",prefix).put("fileName",fileName),None)
        }) transform {
            case Success(value)=>
                Success(File(user=username,prefix=prefix,data=value.getJsonObject("data"),fileName=fileName))
            case scala.util.Failure(exception) => Failure(exception)
        }
    }

    def GetFileWithQuery(username:String,query:JsonObject):Future[JsonObject]={
        go (connection=>{
            connection.findOneFuture("file",query.put("user",username),None)
        })
    }

    def SetFile(file:File):Future[Unit]={
        go (connection=>{
            val fileJson=Json.emptyObj().put("user",file.user).put("prefix",file.prefix).put("fileName",file.fileName).put("data",file.data)
            connection.replaceWithOptionsFuture("file",Json.emptyObj().put("user",file.user).put("prefix",file.prefix).put("fileName",file.fileName),fileJson,UpdateOptions().setUpsert(true))
        })
    }
}