package com.example.failure_hadling

import akka.actor.{Actor, Props}

class SupervisedActor extends Actor{
  override def preStart(): Unit = println("supervised actor started")
  override def postStop(): Unit = println("supervised actor stopped")
  override def receive: Receive = {
    case "fail" ⇒
      println("supervised actor fails now")
      throw new Exception("I failed!")
  }
}
object SupervisedActor {
  def props = Props(new SupervisedActor)
}
