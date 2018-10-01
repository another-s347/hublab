import User.Session.Identity
//import io.lemonlabs.uri._
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.MultiMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable
import scala.concurrent.Future

package object Uri {
    //    def apply(uriString: String, body: Option[JsonObject], identity: Identity, sessionId: String): Future[JsonObject] = {
    //        val uri=Uri.parse(uriString)
    //        uri match {
    //            case UrlWithoutAuthority("hublab",PathParts(hub,action,target),query,_)=>
    //                Hub(hub)(hub, action, target, query.paramMap, body, identity, sessionId)
    //            case _=>
    //                Future.failed(new Exception("uri cannot be parsed using UrlWithoutAuthority"))
    //        }
    //    }

    def apply(hubName: String, action: String, target: String, query: MultiMap, body: Option[JsonObject], sessionId: String): Future[JsonObject] = {
        User.Session.GetOnlineIdentity(sessionId) flatMap { identity =>
            Hub(hubName)(hubName, action, target, ConvertMultiMap(query), body, identity, sessionId)
        }
    }

    def apply(hubName: String, action: String, target: String, query: Map[String, Vector[String]], body: Option[JsonObject], sessionId: String): Future[JsonObject] = {
        User.Session.GetOnlineIdentity(sessionId) flatMap { identity =>
            Hub(hubName)(hubName, action, target, query, body, identity, sessionId)
        }
    }

    private def ConvertMultiMap(query: MultiMap): Map[String, Vector[String]] = {
        val mutableMap = new mutable.HashMap[String, Vector[String]]()
        query.names().foreach(key => mutableMap += (key -> query.getAll(key).toVector))
        mutableMap.toMap
    }
}
