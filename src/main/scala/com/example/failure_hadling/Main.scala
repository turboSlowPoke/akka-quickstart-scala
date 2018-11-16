package com.example.failure_hadling

import akka.actor.ActorSystem

object Main extends App {
  val system = ActorSystem("system")
  val supervisor = system.actorOf(SupervisingActor.props,"supervising-actor")
  supervisor ! "failChild"
}
