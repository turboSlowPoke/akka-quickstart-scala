package com.example.lucen

import java.util.Date

import org.apache.lucene.document.{Document, Field, StringField, TextField}

case class FireObject(
                       var id: Int = 0,
                       var nameobject: String = "",
                       var typeobject: String = "",
                       var rang: String = "1",
                       var prim: String = "",
                       var rangmodifiers: String = "",
                       var addteches: String = "",
                       var isenabled: Int = 0,
                       var oldoid: Int = 0,
                       var x: Double = 0,
                       var y: Double = 0,
                       var address: String = null,
                       var street: String = "",
                       var house: String = "",
                       var updater: String = "",
                       var updwhen: Date = null
                     )

object FireObject{
  def toLuceneDoc(fireObject: FireObject):Document={
    val doc = new Document
    doc.add(new StringField("gid_firepoints", fireObject.id.toString, Field.Store.YES))
    doc.add(new StringField("geomX", fireObject.x.toString, Field.Store.YES))
    doc.add(new StringField("geomY", fireObject.y.toString, Field.Store.YES))
    doc.add(new TextField("nameobject", fireObject.nameobject, Field.Store.YES))
    doc.add(new StringField("typeobject", fireObject.typeobject, Field.Store.YES))
    doc.add(new StringField("rang", fireObject.rang, Field.Store.YES))
    doc.add(new StringField("prim", fireObject.prim, Field.Store.YES))
    doc.add(new StringField("rangmodifiers", fireObject.rangmodifiers, Field.Store.YES))
    doc.add(new StringField("addteches", fireObject.addteches, Field.Store.YES))
    doc
  }

  def fireObjectFromLucenDoc(doc:Document): FireObject =FireObject(
    doc.get("gid_firepoints").toInt,
    doc.get("geomX"),
    doc.get("geomY"),
    doc.get("nameobject"),
    doc.get("typeobject"),
    doc.get("rang"),
    doc.get("prim"),
    doc.get("rangmodifiers").toInt,
    doc.get("addteches").toInt
  )
}