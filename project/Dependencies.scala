import sbt._

object Dependencies {

  object version {
    val zio = "2.0.0"
  }

  object org {
    val zio    = "dev.zio"
    val google = "com.google"
  }

  lazy val zio        = org.zio %% "zio"          % version.zio
  lazy val zioTest    = org.zio %% "zio-test"     % version.zio
  lazy val zioTestSbt = org.zio %% "zio-test-sbt" % version.zio
  lazy val zioNio     = org.zio %% "zio-nio"      % version.zio
  lazy val zioPrelude = org.zio %% "zio-prelude"  % "1.0.0-RC15" // supports zio2 since 1.0.0-RC15

  lazy val googleApiClient             = s"${org.google}.api-client"   % "google-api-client"               % "1.33.0"
  lazy val googleApiServicesDrive      = s"${org.google}.apis"         % "google-api-services-drive"       % "v3-rev20211107-1.32.1"
  lazy val googleOauthClientJetty      = s"${org.google}.oauth-client" % "google-oauth-client-jetty"       % "1.32.1"
  lazy val googleOauthClient           = s"${org.google}.oauth-client" % "google-oauth-client"             % "1.30.4"
  lazy val googleAuthLibraryOauth2Http = s"${org.google}.auth"         % "google-auth-library-oauth2-http" % "1.3.0"

  lazy val client = Seq(
    zio,
    zioNio,
    zioPrelude,
    googleApiClient,
    googleApiServicesDrive,
    googleOauthClientJetty,
    googleOauthClient,
    googleAuthLibraryOauth2Http,
    zioTest    % Test,
    zioTestSbt % Test,
    zioPrelude % Test
  )

  lazy val appcli = Seq(
    zio,
    zioNio,
    zioPrelude,
    googleApiClient,
    googleApiServicesDrive,
    googleOauthClientJetty,
    googleOauthClient,
    googleAuthLibraryOauth2Http,
    zioTest    % Test,
    zioTestSbt % Test
  )

}
