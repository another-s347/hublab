import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.lang.scala.json.JsonObject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}
import scala.util.control.Breaks.{break, breakable}

package object Config {
    def Verify(config: JsonObject): Boolean =
        throw new NotImplementedError()

    var dbLoggingDisable: Boolean = Default.dbLoggingDisable

    def Load(configFileLocation: String = ""): Future[Unit] = {
        val promise = Promise[Unit]()
        val p = if (configFileLocation == "") Config.configFileLocation else configFileLocation
        Global.vertx.fileSystem().readFileFuture(p) andThen {
            case Success(result: Buffer) =>
                Try(new JsonObject(result)) match {
                    case Success(r) =>
                        println("merging config file", configFileLocation)
                        Merge(r)
                    case Failure(c) =>
                        println(s"parse config file $configFileLocation", c)
                }
            case Failure(readFileCause) =>
                println(s"read config file $configFileLocation failed", readFileCause)
        } andThen {
            case _ =>
                MergeEnvironment()
                promise.success()
        }
        promise.future
    }

    def Merge(config: JsonObject): Unit = {
        if (config.containsKey("vertx-bridge")) {
            val vertxObject = config.getJsonObject("vertx-bridge")
            Try(vertxObject.getString("host")) match {
                case Success(null) =>
                case Success(x) =>
                    Config.VertxBridgeHost = x
                case Failure(c) =>
            }
            Try(vertxObject.getInteger("port")) match {
                case Success(null) =>
                case Success(x) =>
                    Config.VertxBridgePort = x
                case Failure(c) =>
            }
            Try(vertxObject.getJsonArray("inbound")) match {
                case Success(null) =>
                case Success(x: JsonArray) =>
                    var inboundArray = mutable.ArrayBuilder.make[(String, Int)]()
                    for (
                        index <- 0 until x.size()
                    ) {
                        breakable {
                            val inbould = x.getJsonObject(index)
                            val item: (String, Int) = (
                                Try(inbould.getString("address")) match {
                                    case Success(null) => break()
                                    case Success(x) => x
                                    case Failure(c) => break()
                                },
                                Try(inbould.getInteger("type")) match {
                                    case Success(null) => 1
                                    case Success(x) => x
                                    case Failure(c) => 1
                                }
                            )
                            inboundArray += item
                        }
                    }
                    VertxBridgeInboundOptions = inboundArray.result()
                case Failure(c) =>
            }
            Try(vertxObject.getJsonArray("outbound")) match {
                case Success(null) =>
                case Success(x: JsonArray) =>
                    var outboundArray = mutable.ArrayBuilder.make[(String, Int)]()
                    for (
                        index <- 0 until x.size()
                    ) {
                        breakable {
                            val inbould = x.getJsonObject(index)
                            val item: (String, Int) = (
                                Try(inbould.getString("address")) match {
                                    case Success(null) => break()
                                    case Success(x) => x
                                    case Failure(c) => break()
                                },
                                Try(inbould.getInteger("type")) match {
                                    case Success(null) => 1
                                    case Success(x) => x
                                    case Failure(c) => 1
                                }
                            )
                            outboundArray += item
                        }
                    }
                    VertxBridgeOutboundOptions = outboundArray.result()
                case Failure(c) =>
            }
        }
        if (config.containsKey("database")) {
            val databaseObject = config.getJsonObject("database")
            Try(databaseObject.getString("host")) match {
                case Success(null) =>
                case Success(x) =>
                    Config.dbHost = x
                case Failure(c) =>
            }
            Try(databaseObject.getInteger("port")) match {
                case Success(null) =>
                case Success(x) =>
                    Config.dbPort = x
                case Failure(c) =>
            }
            Try(databaseObject.getString("name")) match {
                case Success(null) =>
                case Success(x) =>
                    Config.dbName = x
                case Failure(c) =>
            }
            Try(databaseObject.getBoolean("disable-logging")) match {
                case Success(null) =>
                case Success(x) =>
                    Config.dbLoggingDisable = x
                case Failure(c) =>
            }
            Try(databaseObject.getLong("reconnect-delay")) match {
                case Success(null) =>
                case Success(x) =>
                    Config.dbReconnectDelay = x
                case Failure(c) =>
            }
            Try(databaseObject.getLong("query-timeout")) match {
                case Success(null) =>
                case Success(x) =>
                    Config.dbQueryTimeout = x
                case Failure(c) =>
            }
        }
        if (config.containsKey("sockjs")) {
            val sockjsObject = config.getJsonObject("sockjs")
            Try(sockjsObject.getString("host")) match {
                case Success(null) =>
                case Success(x) =>
                    Config.httpHost = x
                case Failure(c) =>
            }
            Try(sockjsObject.getInteger("port")) match {
                case Success(null) =>
                case Success(x) =>
                    Config.httpPort = x
                case Failure(c) =>
            }
            Try(sockjsObject.getInteger("heartbeat-interval")) match {
                case Success(null) =>
                case Success(x) =>
                    Config.sockjsHeartbeatInterval = x
                case Failure(c) =>
            }
        }
    }

    def MergeEnvironment(): Unit = {
        if (sys.env.contains("")) {

        }
    }

    //Vertx Bridge Config
    var VertxBridgeHost: String = Default.VertxBridgeHost
    var VertxBridgePort: Int = Default.VertxBridgePort
    var VertxBridgeInboundOptions: Array[(String, Int)] = Default.VertxBridgeInboundOptions.clone()
    var VertxBridgeOutboundOptions: Array[(String, Int)] = Default.VertxBridgeOutboundOptions.clone()
    //MongoDB Config
    var dbHost: String = Default.dbHost
    var dbPort: Int = Default.dbPort
    var dbName: String = Default.dbName
    var dbReconnectDelay: Long = Default.dbReconnectDelay //milliseconds
    var dbQueryTimeout: Long = Default.dbQueryTimeout //milliseconds

    //Default Config
    object Default {
        var VertxBridgeHost = "0.0.0.0"
        var VertxBridgePort = 8081
        var VertxBridgeInboundOptions = Array(
            ("notification.push", 0),
            ("interface.*", 1),
            ("registerhub",0)
        )
        var VertxBridgeOutboundOptions = Array(
            ("notification.push", 0),
            ("interface.*", 1)
        )

        var dbHost = "localhost"
        var dbPort = 27017
        var dbName = "NotificationHub"
        var dbLoggingDisable = true
        var dbReconnectDelay = 5000
        var dbQueryTimeout = 1000

        var httpHost = "localhost"
        var httpPort = 8080
        var sockjsHeartbeatInterval = 2000

        var configFileLocation = ""
    }

    //Sockjs Server Config
    var httpPort: Int = Default.httpPort
    var httpHost: String = Default.httpHost
    var sockjsHeartbeatInterval: Int = Default.sockjsHeartbeatInterval
    //
    var configFileLocation: String = Default.configFileLocation
}
