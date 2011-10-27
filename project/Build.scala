import sbt._
import Keys._
import com.samskivert.condep.Depends

object AtlantisBuild extends Build {
  val playnVersion = "1.0-SNAPSHOT"
  val nexusVersion = "1.0-SNAPSHOT"

  // TBD: local symlinks are currently also needed for react, pythagoras and playn

  val commonSettings = Defaults.defaultSettings ++ Seq(
    organization := "com.threerings",
    version      := "1.0-SNAPSHOT",
    crossPaths   := false,
    javacOptions ++= Seq("-Xlint", "-Xlint:-serial", "-source", "1.6", "-target", "1.6"),
    fork in Compile := true,
    autoScalaLibrary := false // no scala-library dependency
  )

  val coreLocals = Depends(
    ("tripleplay", null,  "com.threerings" % "tripleplay" % "1.0-SNAPSHOT"),
    ("nexus",     "core", "com.threerings.nexus" % "nexus-core" % nexusVersion)
  )
  lazy val core = coreLocals.addDeps(Project(
    "core", file("core"), settings = commonSettings ++ Seq(
      name := "atlantis-core",
      unmanagedResourceDirectories in Compile <+= baseDirectory / "src/main/java",
      unmanagedResources in Compile ~= (_.filterNot(_.isDirectory)), // work around SBT bug
      libraryDependencies ++= coreLocals.libDeps ++ Seq(
        // compile dependencies
        "com.google.guava" % "guava" % "10.0.1",
        // test dependencies
        "junit" % "junit" % "4.+" % "test",
        "com.novocode" % "junit-interface" % "0.7" % "test->default"
      )
    )
  ))

  val javaLocals = Depends(
    ("playn", "java",   "com.googlecode.playn" % "playn-java" % playnVersion),
    ("nexus", "jvm-io", "com.threerings.nexus" % "nexus-jvm-io" % nexusVersion)
  )
  lazy val java = javaLocals.addDeps(Project(
    "java", file("java"), settings = commonSettings ++ Seq(
      name := "atlantis-java",
      libraryDependencies ++= javaLocals.libDeps ++ Seq(
        // nada for now
      )
    )
  )) dependsOn(core)

  val htmlLocals = Depends(
    ("playn", "html",   "com.googlecode.playn" % "playn-html" % playnVersion),
    ("nexus", "gwt-io", "com.threerings.nexus" % "nexus-gwt-io" % nexusVersion)
  )
  lazy val html = htmlLocals.addDeps(Project(
    "html", file("html"), settings = commonSettings ++ Seq(
      name := "atlantis-html",
      libraryDependencies ++= htmlLocals.libDeps ++ Seq(
        "com.google.guava" % "guava-gwt" % "10.0.1",
        "allen_sauer" % "gwt-log" % "3.1.4"
      )
    )
  )) dependsOn(core)

  val serverLocals = Depends(
    ("nexus", "gwt-server", "com.threerings.nexus" % "nexus-gwt-server" % nexusVersion),
    ("nexus", "jvm-server", "com.threerings.nexus" % "nexus-jvm-server" % nexusVersion)
  )
  lazy val server = serverLocals.addDeps(Project(
    "server", file("server"), settings = commonSettings ++ Seq(
      name := "atlantis-server",
      libraryDependencies ++= serverLocals.libDeps ++ Seq(
        // nada for now
      )
    )
  )) dependsOn(core)

  // one giant fruit roll-up to bring them all together
  lazy val atlantis = Project("atlantis", file(".")) aggregate(core, java, html, server)
}
