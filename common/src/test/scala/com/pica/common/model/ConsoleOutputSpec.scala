package com.pica.common.model

import com.prystupa.JasmineSpec
import org.scalatest.MustMatchers

class ConsoleOutputSpec extends JasmineSpec with MustMatchers {
  
  describe("ConsoleOutput") {
    
    describe("unapply") {

      val consoleMessage = ConsoleOutput("hello", false)
      
      val topicMessage = TopicMessage("console", """{"line": "hello", "isStandard": false}""")
      val topicMessageNoMatch = TopicMessage("console2", """{"line": "hello", "isStandard": false}""")
      
      it("should extract a ConsoleOutput if the messageType is 'console'") {
               
        //Extract
        val rs = topicMessage match {
          case msg @ ConsoleOutput(log) => Some(log)
          case _ => None
        }
        rs must equal(Some(consoleMessage))
        
      }
      
      it("should not match a TopicMessage if messageType is not 'console'") {
        //only for testing purpose, see above for code extractor patterns 
        //http://danielwestheide.com/blog/2012/11/21/the-neophytes-guide-to-scala-part-1-extractors.html
        ConsoleOutput.unapply(topicMessageNoMatch) must equal(None)
        
      }
      
    }
    
  }

}