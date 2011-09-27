resolvers += "Condep repo" at "http://samskivert.github.com/sbt-condep-plugin/maven"

libraryDependencies ++= Seq(
  "net.thunderklaus" %% "sbt-gwt-plugin" % "1.1-SNAPSHOT",
  "com.samskivert" %% "sbt-condep-plugin" % "1.0"
)
