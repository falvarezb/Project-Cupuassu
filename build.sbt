
lazy val commonSettings = Seq(
  name := "Project Cupuassu",
  version := "0.1",
  scalaVersion := "2.12.8"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.3" % "test",
  "com.typesafe" % "config" % "1.3.2"
)

scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-deprecation", "-feature",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps",
  "-Ywarn-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-unused",
  "-Ywarn-inaccessible",
  "-Ywarn-value-discard" ,
  "-Ywarn-unused-import",
  "-unchecked")

lazy val cupuassu = (project in file(".")).
  enablePlugins(JavaAppPackaging).
  settings(commonSettings: _*)

mainClass in Compile := Some("fjab.chess.apps.MainApp")