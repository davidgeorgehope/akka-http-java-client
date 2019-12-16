name := "akka-http-java-client"

organization := "com.ferhtaydn"

version := "0.1.0"

version := "0.1"
scalaVersion := "2.11.8"
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")
mainClass in Compile := Some("HelloWorld")

libraryDependencies ++= {
  val akkaStreamVersion = "2.5.4"
  val akkaHttpVersion = "10.0.10"
  val scalaTestV = "3.0.4"

  Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-jackson" % akkaHttpVersion,
    "com.fasterxml.jackson.dataformat" % "jackson-dataformat-csv" % "2.7.5",
    // Only when running against Akka 2.5 explicitly depend on akka-streams in same version as akka-actor
    "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion, // or whatever the latest version is
    "com.typesafe.akka" %% "akka-actor"  % akkaStreamVersion

  )
}


assemblyJarName in assembly := "GoEuroTest.jar"
mainClass in assembly := Some("com.ferhtaydn.akkahttpjavaclient.GoEuroTest")

initialCommands := "import com.ferhtaydn.akkahttpjavaclient._"