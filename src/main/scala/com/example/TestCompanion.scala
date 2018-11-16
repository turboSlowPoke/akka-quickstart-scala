package com.example

import TestCompanion._

object TestCompanion {
   private val objectVal="objectVal"
    def objectMethod(): String ={"objectMethod"}
}

 class TestCompanion(p1:String) {
  private val classVal="classVal"
  private def classMethod():String ={"classMethod"}
  def check= objectMethod()

}
