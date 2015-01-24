package com.pica.controller.di

import com.pica.common.util.MyProps
import java.io.File

//import com.pica.controller.mongo.MongoMessageBroker

trait ControllerDefaults {

  //def broadcastEnqueue = MongoMessageBroker.enqueue _
  //def broadcastDequeue = MongoMessageBroker.dequeue _
  
  def controllerBuildDirectory(codeId: String): File = new File(MyProps("CODE_DIR").get, codeId)
  
}