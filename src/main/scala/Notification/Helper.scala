package Notification

package Message

import io.vertx.lang.scala.json.{Json, JsonArray}

object MessageHelper{
    implicit class ArrayMessageExt[T <: EventMessage[T]](val self: Array[T])(implicit m: scala.reflect.ClassTag[T]) {
        def toJson(): JsonArray = {
            val ret = Json.emptyArr()
            self.foreach(b =>
                ret.add(b.toJson())
            )
            ret
        }
    }

}

object ArrayHelper {

    implicit class ArrayExt[T](val self: Array[T]) {
        def toJson(): JsonArray = {
            val ret = Json.emptyArr()
            self.foreach(b =>
                ret.add(b)
            )
            ret
        }
    }

}