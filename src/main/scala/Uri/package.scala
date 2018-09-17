import User.Session.Identity
import io.lemonlabs.uri._
import io.vertx.lang.scala.json.JsonObject

import scala.concurrent.Future

package object Uri {
    def apply(uriString:String,body:Option[JsonObject],identity:Identity):Future[JsonObject]={
        val uri=Uri.parse(uriString)
        uri match {
            case UrlWithoutAuthority("hublab",PathParts(hub,action,target),query,_)=>
                Hub(hub)(hub,action,target,query.paramMap,body,identity)
            case _=>
                Future.failed(new Exception("uri cannot be parsed using UrlWithoutAuthority"))
        }
    }
}
