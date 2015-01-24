package com.pica.common.actor

import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.Props
import com.pica.common.util.ByteSerializer

package object BroadcastSendType {
  type BroadcastSendAction = (PicaBroadcastMessage) => Unit
}

class BroadcastSenderActor(enqueue: BroadcastSendType.BroadcastSendAction, topic: String) extends Actor {  
  
   def receive = {
     case msg: Any => {
       enqueue(PicaBroadcastMessage(topic, ByteSerializer.serialize(msg)))
     }
     case _ => {
       println("unable to send message")
     }
   }

}

case class BroadcastSender(enqueue: BroadcastSendType.BroadcastSendAction, topic: String)(implicit system: akka.actor.ActorSystem) {
    
  val client = system.actorOf(Props(new BroadcastSenderActor(enqueue, topic)))

  def apply(code:(ActorRef) => Unit) {
    code(client)
  }
  
  def close() {
    system.stop(client)    
  }
  
}

object BroadcastSender {
    
  def using(enqueue: BroadcastSendType.BroadcastSendAction)(topic: String)(code:(ActorRef) => Unit)(implicit system: akka.actor.ActorSystem) {
    val client = system.actorOf(Props(new BroadcastSenderActor(enqueue, topic)))
    println(client)
    code(client)
    //system.stop(client)
  }
  
}