package com.pica.controller.app

import scala.collection.mutable.Queue
import scala.concurrent.duration.DurationInt

import org.json4s.DefaultFormats

import com.codahale.jerkson.Json.generate
import com.pica.common.model.BuildInstructions
import com.pica.common.model.BuildRequest
import com.pica.controller.di.Defaults.controllerBuildDirectory

import akka.actor.Actor
import akka.actor.Props
import akka.actor.actorRef2Scala
import spray.can.Http
import spray.http._
import spray.http.MediaTypes._
import spray.httpx.Json4sJacksonSupport
import spray.httpx.marshalling.ToResponseMarshallable.isMarshallable
import spray.routing.Directive.pimpApply
import spray.routing.HttpService
import spray.routing.RequestContext


case class Tick(ct: Int)

trait BuildService extends HttpService with Json4sJacksonSupport with PushSupport  {
  
  private lazy val buildQueue: Queue[BuildInstructions] = Queue()

  implicit def executionContext = actorRefFactory.dispatcher
   
  def json4sJacksonFormats = DefaultFormats
      
  def pushBuildInstructions(ctx: RequestContext): Unit =
    actorRefFactory.actorOf {
      Props {
        new Actor  {
          // we use the successful sending of a chunk as trigger for scheduling the next chunk
          val responseStart = HttpResponse(entity = HttpEntity(`application/json`,  generate(BuildRequest("ok", None))))
          ctx.responder ! ChunkedResponseStart(responseStart).withAck(Tick(0))

          def receive = {
            case Tick(ct) =>
              in(3.seconds) {
                val instructions = generate(BuildRequest("ok", buildQueue.dequeueFirst(_ => true)))
                val nextChunk = MessageChunk(instructions)
                ctx.responder ! nextChunk.withAck(Tick(ct + 1))
              }
            case ev: Http.ConnectionClosed =>
              println("connection closed")
          }
        }
      }
    }
  
  val buildRoute = {
    get {
      path("build") {
        pushBuildInstructions
      } ~
      pathPrefix("code" / Segment) { codeId =>
        val codeFile = controllerBuildDirectory(codeId + ".tar.gz") 
        getFromFile(codeFile)
      }
    } ~
    post {
      path("build") {
        entity(as[BuildInstructions]) { build =>
          complete { 
            buildQueue.enqueue(build)
            build
          }
        }
      }
    }
  }
  
}