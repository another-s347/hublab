import io.vertx.scala.core.Vertx

package object Global {
    val vertx=Vertx.vertx()
    val eventBus=vertx.eventBus()
}
