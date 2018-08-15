import io.vertx.lang.scala.json.JsonObject

package object Data {
    case class File(fileName:String, prefix:String, data:JsonObject, user:String)
}
