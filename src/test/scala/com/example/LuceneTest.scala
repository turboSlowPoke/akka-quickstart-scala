package com.example

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import com.example.lucen.LucenActor
import com.example.lucen.LucenActor.{FindFireObjectsRequest, FoundFireObjectsResponse}
import com.example.sample.Device
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class LuceneTest(_system: ActorSystem)
  extends TestKit(_system)
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll {
    //#test-classes

  def this() = this(ActorSystem("AkkaQuickstartSpec"))

  override def afterAll: Unit = {
      shutdown(system)
    }

  "return length reading even if device stops after answering" in {
    val requester = TestProbe()

    val probe = TestProbe()

    val lucenActorRef = system.actorOf(LucenActor.props(GlobalTesterConfig.LuceneDirectory))
    lucenActorRef.tell(FindFireObjectsRequest("id","1"),probe.ref)
    val response = probe.expectMsgType[FoundFireObjectsResponse]
    response.fireObjects.length.should(0)
  }

}
