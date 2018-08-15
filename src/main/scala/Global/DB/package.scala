package Global

import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.{Level, Logger}
import scala.concurrent.ExecutionContext.Implicits.global
import io.vertx.lang.scala.json.{Json, JsonObject}
import io.vertx.scala.ext.mongo.{MongoClient, MongoClientUpdateResult, UpdateOptions}

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

package object DB {
    private val connectionConfig = Json.emptyObj()
        .put("host", Config.dbHost)
        .put("port", Config.dbPort)
        .put("db_name", Config.dbName)
        .put("serverSelectionTimeoutMS", Config.dbQueryTimeout)
    private var client: Option[MongoClient] = None
    private val reconnecting: AtomicBoolean = new AtomicBoolean(false)
    if (true) {
        val mongoLogger: Logger = Logger.getLogger("org.mongodb.driver")
        mongoLogger.setLevel(Level.SEVERE)
    }

    def Reconnect(): Unit = {
        if (reconnecting.get)
            return
        reconnecting.set(true)
        Global.vertx.setTimer(Config.dbReconnectDelay, _ => {
            Load() onComplete {
                case Success(_) => println("reconnect success")
                case Failure(_) =>
            }
        })
    }

    def Load(): Future[Unit] = {
        val promise: Promise[Unit] = Promise()
        client = Some(MongoClient.createShared(Global.vertx, connectionConfig))
        client.get.getCollectionsFuture() andThen {
            case Success(_) =>
                reconnecting.set(false)
                println("connect MongoDB success")
                Global.eventBus.publish("db.connected", Json.emptyObj())
                promise.success()
            case Failure(c) =>
                client = None
                println("connect MongoDB fail", c)
                println("Host", Config.dbHost)
                println("Port", Config.dbPort)
                println(s"Retry in ${Config.dbReconnectDelay} milliseconds")
                reconnecting.set(false)
                Reconnect()
                promise.failure(c)
        }
        promise.future
    }

    def go[A](m: MongoClient => Future[A]): Future[A] =
        if (client.isDefined) {
            m(client.get)
        }
        else {
            Reconnect()
            Future.failed[A](new Exception("not connected"))
        }
}
