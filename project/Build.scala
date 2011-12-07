import sbt._
import Keys._
import samskivert.ProjectBuilder

object AtlantisBuild extends Build {
  val builder = new ProjectBuilder("pom.xml") {
    override val globalSettings = Seq(
      crossPaths   := false,
      scalaVersion := "2.9.1",
      javacOptions ++= Seq("-Xlint", "-Xlint:-serial", "-source", "1.6", "-target", "1.6"),
      fork in Compile := true,
      autoScalaLibrary := false // no scala-library dependency
    )
    override def projectSettings (name :String) = name match {
      case "core" => Seq(
        unmanagedResourceDirectories in Compile <+= baseDirectory / "src/main/java",
        libraryDependencies ++= Seq(
          // scala test dependencies
 	        "com.novocode" % "junit-interface" % "0.7" % "test->default"
        )
      )
      case _ => Nil
    }
  }

  lazy val core = builder("core")
  lazy val java = builder("java")
  lazy val server = builder("server")

  // one giant fruit roll-up to bring them all together
  lazy val atlantis = Project("atlantis", file(".")) aggregate(core, java, server)
}
