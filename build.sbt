name := "akka-quickstart-scala"

version := "1.0"

scalaVersion := "2.12.6"

lazy val akkaVersion = "2.5.18"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.apache.lucene" % "lucene-core" % "7.5.0",
  "org.apache.lucene" % "lucene-analyzers-common" % "7.5.0",
  "org.apache.lucene" % "lucene-queryparser" % "7.5.0"
)
