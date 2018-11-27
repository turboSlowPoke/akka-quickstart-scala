package com.example.lucen

import java.io.IOException
import java.nio.file.{Path, Paths}

import akka.actor.{Actor, ActorLogging, Props}
import com.example.lucen.LuceneActor.{DeleteFireObject, FindFireObjectsRequest, FoundFireObjectsResponse, UpdateOrCreateFireObject}
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document._
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig, Term}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search._
import org.apache.lucene.store.FSDirectory

import scala.util.Try
/**
  * Актор для записи, обновления, поиска и удаления fireObject в apache Lucene
   */
class LuceneActor(luceneDir:String) extends Actor with  ActorLogging{
  private val path: Path = Paths.get(luceneDir) //здесь будем писать и читать
  private val analyzer: StandardAnalyzer = new StandardAnalyzer()
  private val initObject = FireObject(0,"object_00","type_00","rang1","prim","rang")

  override def preStart(): Unit = {
    //если при открытии reader-а вылетает исключение, то возможно папка пуста и надо туда что-то записать
    Try(DirectoryReader.open(FSDirectory.open(path)).close()).getOrElse(initLuceneDir())
  }

  private def initLuceneDir(): Unit = {
    addNewDoc(documentFromFireObject(initObject))
    log.info(s"Директория Lucene: $luceneDir была проинициализирована")
  }

  override def receive: Receive = {
    case UpdateOrCreateFireObject(fireObject: FireObject) =>
     search("gid_firepoints",fireObject.id.toString) match {
        case Some(fireFromLucen) =>
          if (fireFromLucen.length>1){
            log.error("Дублирование объектов в Lucene, объект не будет обновляться: " + fireFromLucen)
          }else{
            update(documentFromFireObject(fireObject))
            log.info("Обновлен объект: "+fireObject)
          }
        case None =>
         addNewDoc(documentFromFireObject(fireObject))
         log.info("Добавлен новый объект: " + fireObject)
      }

    case FindFireObjectsRequest(fieldName:String, keyWord:String) =>
      search(fieldName,keyWord) match {
        case Some(scoreDocs) =>
          val reader = DirectoryReader.open(FSDirectory.open(path))
          val searcher = new IndexSearcher(reader)
          val docs: Array[Document] = scoreDocs.map(scoreDoc => searcher.doc(scoreDoc.doc))
          reader.close()
          val fireObjects: Array[FireObject] = docs.map(
            doc => fireObjectFromDocument(doc)
          )
          sender() ! FoundFireObjectsResponse(fireObjects.toList)
        case None =>
          sender() ! FoundFireObjectsResponse(List.empty[FireObject])
      }

    case DeleteFireObject(fireObject: FireObject) =>
      deleteDoc(fireObject.id.toString)
      log.info("Удален объект: " +fireObject)
    case _ =>
      log.warning("Неизвестный запрос")
  }

  private def createWriter()={
    val directory = FSDirectory.open(path)
    val writerConfig = {
      new IndexWriterConfig(analyzer).setOpenMode(OpenMode.CREATE_OR_APPEND)}
    new IndexWriter(directory, writerConfig)
  }

  private def update(doc: Document): Unit = {
    val writer = createWriter()
    writer.updateDocument(new Term("gid_firepoints",doc.get("gid_firepoints")),doc)
    writer.close()
  }

  private def deleteDoc(gidFirepoinst: String): Unit = {
    val writer = createWriter()
    writer.deleteDocuments(new Term("gid_firepoints",gidFirepoinst))
    writer.close()
  }

  private def search(fieldNameInDoc:String, fieldValue:String): Option[Array[ScoreDoc]] = {
    val query = new QueryParser(fieldNameInDoc,analyzer).parse(fieldValue)
    val reader = DirectoryReader.open(FSDirectory.open(path))
    val searcher = new IndexSearcher(reader)
    val TopDocs = searcher.search(query,10)
    val scoreDocs = TopDocs.scoreDocs
    reader.close()
    if (scoreDocs!=null && scoreDocs.nonEmpty)
      Some(scoreDocs)
    else None
  }


  private def addNewDoc(doc: Document): Unit = {
    val writer = createWriter()
    try {
      writer.addDocument(doc)
    }catch {
      case e:IOException =>
        log.error(e,"IOException при попытке сохранить document: " + doc)
    }
    finally {
      if(writer.isOpen)
        writer.close()
    }
  }

  private def documentFromFireObject(fireObject: FireObject):Document={
    val doc = new Document
    doc.add(new StringField("gid_firepoints", fireObject.id.toString, Field.Store.YES))
    doc.add(new TextField("nameobject", fireObject.nameobject, Field.Store.YES))
    doc.add(new StringField("typeobject", fireObject.typeobject, Field.Store.YES))
    doc.add(new StringField("rang", fireObject.rang, Field.Store.YES))
    doc.add(new StringField("prim", fireObject.prim, Field.Store.YES))
    doc.add(new StringField("rangmodifiers", fireObject.rangmodifiers, Field.Store.YES))
    doc.add(new StringField("addteches", fireObject.addteches, Field.Store.YES))
    if (Option(fireObject.isenabled).isDefined) doc.add(new StringField("isenabled",fireObject.isenabled.toString, Field.Store.YES))//isenabled
    if (Option(fireObject.oldoid).isDefined) doc.add(new StringField("oldoid",fireObject.oldoid.toString, Field.Store.YES))//oldoid
    doc.add(new StringField("geomX", fireObject.x.toString, Field.Store.YES))
    doc.add(new StringField("geomY", fireObject.y.toString, Field.Store.YES))
    if (fireObject.address!=null && fireObject.address.nonEmpty) doc.add(new StringField("address",fireObject.address, Field.Store.YES)) //address
    if (fireObject.street!=null && fireObject.street.nonEmpty) doc.add(new StringField("street",fireObject.street, Field.Store.YES))//street
    if (fireObject.house!=null && fireObject.house.nonEmpty) doc.add(new StringField("house",fireObject.house, Field.Store.YES))//house
    if (fireObject.updater!=null && fireObject.updater.nonEmpty) doc.add(new StringField("updater",fireObject.updater, Field.Store.YES))//updater
    //updwhen
    doc
  }


  private def fireObjectFromDocument(doc:Document): FireObject =FireObject(
    doc.get("gid_firepoints").toInt,
    doc.get("nameobject"),
    doc.get("typeobject"),
    doc.get("rang"),
    doc.get("prim"),
    doc.get("rangmodifiers"),
    doc.get("addteches"),
    Try(doc.get("isenabled").toInt).getOrElse(0),
    Try(doc.get("oldoid").toInt).getOrElse(0),
    Try(doc.get("geomX").toDouble).getOrElse(0),
    Try(doc.get("geomY").toDouble).getOrElse(0),
    doc.get("address"),
    doc.get("street"),
    doc.get("house"),
    doc.get("updater")
//    doc.get("updwhen")
  )
}

object LuceneActor {
  def props(lucenDirSearch:String) = Props(new LuceneActor(lucenDirSearch))
  final case class UpdateOrCreateFireObject(fireObject: FireObject)
  final case class FindFireObjectsRequest(fieldName:String, keyWord:String)
  final case class FoundFireObjectsResponse(fireObjects:List[FireObject])
  final case class DeleteFireObject(fireObject: FireObject)
}


