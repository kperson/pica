package com.pica.actor

case class ExecutionCount(var count: Int = 0) {
  
  def execute() {
    count = count + 1
  }
  
  def executionCount = {
    count
  }
  
}
