name := "uscis"

version := "1.0-SNAPSHOT"

resolvers += "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + ".m2/maven.repo"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "com.noorq.casser" % "casser-core" % "1.0.0",
  "org.jsoup" % "jsoup" % "1.8.1",
  "org.quartz-scheduler" % "quartz" % "2.2.1",
  javaJdbc,
  javaEbean,
  cache,
  javaWs
)

pipelineStages := Seq(rjs, digest, gzip)