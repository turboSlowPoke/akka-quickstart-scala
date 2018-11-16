package com.example.start_stop

import akka.actor.{Actor, Props}

class StartStopActor1 extends Actor{

  override def preStart(): Unit = {
    println("first started")
    context.actorOf(StartStopActor2.props,"second")
  }

  override def postStop(): Unit = {
    println("first stopped")
  }

  override def receive: Receive = {
    case "stop" => context.stop(self)
  }
}

object StartStopActor1 {
  def props = Props(new StartStopActor1)
}
