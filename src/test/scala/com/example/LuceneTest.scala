package com.example

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import com.example.lucen.{FireObject, LuceneActor}
import com.example.lucen.LuceneActor.{FindFireObjectsRequest, FoundFireObjectsResponse, UpdateOrCreateFireObject}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.collection.immutable

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

  "Есди запросить несуществующий объект должен вернуть пустой список" in {
    val requester = TestProbe()
    val probe = TestProbe()
    val lucenActorRef = system.actorOf(LuceneActor.props(GlobalTesterConfig.LuceneDirectory))
    lucenActorRef.tell(FindFireObjectsRequest("gid_firepoints","101"),probe.ref)
    val response: FoundFireObjectsResponse = probe.expectMsgType[FoundFireObjectsResponse]
    response.fireObjects.isEmpty should === (true)
  }

  "Записать два объекта с одинаковым id, при запросе для данного id должен вернуть один объект" in {
    val requester = TestProbe()
    val probe = TestProbe()
    val lucenActorRef = system.actorOf(LuceneActor.props(GlobalTesterConfig.LuceneDirectory))
    val fireObject = FireObject(11,"name01")
    val fireObjectUpdate = FireObject(11,"updateName01")

    lucenActorRef.tell(UpdateOrCreateFireObject(fireObject),probe.ref)
    lucenActorRef.tell(UpdateOrCreateFireObject(fireObjectUpdate),probe.ref)
    lucenActorRef.tell(FindFireObjectsRequest("gid_firepoints",fireObject.id.toString),probe.ref)

    val response: FoundFireObjectsResponse = probe.expectMsgType[FoundFireObjectsResponse]
    response.fireObjects.length should === (1)
    response.fireObjects.head.id should === (fireObjectUpdate.id)
    response.fireObjects.head.nameobject should === (fireObjectUpdate.nameobject)

  }



}
