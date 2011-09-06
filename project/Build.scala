import sbt._
import Keys._
import net.thunderklaus.GwtPlugin._

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
  val playnVersion = "1.0-SNAPSHOT"
  val nexusVersion = "1.0-SNAPSHOT"

  val coreLocals = new Local(
    ("tripleplay", null,  "com.threerings" % "tripleplay" % "1.0-SNAPSHOT"),
    ("nexus",     "core", "com.threerings" % "nexus-core" % nexusVersion)
  )
  val htmlLocals = new Local(
    ("playn", "html",   "com.googlecode.playn" % "playn-html" % playnVersion),
    ("nexus", "gwt-io", "com.threerings" % "nexus-gwt-io" % nexusVersion)
  )
  val serverLocals = new Local(
    ("nexus", "gwt-server", "com.threerings" % "nexus-gwt-server" % nexusVersion),
    ("nexus", "jvm-server", "com.threerings" % "nexus-jvm-server" % nexusVersion)
  )
  // TBD: local symlinks are currently also needed for react, pythagoras and playn

  val commonSettings = Defaults.defaultSettings ++ Seq(
    organization := "com.threerings",
    version      := "1.0-SNAPSHOT",
    crossPaths   := false,
    javacOptions ++= Seq("-Xlint", "-Xlint:-serial"),
    fork in Compile := true,
    resolvers    += "Local Maven Repository" at Path.userHome.asURL + "/.m2/repository",
    autoScalaLibrary := false // no scala-library dependency
  )

  lazy val core = coreLocals.addDeps(Project(
    "core", file("core"), settings = commonSettings ++ Seq(
      name := "atlantis-core",
      unmanagedResourceDirectories in Compile <+= baseDirectory / "src/main/java",
      unmanagedResources in Compile ~= (_.filterNot(_.isDirectory)), // work around SBT bug
      libraryDependencies ++= coreLocals.libDeps ++ Seq(
        // compile dependencies
        "com.google.guava" % "guava" % "r09",
        // test dependencies
        "junit" % "junit" % "4.+" % "test",
 	      "com.novocode" % "junit-interface" % "0.7" % "test->default"
      )
    )
  ))

  lazy val html = htmlLocals.addDeps(Project(
    "html", file("html"), settings = commonSettings ++ gwtSettings ++ Seq(
      name       := "atlantis-html",
      gwtVersion := "2.3.0",
      libraryDependencies ++= htmlLocals.libDeps ++ Seq(
        // compile dependencies
        "com.google.guava" % "guava" % "r09",
        // TODO: sbt bug? causes above guava to be overridden by below
        "com.google.guava" % "guava" % "r09" classifier "gwt",
        "allen_sauer" % "gwt-log" % "3.1.4"
      )
    )
  )) dependsOn(core)

  // one giant fruit roll-up to bring them all together
  lazy val atlantis = Project("atlantis", file(".")) aggregate(core, html)
}
