import BuildHelper._

inThisBuild(
  List(
    organization := "me.mnedokushev"
  )
)

lazy val root =
  project
    .in(file("."))
    .aggregate(client, appcli)
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

lazy val appcli =
  project
    .in(file("modules/appcli"))
    .dependsOn(client)
    .settings(stdSettings("appcli"))
    .settings(
      libraryDependencies := Dependencies.appcli
    )
    .settings(
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
    )
