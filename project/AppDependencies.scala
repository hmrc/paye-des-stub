import play.core.PlayVersion
import sbt._

object AppDependencies {

  private val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-27" % "5.3.0",
    "uk.gov.hmrc" %% "simple-reactivemongo"      % "8.0.0-play-27",
    "uk.gov.hmrc" %% "domain"                    % "5.11.0-play-27",
    "uk.gov.hmrc" %% "tax-year"                  % "1.3.0",
    "uk.gov.hmrc" %% "hmrc-stubs-core"           % "6.2.0-play-26"
  )

  private val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "reactivemongo-test" % "5.0.0-play-27",
    "org.scalatest"          %% "scalatest"          % "3.0.9",
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3",
    "org.mockito"            % "mockito-core"        % "2.10.0",
    "org.pegdown"            % "pegdown"             % "1.6.0",
    "com.typesafe.play"      %% "play-test"          % PlayVersion.current,
    "org.scalaj"             %% "scalaj-http"        % "2.4.2"
  ).map(_ % "test, it")

  private val silencerDependencies: Seq[ModuleID] = Seq(
    compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.1" cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % "1.7.1" % Provided cross CrossVersion.full
  )

  def apply(): Seq[ModuleID] = compile ++ test ++ silencerDependencies
}
