package com.example.lucen

import java.io.IOException
import java.nio.file.{Path, Paths}

import akka.actor.{Actor, ActorLogging, Props}
import com.example.lucen.LuceneActor.{FindFireObjectsRequest, FoundFireObjectsResponse, UpdateOrCreateFireObject}
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document._
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig, Term}
import org.apache.lucene.search._
import org.apache.lucene.store.FSDirectory

import scala.util.Try

class LuceneActor(lucenDir:String) extends Actor with  ActorLogging{
  private val path: Path = Paths.get(lucenDir)
  private val analyzer: StandardAnalyzer = new StandardAnalyzer()
  private val initObject = FireObject(0,"name_00","type_00","rang1","prim","rang")


//  private val searcher:IndexSearcher=Try(createSearcher(path,analyzer)).getOrElse(initLuceneDir())


  override def preStart(): Unit = {
    Try(DirectoryReader.open(FSDirectory.open(path))).getOrElse(initLuceneDir())
  }

  private def initLuceneDir(): Unit = {
    addNewDoc(documentFromFireObject(initObject))
    log.info(s"Директория Lucene: ${lucenDir} была проинициализирована")
  }

  private def createWriter()={
    val directory = FSDirectory.open(path)
    val writerConfig = {
      new IndexWriterConfig(analyzer).setOpenMode(OpenMode.CREATE_OR_APPEND)}
    new IndexWriter(directory, writerConfig)
  }

  override def receive: Receive = {
    case UpdateOrCreateFireObject(fireObject: FireObject) =>
      //найти в базе
     search("gid_firepoints",fireObject.id.toString) match {
        case Some(fireFromLucen) =>
          if (fireFromLucen.length>1){
            log.error("Дублирование объектов в Lucene, объект не будет обновляться: " + fireFromLucen)
          }else{
            update(documentFromFireObject(fireObject))
          }
        case None =>
         addNewDoc(documentFromFireObject(fireObject))
      }
    case FindFireObjectsRequest(fieldName:String, fieldValue:String) =>
      search(fieldName,fieldValue) match {
        case Some(scoreDocs) =>
          val reader = DirectoryReader.open(FSDirectory.open(path))
          val searcher = new IndexSearcher(reader)
          val docs: Array[Document] = scoreDocs.map(scoreDoc => {searcher.doc(scoreDoc.doc)})
          reader.close()
          val fireObjects: Array[FireObject] = docs.map(
            doc => fireObjectFromDocument(doc)
          )
          println(fireObjects.toList.head)
          sender() ! FoundFireObjectsResponse(fireObjects.toList)
        case None =>
          sender() ! FoundFireObjectsResponse(List.empty[FireObject])
      }

    case _ =>
      log.warning("Неизвестный запрос")
  }

  private def update(doc: Document): Unit = {
    val writer = createWriter()
    writer.updateDocument(new Term("gid_firepoints",doc.get("gid_firepoints")),doc)
    writer.close()
  }

  private def search(fieldName:String, fieldValue:String): Option[Array[ScoreDoc]] = {
    val gidBuilder: BooleanQuery.Builder = new BooleanQuery.Builder
    gidBuilder.add(new TermQuery(new Term(fieldName, fieldValue)), BooleanClause.Occur.MUST)
    val reader = DirectoryReader.open(FSDirectory.open(path))
    val searcher = new IndexSearcher(reader)
    val TopDocs = searcher.search(gidBuilder.build(),10)
    val scoreDocs = TopDocs.scoreDocs
    if (scoreDocs!=null && scoreDocs.nonEmpty)
      Some(scoreDocs)
    else None
  }

  private def addNewDoc(doc: Document): Unit = {
    val writer = createWriter()

    try {
      writer.addDocument(doc)
      writer.commit()
    }catch {
      case e:IOException =>
        log.error(e,"IOException при попытке сохранить document: " + doc)
    }
    finally {
      if(writer.isOpen)
        writer.close()
    }
  }

  def documentFromFireObject(fireObject: FireObject):Document={
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

  def fireObjectFromDocument(doc:Document): FireObject =FireObject(
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
  final case class FindFireObjectsRequest(fieldName:String, fieldValue:String)
  final case class FoundFireObjectsResponse(fireObjects:List[FireObject])
}


