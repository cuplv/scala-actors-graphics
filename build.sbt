name := "scala-graphics-akka"

version := "0.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
    "org.scala-lang.modules" %% "scala-swing" % "2.0.3",
    "com.typesafe.akka" %% "akka-actor" % "2.5.18",
)

scalaVersion := "2.12.7"