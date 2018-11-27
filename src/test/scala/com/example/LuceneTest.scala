package com.example

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import com.example.lucen.{FireObject, LuceneActor}
import com.example.lucen.LuceneActor.{DeleteFireObject, FindFireObjectsRequest, FoundFireObjectsResponse, UpdateOrCreateFireObject}
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


  "CRUD тест" in {
    val requester = TestProbe()
    val probe = TestProbe()
    val lucenActorRef = system.actorOf(LuceneActor.props(GlobalTesterConfig.LuceneDirectory))
    val fireObject = FireObject(1,"object1")
    val fireObjectUpdate = FireObject(1,"updated_object1")

//    lucenActorRef.tell(FindFireObjectsRequest("gid_firepoints","101"),probe.ref)
//    val response1: FoundFireObjectsResponse = probe.expectMsgType[FoundFireObjectsResponse]
//    response1.fireObjects.isEmpty should === (true)
//
//    lucenActorRef.tell(UpdateOrCreateFireObject(fireObject),probe.ref)
//    lucenActorRef.tell(FindFireObjectsRequest("gid_firepoints",fireObject.id.toString),probe.ref)
//
//    val response2: FoundFireObjectsResponse = probe.expectMsgType[FoundFireObjectsResponse]
//    response2.fireObjects.length should === (1)
//    response2.fireObjects.head.id should === (fireObject.id)
//    response2.fireObjects.head.nameobject should === (fireObject.nameobject)
//
//    lucenActorRef.tell(UpdateOrCreateFireObject(fireObjectUpdate),probe.ref)
//    lucenActorRef.tell(FindFireObjectsRequest("gid_firepoints",fireObject.id.toString),probe.ref)
//    val response3: FoundFireObjectsResponse = probe.expectMsgType[FoundFireObjectsResponse]
//    response3.fireObjects.length should === (1)
//    response3.fireObjects.head.id should === (fireObjectUpdate.id)
//    response3.fireObjects.head.nameobject should === (fireObjectUpdate.nameobject)
//
//    lucenActorRef.tell(DeleteFireObject(fireObject),probe.ref)
//    lucenActorRef.tell(FindFireObjectsRequest("gid_firepoints",fireObject.id.toString),probe.ref)
//    val response4: FoundFireObjectsResponse = probe.expectMsgType[FoundFireObjectsResponse]
//    response4.fireObjects.isEmpty should === (true)

    val fireObject1 = FireObject(1,"auto nameee #35")
    val fireObject2 = FireObject(2,"big nameee lalala")
    val fireObject3 = FireObject(3,"mega nameeelkddsds")

    lucenActorRef.tell(UpdateOrCreateFireObject(fireObject1),probe.ref)
    lucenActorRef.tell(UpdateOrCreateFireObject(fireObject2),probe.ref)
    lucenActorRef.tell(UpdateOrCreateFireObject(fireObject3),probe.ref)
    lucenActorRef.tell(FindFireObjectsRequest("nameobject","nameee*"),probe.ref)
    val response5: FoundFireObjectsResponse = probe.expectMsgType[FoundFireObjectsResponse]
    response5.fireObjects.length should === (3)

  }



}
