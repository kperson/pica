package com.pica.actor

import com.prystupa.JasmineSpec
import akka.actor.ActorSystem
import scala.concurrent.duration._
import java.lang.ProcessBuilder.Redirect
import com.pica.common.actor.BroadcastReceiver
import com.pica.common.actor.PicaBroadcastMessage
import com.pica.common.util.ByteSerializer
import org.scalatest.MustMatchers

class BroadcastReceiverSpec extends JasmineSpec with MustMatchers {

  describe("BroadcastReceiver loaner") {
        
    implicit val system = ActorSystem("sample-system")
    
    it("should receive new messages") {
      
      val expectedMessage = "hello world"
      val expectedTopic = "sample" 
        
      val receiver = new BroadcastReceiver((topic: String) => {
        topic must equal(expectedTopic)
        Some(PicaBroadcastMessage(topic, ByteSerializer.serialize(expectedMessage)))
      })
      
      val counter = ExecutionCount()
      receiver(expectedTopic, 50.milliseconds) { (msg, shutdown) => 
      	counter.execute()
      	msg must equal(expectedMessage)
      	shutdown()
      }
      
      Thread.sleep(150)
      system.shutdown()
      counter.executionCount must equal(1)
    
    }
    
  }
  
}