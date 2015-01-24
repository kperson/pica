package com.pica.actor

import com.prystupa.JasmineSpec
import org.scalatest.matchers.ShouldMatchers
import akka.actor.ActorSystem
import com.pica.common.actor.BroadcastSender


class BroadcastSenderSpec extends JasmineSpec with ShouldMatchers  {
  
  describe("BroadcastSender loaner") {
    implicit val system = ActorSystem("sample-system")

    it("should trigger an enqueue if a message is sent") {
      val counter = ExecutionCount()
      val sender = BroadcastSender(message => {  
        counter.execute()
      }, "sample") 
    
      sender { client => 
        client ! "hello"
      }
      
      Thread.sleep(150)
      counter.executionCount should equal(1)
    }
 
    afterEach {
      system.shutdown()
    }
  }
  
}