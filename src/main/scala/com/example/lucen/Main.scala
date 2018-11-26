package com.example.lucen

import java.nio.file.Paths

import akka.actor.ActorSystem
import com.example.lucen.LuceneActor.{FindFireObjectsRequest, UpdateOrCreateFireObject}
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Document, Field, StringField}
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.{IndexSearcher, Query, ScoreDoc}
import org.apache.lucene.store.{Directory, FSDirectory}

object Main extends App {
  val system = ActorSystem("lucen")
  val lucenActor = system.actorOf(LuceneActor.props("ForLucene"))
//  lucenActor ! UpdateOrCreateFireObject(FireObject("id-01","name-01"))
//  lucenActor ! FindFireObjectsRequest("id-01","01")


//  val analayzer = new StandardAnalyzer()
//  val directory:Directory = FSDirectory.open(Paths.get("ForLucene"))
//  val configWriter = new IndexWriterConfig(analayzer)
//  val writer = new IndexWriter(directory,configWriter)
//  val document = new Document
//  val fireObject = new FireObject("id01","name01")
//  document.add(new StringField("id",fireObject.id,Field.Store.YES))
//  document.add(new StringField("nameobject",fireObject.name,Field.Store.YES))
//  writer.addDocument(document)
//  writer.close()
//
//  val reader = DirectoryReader.open(directory)
//  val searcher = new IndexSearcher(reader)
//  val parser = new QueryParser("id",analayzer)
//  val query: Query = parser.parse("id01")
//  val scoreDocs: Array[ScoreDoc] = searcher.search(query,10).scoreDocs
//  val docs: Array[Document] = scoreDocs.map(scoreDoc => searcher.doc(scoreDoc.doc))
//  val fireObjects: Array[FireObject] = docs.map(
//    doc =>
//      FireObject(
//        doc.get("id"),
//        doc.get("nameobject")
//      )
//  )
//  fireObjects.foreach(fireObject =>println(fireObject))

}
