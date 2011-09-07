import sbt._
import Keys._
import com.samskivert.condep.Depends
import net.thunderklaus.GwtPlugin._

object AtlantisBuild extends Build {
  val playnVersion = "1.0-SNAPSHOT"
  val nexusVersion = "1.0-SNAPSHOT"

  val coreLocals = Depends(
    ("tripleplay", null,  "com.threerings" % "tripleplay" % "1.0-SNAPSHOT"),
    ("nexus",     "core", "com.threerings" % "nexus-core" % nexusVersion)
  )
  val htmlLocals = Depends(
    ("playn", "html",   "com.googlecode.playn" % "playn-html" % playnVersion),
    ("nexus", "gwt-io", "com.threerings" % "nexus-gwt-io" % nexusVersion)
  )
  val serverLocals = Depends(
    ("nexus", "gwt-server", "com.threerings" % "nexus-gwt-server" % nexusVersion),
    ("nexus", "jvm-server", "com.threerings" % "nexus-jvm-server" % nexusVersion)
  )
  // TBD: local symlinks are currently also needed for react, pythagoras and playn

  val commonSettings = Defaults.defaultSettings ++ Seq(
    organization := "com.threerings",
    version      := "1.0-SNAPSHOT",
    crossPaths   := false,
    javacOptions ++= Seq("-Xlint", "-Xlint:-serial", "-source", "1.6", "-target", "1.6"),
    fork in Compile := true,
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
