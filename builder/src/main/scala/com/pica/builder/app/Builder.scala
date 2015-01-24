package com.pica.builder.app

import java.io.File
import java.io.FileOutputStream
import scala.concurrent.Future
import com.pica.common.model.BuildInstructions
import com.pica.common.model.BuildRequest
import com.pica.common.util.MyProps
import akka.actor._
import akka.io.IO
import spray.can.Http
import spray.http._
import spray.http.HttpMethods.GET
import spray.http.Uri.apply
import scala.concurrent._
import akka.pattern.pipe
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Queue
import scala.concurrent.duration._
import spray.httpx.SprayJsonSupport
import spray.client.pipelining._
import com.pica.common.model.TopicMessage._
import com.pica.common.model.TopicMessageEvent
import spray.json.{ JsonFormat, DefaultJsonProtocol }
import spray.can.Http
import spray.httpx.SprayJsonSupport
import spray.client.pipelining._
import spray.util._
import com.pica.common.model.TopicMessage
import com.codahale.jerkson.Json.generate
import scala.util.{ Success, Failure }
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.event.Logging
import akka.io.IO
import spray.json.{ JsonFormat, DefaultJsonProtocol }
import spray.can.Http
import spray.httpx.SprayJsonSupport
import spray.client.pipelining._
import spray.util._
import com.pica.common.model.ConsoleOutput

case class DownloadComplete(directory: File, instructions: BuildInstructions)

object Builder extends App {
  
  implicit val system = ActorSystem("system")

  def buildDocker(instructions: LocalBuildInstructions, logger: Option[ActorRef] = None, remove: Boolean = false): Future[Unit] = {
    println("building job: " + instructions.buildId)
    val execute = DockerBuild(instructions, remove) { (line, isStandard) =>
      logger.map { l => l ! ConsoleOutput(line, isStandard) }
    }
    execute
  }

  import system.dispatcher

  class BuildDownloader(instructions: BuildInstructions, foreman: ActorRef) extends Actor {
    val io = IO(Http)
    io ! Http.Connect(MyProps("SERVER_HOST").get, port = MyProps("SERVER_PORT").get.toInt)

    val file = new File(MyProps("BUILD_DIR").get, instructions.buildId + ".tar.gz")
    val stream = new FileOutputStream(file)

    def receive = {
      case _: Http.Connected =>
        sender ! HttpRequest(GET, "/code/" + instructions.buildId)
      case ChunkedResponseStart(res) =>
        println("downloading code for build: " + instructions.buildId)
      case MessageChunk(body, ext) =>
        stream.write(body.toByteArray)
      case ChunkedMessageEnd(ext, trailer) =>
        println("download complete: " + instructions.buildId)
        stream.close()
        Runtime.getRuntime().exec("tar -xzf " + file.getName(), null, file.getParentFile())
        context.stop(io)
        foreman ! DownloadComplete(file.getParentFile(), instructions)
      case x =>
        println("unknow message received: " + x.getClass.getName)
    }
  }

  val buffer: Queue[ConsoleOutput] = Queue()

  
  object Logger {

    
    import SprayJsonSupport._
    
    var lastMessage: Option[ConsoleMessage] = None

    def log(topic: String, msg: ConsoleOutput) {
      val cMessage = ConsoleMessage(topic, msg)
      lastMessage match {
        case Some(x) => {
          x.next(cMessage)
          lastMessage = Some(cMessage)
        }
        case _ => {
          lastMessage = Some(cMessage)
          cMessage.log()
        }
      }
    }

    val pipeline = sendReceive ~> unmarshal[TopicMessageEvent]

    
    def logTopicMessage(topic: String, output: TopicMessage) = {
        val host = MyProps("SERVER_HOST").get
        val port = MyProps("SERVER_PORT").get
        
        val endpoint = s"http://$host:$port/topic/$topic" 
        pipeline(Post(endpoint, output))
    }
    
    case class ConsoleMessage(topic: String, output: ConsoleOutput) {
      
      private var nextMessage: Option[ConsoleMessage] = None
      private var complete: Boolean = false

      def next(msg: ConsoleMessage) = {
        nextMessage = Some(msg)
        if (complete) {
          msg.log()
        }
      }

      def log() {
        val msg = TopicMessage("console", generate(output))
                
        logTopicMessage(topic, msg).onComplete {
          case _ =>
            complete = true
            nextMessage.map { _.log() }
        }
      }
    }

  }

  class Foreman extends Actor {

    var finder = context.actorOf(Props(classOf[BuildFinder], self))
    var downloader: Option[ActorRef] = None
    var logger: Option[ActorRef] = None

    def receive = {
      case instructions: BuildInstructions =>
        context.stop(finder)
        downloader = Some(context.actorOf(Props(classOf[BuildDownloader], instructions, self)))
      case DownloadComplete(file, instructions) =>
        downloader.map { d => context.stop(d) }

        val localInstructions = LocalBuildInstructions(instructions.buildId, file.getAbsolutePath(), instructions.dockerFileLocation, instructions.imageName, instructions.registryUrl)
      // buildDocker(localInstructions, logger, true)
    }
  }

  class BuildFinder(foreman: ActorRef) extends Actor {

    val io = IO(Http)

    io ! Http.Connect(MyProps("SERVER_HOST").get, port = MyProps("SERVER_PORT").get.toInt)

    def parseJSON[T](data: Array[Byte])(implicit mf: Manifest[T]) = com.codahale.jerkson.Json.parse[T](data)

    def receive = {
      case _: Http.Connected =>
        sender ! HttpRequest(GET, "/build")
      case ChunkedResponseStart(res) =>
        println("build connection initialized")
      case MessageChunk(body, ext) =>
        parseJSON[BuildRequest](body.toByteArray).instructions.map { instructions =>
          context.stop(io)
          foreman ! instructions
        }
      case ChunkedMessageEnd(ext, trailer) =>
        println("we are using persistent http connections, this message should never been sent")
        system.shutdown()
        System.exit(1)
      case x =>
        system.shutdown()
        println("unknown message received: " + x.getClass.getName)
        System.exit(1)
    }
  }

  //system.actorOf(Props(classOf[Foreman]))

  for (z <- 0 to 50000) {
    Logger.log("mytopic", ConsoleOutput(z.toString, true))
  }
  


}