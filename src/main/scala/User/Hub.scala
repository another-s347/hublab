package User
import io.vertx.lang.scala.json.JsonObject

import scala.concurrent.Future

class UserHub extends Uri.Hub.HubTrait{
    override def apply(hubName: String, action: String, target: String, query: Map[String, Vector[String]], body: Option[JsonObject], identity: Session.Identity, sessionId: String): Future[JsonObject] = {
        action match { //TODO: use query or body ?
            case "isonline"=>{
                ???
            }
        }
    }
}