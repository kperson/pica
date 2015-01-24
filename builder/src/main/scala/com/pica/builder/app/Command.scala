package com.pica.builder.app

import scala.concurrent._
import ExecutionContext.Implicits.global
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.File

object Command {
  
  def apply(command: String, directory: Option[String] = None, ignoreErrors: Boolean = false)(code:(String, Boolean) => Unit) : Future[Unit] = {
    val process = directory match {
      case Some(dir) => Runtime.getRuntime().exec(command, null, new File(dir))
      case _ => Runtime.getRuntime().exec(command)
    }    
    def streamOutput(reader: BufferedReader, isStandardOut: Boolean) {
      Stream.continually(reader.readLine()).takeWhile(a => a != null).foreach(a => {
        code(a, isStandardOut)
      }) 
    }
    
    val std = future {
      val stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()))
      streamOutput(stdInput, true)
    }
    
    val error = future {   
      val stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))
      streamOutput(stdError, false)
    }
    
   val exitCode = process.waitFor()
   
   Future.sequence(List(std, error)).map { x =>   
     if(exitCode == 0 || ignoreErrors) {
       Unit
     }
     else {
       throw new Exception("command was not completed successfully")
     }
   }
   
  } 
 
}