import BuildHelper._

inThisBuild(
  List(
    organization := "me.mnedokushev"
  )
)

lazy val root =
  project
    .in(file("."))
    .aggregate(client)
    .settings(publish / skip := true)

lazy val client =
  project
    .in(file("modules/client"))
    .settings(stdSettings("client"))
    .settings(
      libraryDependencies := Dependencies.client
    )
    .settings(
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
    )
