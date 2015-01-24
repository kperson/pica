package com.pica.controller.app

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

import org.json4s.DefaultFormats

import com.codahale.jerkson.Json.generate
import com.pica.common.model._

import akka.actor.Actor
import akka.actor.Props
import spray.can.Http
import spray.http._
import spray.http.MediaTypes._
import spray.httpx.Json4sJacksonSupport
import spray.routing.HttpService
import spray.routing.RequestContext
import spray.util.actorSystem

case class TopicMessageAck(createdAt: Long)
case class TopicMessageEventWrapper(topicMessageEvent: TopicMessageEvent, count: Long)

trait TopicService extends HttpService with Json4sJacksonSupport with PushSupport {
  
  private lazy val messages: ListBuffer[TopicMessageEventWrapper] = ListBuffer()
  
  def json4sJacksonFormats = DefaultFormats
  
  private implicit def executionContext = actorRefFactory.dispatcher
    
  var count = 0L
  
  def nexLong() = {
    if(count == Long.MaxValue) {
      count = 0
    }
    else {
      count = count + 1
    }
    count
  }
  
  val topicRoute = {
    get {
       pathPrefix("topic" / Segment) { topic => 
         streamTopic(topic)
       }
    } ~
    post {
       pathPrefix("topic" / Segment) { topic => 
         entity(as[TopicMessage]) { message =>
           val event = TopicMessageEvent(message, topic, System.currentTimeMillis)
           messages.synchronized {
             messages.append(TopicMessageEventWrapper(event, nexLong()))
           }
           complete {
             event
           }
         }
       }
    }
  }
  
 var nextClean = System.currentTimeMillis + (3 * 1000)
 
  def removeOldMessages() {
     if(System.currentTimeMillis >  nextClean) {
       nextClean = System.currentTimeMillis + (3 * 1000)
        for(i <- (0 until messages.length).reverse) {
          if(messages(i).topicMessageEvent.createdAt < System.currentTimeMillis - 5 * 1000) {
             messages.remove(i)
          }
        }
      }
  }
  
  
  def fetchTopics(topic: String, time: Long) = {
    messages.synchronized {
      val msgs = messages
      .filter(a => a.topicMessageEvent.createdAt > time && a.topicMessageEvent.topic == topic)
      .sortWith((a, b) => a.count < b.count)
      .sortWith((a, b) => a.topicMessageEvent.createdAt < b.topicMessageEvent.createdAt)
      val ackTime: Long = msgs.lastOption.map(a => a.topicMessageEvent.createdAt).getOrElse(time)
      val rs = (msgs.map(_.topicMessageEvent), ackTime)
      removeOldMessages()
      rs
    }
  }
  
  def streamTopic(topic: String)(ctx: RequestContext): Unit = {
    actorRefFactory.actorOf {
      Props {
        new Actor  {
          // we use the successful sending of a chunk as trigger for scheduling the next chunk
          val responseStart = HttpResponse(entity = HttpEntity(`application/json`,  generate(Nil)))
          ctx.responder ! ChunkedResponseStart(responseStart).withAck(TopicMessageAck(System.currentTimeMillis))

          def receive = {
            case TopicMessageAck(time) =>
              in(25.milliseconds) {
                val (instructions, ackTime) = fetchTopics(topic, time)
                if(!instructions.isEmpty) {
                  val nextChunk = MessageChunk(generate(instructions))
                  ctx.responder ! nextChunk.withAck(TopicMessageAck(ackTime))
                }
                else {
                  self ! TopicMessageAck(ackTime)
                }
              }
            case ev: Http.ConnectionClosed =>
              println("connection closed: " + topic)
              context.stop(self)
          }
        }
      }
    }
  }
}