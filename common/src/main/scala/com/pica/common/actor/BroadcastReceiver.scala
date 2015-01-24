package com.pica.common.actor

import akka.actor.Actor
import akka.actor.FSM
import akka.actor.ActorRef
import scala.concurrent.duration._
import akka.actor.Props
import akka.actor.ActorContext
import com.pica.common.util.ByteSerializer

sealed trait State
case object Idle extends State
case object Active extends State
case object Tick
case class ActionTarget(action: BroadcastReceiveTye.BroadcastReceiveExeuction)

package object BroadcastReceiveTye {
  type BroadcastReceiveAction = (String) => Option[PicaBroadcastMessage]
  type BroadcastReceiveExeuction = (Any, () => Unit) => Unit
}

class BroadcastReceiverActor(dequeue: BroadcastReceiveTye.BroadcastReceiveAction, topic: String, pollFrequency: FiniteDuration) extends Actor with FSM[State, Option[BroadcastReceiveTye.BroadcastReceiveExeuction]] {
  
  startWith(Idle, None)

  when (Idle) {
    case Event(ActionTarget(ref), None) => {
      goto(Active) using Some(ref)
    }
    case _ => {
      stay()
    }
  }
  
  def stopSelf() {
    context.stop(self)
  }
  
  when(Active) {
    case Event(Tick, Some(action)) => {
      dequeue(topic) match {
        case Some(msg) => {
          val message = ByteSerializer.deserialize(msg.messageBody)
          action(message, stopSelf)
        }
        case _ => { 
          /*Nothing to do*/
        }
      }
      stay()
    }
    case Event(None, _) => {
      goto(Idle)
    }
  }
  
  onTransition {
    
    case Idle -> Active => setTimer("moreRequests", Tick, pollFrequency, true)
    case Active -> Idle => cancelTimer("moreRequests")
  }
 
  initialize()
  
}

class BroadcastReceiver(dequeue: BroadcastReceiveTye.BroadcastReceiveAction)(implicit system: akka.actor.ActorSystem) {
  
  def apply(topic: String, pollFrequency: FiniteDuration)(code: BroadcastReceiveTye.BroadcastReceiveExeuction) {
    BroadcastReceiver.using(dequeue)(pollFrequency)(topic)(code)
  }
  
}

object BroadcastReceiver {
  
  def using(dequeue: BroadcastReceiveTye.BroadcastReceiveAction)(pollFrequency: FiniteDuration)(topic: String)(code: BroadcastReceiveTye.BroadcastReceiveExeuction)(implicit system: akka.actor.ActorSystem) {
    val client = system.actorOf(Props(new BroadcastReceiverActor(dequeue, topic, pollFrequency)))
    client ! ActionTarget(code)
  }
}