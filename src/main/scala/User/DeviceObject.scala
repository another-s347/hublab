package User

import java.time.LocalDateTime

import io.vertx.lang.scala.json.{Json, JsonObject}
import io.vertx.scala.core.net.SocketAddress
import io.vertx.scala.ext.web.handler.sockjs.SockJSSocket

case class BaseDeviceInfo(name:String,typ:String){
    def toJson:JsonObject={
        val j=Json.emptyObj()
        j.put("DeviceName",name).put("Type",typ)
    }
}
object BaseDeviceInfo{
    def fromJson(j:JsonObject):BaseDeviceInfo={
       BaseDeviceInfo(j.getString("DeviceName"),j.getString("Type"))
    }
}

class DeviceObject(val deviceConfig: BaseDeviceInfo,user:UserObject) {
    var connection: Option[SockJSSocket] = None
    var remoteAddress: Option[SocketAddress] = None
    var lastActivityTime: LocalDateTime = LocalDateTime.now()

    def setConnection(newConnection: SockJSSocket): Boolean = {
        connection match {
            case None =>
                connection = Some(newConnection)
                println("set socket on address", newConnection.remoteAddress())
                true
            case Some(old) if old.remoteAddress() == newConnection.remoteAddress() =>
                println("replace new socket on address", old.remoteAddress())
                connection = Some(newConnection)
                true
            case Some(_) =>
                throw new NotImplementedError("setConnection with different address")
        }
    }

    def removeConnection(): Unit = {
        connection match {
            case None =>
            case Some(x) => {
                println("closing on address", connection.get.remoteAddress())
                x.write("disconnect")
                x.close()
                connection = None
            }
        }
    }
}