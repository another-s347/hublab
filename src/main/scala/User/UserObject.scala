package User

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import User.UserManager.Credential
import io.vertx.lang.scala.json.{Json, JsonObject}
import io.vertx.scala.ext.web.handler.sockjs.SockJSSocket

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.{Failure, Success}

case class BaseUserInfo(name: String, devices: List[BaseDeviceInfo], defaultDeviceName: String, credential: Credential){
    def toJson:JsonObject={
        val devicesList=Json.emptyArr()
        devices.foreach(b=>{
            devicesList.add(b.toJson)
        })
        Json.emptyObj().put("Username",name).put("Credential",credential.toJson).put("DefaultDeviceName","host").put("Devices",devicesList)
    }
}
object BaseUserInfo{
    def fromJson(j:JsonObject):BaseUserInfo={
        val mutableList=new mutable.MutableList[BaseDeviceInfo]
        for(i<-0 until j.getJsonArray("Devices").size()){
            mutableList+=BaseDeviceInfo.fromJson(j.getJsonArray("Devices").getJsonObject(i))
        }
        BaseUserInfo(j.getString("Username"),mutableList.toList,j.getString("DefaultDeviceName"),Credential.fromJson(j.getJsonObject("Credential")))
    }
}

class UserObject(val baseUserInfo: BaseUserInfo) {
    type DeviceName=String
    type DeviceRuntimeID=String
    private var _devices = mutable.HashMap[DeviceName, DeviceObject]()
    val deviceByID: mutable.HashMap[DeviceRuntimeID, DeviceObject] = mutable.HashMap[DeviceRuntimeID, DeviceObject]()
    baseUserInfo.devices.foreach(device => {
        _devices += (device.name -> new DeviceObject(device, this))
    })

    def SetConnection(deviceRuntimeID: DeviceRuntimeID,connection:SockJSSocket): Boolean =
        if(deviceByID(deviceRuntimeID).setConnection(connection)){
            val device=deviceByID(deviceRuntimeID)
            Notification.ResendToUserDevice(user = baseUserInfo.name,device.deviceConfig.name) onComplete {
                case Success(value)=>
                    println("success resend to device")
                case Failure(exception)=>
                    print(s"fail on resend:${exception.getLocalizedMessage}")
            }
            true
        }
        else false

    def UnsetConnection(deviceRuntimeID: DeviceRuntimeID): Unit =
        deviceByID(deviceRuntimeID).removeConnection()

    def registerDevice(device: String,sessionIDSpecify:Option[String]=None): DeviceRuntimeID = {
        val sessionID = sessionIDSpecify.getOrElse(UUID.randomUUID().toString)
        deviceByID += (sessionID -> _devices(device))
        sessionID
    }

    def unregisterDevice(deviceRuntimeID: DeviceRuntimeID): Unit =
        deviceByID.-(deviceRuntimeID)
}

