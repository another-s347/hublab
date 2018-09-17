package Uri

import Data.FileHub
import User.Session.Identity
import io.vertx.lang.scala.json.{Json, JsonObject}

import scala.concurrent.Future

object Hub{
    trait HubTrait{
        def apply(hubName:String,action:String,target:String,query:Map[String,Vector[String]],body:Option[JsonObject],identity:Identity):Future[JsonObject]
    }

    def apply(hubName:String):HubTrait={
        map(hubName)
    }

    val map:Map[String,HubTrait]=Map(
        "file"->new FileHub
    ).withDefaultValue(new ErrorHub)

    class ErrorHub extends HubTrait{
        override def apply(action: String, target: String, query: Map[String, Vector[String]],body:Option[JsonObject],identity:Identity): Future[JsonObject] =
            Future.successful(Json.emptyObj().put("error","hub do not exist"))
    }
}