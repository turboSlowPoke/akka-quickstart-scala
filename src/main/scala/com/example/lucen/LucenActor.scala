package com.example.lucen

import java.io.IOException
import java.nio.file.Paths

import akka.actor.{Actor, ActorLogging, Props}
import com.example.lucen.LucenActor.{FindFireObjectsRequest, FoundFireObjectsResponse, UpdateOrCreateFireObject}
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document._
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.{IndexSearcher, Query, ScoreDoc, TopDocs}
import org.apache.lucene.store.FSDirectory

class LucenActor(lucenDir:String) extends Actor with  ActorLogging{
  private val directory = Paths.get(lucenDir)
  private val reader = DirectoryReader.open(FSDirectory.open(directory))
  private val indexSearcher = new IndexSearcher(reader)
  private val analyzer = new StandardAnalyzer()
  private val writerConfig = {
    new IndexWriterConfig(analyzer).setOpenMode(OpenMode.CREATE_OR_APPEND)}
  private val directoryWriter = FSDirectory.open(directory)

  override def receive: Receive = {
    case UpdateOrCreateFireObject(fireObject: FireObject) =>
      //найти в базе
     search("id",fireObject.id) match {
        case Some(fireFromLucen) =>
          if (fireFromLucen.length>0){
            log.error("Дублирование объектов в Lucene " + fireFromLucen)
          }else{
            //update()
          }
        case None =>
         addNewDoc(fireObject)
      }
    case FindFireObjectsRequest(fieldName:String, fieldValue:String) =>
      search(fieldName,fieldValue) match {
        case Some(scoreDocs) =>
          val docs: Array[Document] = scoreDocs.map(scoreDoc => indexSearcher.doc(scoreDoc.doc))
          val fireObjects: Array[FireObject] = docs.map(
            doc =>
              FireObject(
                doc.get("id"),
                doc.get("name")
            )
          )
          sender() ! FoundFireObjectsResponse(fireObjects.toList)
      }

    case _ =>
      log.warning("Неизвестный запрос")
  }

  private def search(fieldName:String, fieldValue:String): Option[Array[ScoreDoc]] = {
    val parser = new QueryParser(fieldName,analyzer)
    val query: Query = parser.parse(fieldValue)
    val scoreDocs: Array[ScoreDoc] = indexSearcher.search(query,10).scoreDocs
    if (scoreDocs!=null && !scoreDocs.isEmpty)
      Some(scoreDocs)
    else None
  }

  private def addNewDoc(fireObject: FireObject): Unit = {
    val writer = new IndexWriter(directoryWriter, writerConfig)
    val document = new Document
    document.add(new StringField("id",fireObject.id,Field.Store.YES))
    document.add(new StringField("nameobject",fireObject.name,Field.Store.YES))
    try {
      writer.addDocument(document)
    }catch {
      case e:IOException =>
        log.error(e,"IOException при поытке сохранить document: " + document)
    }
    finally {
      if(writer.isOpen)
        writer.close()
    }
  }
}

object LucenActor {
  def props(lucenDirSearch:String) = Props(new LucenActor(lucenDirSearch))
  final case class UpdateOrCreateFireObject(fireObject: FireObject)
  final case class FindFireObjectsRequest(fieldName:String, fieldValue:String)
  final case class FoundFireObjectsResponse(fireObjects:List[FireObject])
}


