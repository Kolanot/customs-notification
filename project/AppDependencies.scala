import sbt._

object AppDependencies {
  
  private val scalatestplusVersion = "4.0.3"
  private val mockitoVersion = "3.5.9"
  private val wireMockVersion = "2.27.2"
  private val customsApiCommonVersion = "1.54.0"
  private val workItemRepoVersion = "7.10.0-play-27"
  private val reactiveMongoTestVersion = "4.21.0-play-27"

  private val testScope = "test,it"

  val scalaTestPlusPlay = "org.scalatestplus.play" %% "scalatestplus-play" % scalatestplusVersion % testScope
  val wireMock = "com.github.tomakehurst" % "wiremock-jre8" % wireMockVersion % testScope
  val mockito =  "org.mockito" % "mockito-core" % mockitoVersion % testScope
  val customsApiCommon = "uk.gov.hmrc" %% "customs-api-common" % customsApiCommonVersion
  val workItemRepo = "uk.gov.hmrc" %% "work-item-repo" % workItemRepoVersion
  val customsApiCommonTests = "uk.gov.hmrc" %% "customs-api-common" % customsApiCommonVersion % testScope classifier "tests"
  val reactiveMongoTest = "uk.gov.hmrc" %% "reactivemongo-test" % reactiveMongoTestVersion % testScope
}
