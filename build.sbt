name := "message-service"

version := "0.1"

scalaVersion := "2.12.8"

val akkaVersion = "2.5.20"
val akkaHttpVersion = "10.1.7"

libraryDependencies ++= Seq (
  "com.github.scopt" %% "scopt" % "3.7.0",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "io.argonaut" %% "argonaut" % "6.2.2",
  "org.typelevel" %% "cats-core" % "1.5.0",
  "commons-codec" % "commons-codec" % "1.11",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)

javacOptions ++= Seq (
  "target", "1.8"
)

scalacOptions ++= Seq (
  "-encoding", "utf-8",
  "-target:jvm-1.8",
  "-feature",
  "-deprecation",
  "-unchecked",
  "-explaintypes",
  "-Xcheckinit",
  "-Xfatal-warnings",
  "-Xlint:_",
  "-Ypartial-unification"
)
