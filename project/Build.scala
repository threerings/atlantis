import sbt._
import Keys._

object AtlantisBuild extends Build {
  // allow projects to be symlinked into the current directory for a direct dependency, or fall
  // back to obtaining the project from maven otherwise
  val locals = Seq(
    ("pythagoras", "com.samskivert" % "pythagoras" % "1.1-SNAPSHOT",
     () => RootProject(file("pythagoras"))),
    ("forplay", "com.googlecode.forplay" % "core" % "1.0-SNAPSHOT",
     () => ProjectRef(file("forplay"), "core")),
    ("tripleplay", "com.threerings" % "tripleplay" % "1.0-SNAPSHOT",
     () => RootProject(file("tripleplay"))),
    ("nexus", "com.threerings" % "nexus-gwt-server" % "1.0-SNAPSHOT",
     () => ProjectRef(file("nexus"), "gwt-server")),
    ("nexus", "com.threerings" % "nexus-jvm-server" % "1.0-SNAPSHOT",
     () => ProjectRef(file("nexus"), "jvm-server"))
  )
  val (localDeps, localProjs) = locals map {
    case (id, dep, proj) => if (new java.io.File(id).exists) (None, Some(proj()))
                            else (Some(dep), None)
  } unzip

  lazy val atlantis = (Project(
    "atlantis", file("."), settings = Defaults.defaultSettings ++ Seq(
      organization := "com.threerings",
      version      := "1.0-SNAPSHOT",
      name         := "atlantis",
      crossPaths   := false,

      javacOptions ++= Seq("-Xlint", "-Xlint:-serial"),
      fork in Compile := true,

      resolvers += "Local Maven Repository" at Path.userHome.asURL + "/.m2/repository",

      libraryDependencies ++= localDeps.flatten ++ Seq(
        // compile dependencies
        "com.google.guava" % "guava" % "r09",
        "com.google.gwt" % "gwt-user" % "2.3.0", // should be exported by forplay-core?
        "allen_sauer" % "gwt-log" % "1.0.r613",
        // test dependencies
        "junit" % "junit" % "4.+" % "test",
 	      "com.novocode" % "junit-interface" % "0.7" % "test->default"
      )
    )
  ) /: localProjs.flatten) { _ dependsOn _ }
}
