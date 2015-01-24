package com.pica.builder.app

import java.io.File
import scala.concurrent._
import ExecutionContext.Implicits.global
import org.apache.commons.io.FileUtils

object DockerBuild {
  
  def apply(instructions: LocalBuildInstructions, remove: Boolean)(code:(String, Boolean) => Unit) : Future[Unit] = {
    val directory = instructions.directory
    val dockerFileLocation = instructions.dockerFileLocation
    val buildId = instructions.buildId
    val imageName = instructions.imageName
    val registryUrl = instructions.registryUrl
    
    val buildCommand = s"docker build --rm=true --no-cache -t $registryUrl/$imageName:$buildId $dockerFileLocation"    
    val pushCommand = s"docker push $registryUrl/$imageName"
    val removeRemoteCommand = s"docker rmi $registryUrl/$imageName:$buildId"
        
    Command(buildCommand, Some(directory))(code).flatMap {
      case _ => Command(pushCommand)(code)
    }.flatMap {
      case _ => Command(removeRemoteCommand, None, true)(code) 
    }.map {
      case _ => 
        if(remove) {
          FileUtils.deleteDirectory(new File(directory))
        }
    }
  }
  
}