package com.example.start_stop

import akka.actor.ActorSystem

object Main extends App {
  val system = ActorSystem("system")
  val actor1 = system.actorOf(StartStopActor1.props,"actor1")
  actor1 ! "stop"
}
