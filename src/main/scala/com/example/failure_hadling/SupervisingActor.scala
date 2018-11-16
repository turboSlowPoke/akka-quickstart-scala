package com.example.failure_hadling

import akka.actor.{Actor, Props}



class SupervisingActor extends Actor{

  val child = context.actorOf(SupervisedActor.props,"supervised-actor")

  override def receive: Receive = {
    case "failChild" => child ! "fail"
  }
}

object SupervisingActor {
  def props = Props(new SupervisingActor)
}
