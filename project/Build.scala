import sbt._
import Keys._

// allows projects to be symlinked into the current directory for a direct dependency,
// or fall back to obtaining the project from Maven otherwise
class Local (locals :(String, String, ModuleID)*) {
  def addDeps (p :Project) = (locals collect {
    case (id, subp, dep) if (file(id).exists) => symproj(file(id), subp)
  }).foldLeft(p) { _ dependsOn _ }
  def libDeps = locals collect {
    case (id, subp, dep) if (!file(id).exists) => dep
  }
  private def symproj (dir :File, subproj :String = null) =
    if (subproj == null) RootProject(dir) else ProjectRef(dir, subproj)
}

object AtlantisBuild extends Build {
  val locals = new Local(
    ("tripleplay", null,    "com.threerings" % "tripleplay" % "1.0-SNAPSHOT"),
    ("nexus", "gwt-server", "com.threerings" % "nexus-gwt-server" % "1.0-SNAPSHOT"),
    ("nexus", "jvm-server", "com.threerings" % "nexus-jvm-server" % "1.0-SNAPSHOT")
  )
  // TBD: local symlinks are currently also needed for react, pythagoras and forplay

  lazy val atlantis = locals.addDeps(Project(
    "atlantis", file("."), settings = Defaults.defaultSettings ++ Seq(
      organization := "com.threerings",
      version      := "1.0-SNAPSHOT",
      name         := "atlantis",
      crossPaths   := false,

      javacOptions ++= Seq("-Xlint", "-Xlint:-serial"),
      fork in Compile := true,

      resolvers += "Local Maven Repository" at Path.userHome.asURL + "/.m2/repository",

      autoScalaLibrary := false, // no scala-library dependency
      libraryDependencies ++= locals.libDeps ++ Seq(
        // compile dependencies
        "com.google.guava" % "guava" % "r09",
        "com.google.gwt" % "gwt-user" % "2.3.0", // should be exported by forplay-core?
        "allen_sauer" % "gwt-log" % "1.0.r613",
        // test dependencies
        "junit" % "junit" % "4.+" % "test",
 	      "com.novocode" % "junit-interface" % "0.7" % "test->default"
      )
    )
  ))
}
