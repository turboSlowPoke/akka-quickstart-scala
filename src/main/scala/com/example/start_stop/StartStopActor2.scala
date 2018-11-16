package com.example.start_stop

import akka.actor.{Actor, Props}

class StartStopActor2 extends Actor {
  override def preStart(): Unit = println("second started")
  override def postStop(): Unit = println("second stopped")
  override def receive: Receive = Actor.emptyBehavior
}

object StartStopActor2 {
  def props: Props =
    Props(new StartStopActor2)
}
