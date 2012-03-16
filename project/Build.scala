import sbt._
import Keys._
import samskivert.ProjectBuilder

object Nexus {
  def NexusGen = sbt.config("nexus") hide

  val nexusGen = TaskKey[Int]("generate", "Generates Nexus streaming code")
  def nexusGenTask = (streams, javaSource in Compile, dependencyClasspath in Compile,
                      managedClasspath in NexusGen) map {
    (s, sourceDir, cp, ncp) => {
      val copts = Seq("-sourcepath", sourceDir.getPath, "-s", sourceDir.getPath, "-proc:only",
                      "-classpath", (cp.files ++ ncp.files).map(_.getPath).mkString(":"),
                      "-processor", "com.threerings.nexus.gencode.Processor",
                      "-Acom.threerings.nexus.gencode.header=etc/SOURCE_HEADER")
      val excludes = (sourceDir ** "Streamer_*.java") +++ (sourceDir ** "Factory_*.java")
      val sources = ((sourceDir ** "*.java") --- excludes).get.map(_.getPath)
      s.log.debug("Generating Nexus streaming classes")
      Fork.javac(None, copts ++ sources, StdoutOutput)
    }
  }

  def config = Seq[Setting[_]](
    nexusGen in NexusGen <<= nexusGenTask,
    ivyConfigurations += NexusGen,
    managedClasspath in NexusGen <<= (classpathTypes, update) map {
      (ct, up) => Classpaths.managedJars(NexusGen, ct, up)},
    libraryDependencies += "com.threerings.nexus" % "nexus-tools" % "1.0-SNAPSHOT" % "nexus"
  )
}

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
      case "core" => Nexus.config ++ Seq(
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
