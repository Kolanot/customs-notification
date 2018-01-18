/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package integration

import org.scalatest.BeforeAndAfterAll
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.customs.notification.connectors.PublicNotificationServiceConnector
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.logging.Authorization
import util.ExternalServicesConfig.{Host, Port}
import util.TestData._
import util.{ExternalServicesConfig, PublicNotificationService, RequestHeaders}

class PublicNotificationServiceConnectorSpec extends IntegrationTestSpec with GuiceOneAppPerSuite with MockitoSugar
  with BeforeAndAfterAll with PublicNotificationService {

  private lazy val connector = app.injector.instanceOf[PublicNotificationServiceConnector]

  val incomingBearerToken = "some_client's_bearer_token"
  val incomingAuthToken = s"Bearer $incomingBearerToken"

  private implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(incomingAuthToken)))
    .withExtraHeaders(RequestHeaders.X_CDS_CLIENT_ID_HEADER)

  override protected def beforeAll() {
    startMockServer()
  }

  override protected def afterEach(): Unit = {
    resetMockServer()
  }

  override protected def afterAll() {
    stopMockServer()
  }

  override implicit lazy val app: Application =
    GuiceApplicationBuilder().configure(Map(
      "auditing.enabled" -> false,
      "microservice.services.public-notification.host" -> Host,
      "microservice.services.public-notification.port" -> Port,
      "microservice.services.public-notification.context" -> ExternalServicesConfig.PublicNotificationServiceContext
    )).build()

  "PublicNotificationServiceConnector" should {

    "make a correct request" in {
      setupPublicNotificationServiceToReturn(NO_CONTENT)

      await(connector.send(publicNotificationRequest))

      verifyPublicNotificationServiceWasCalledWith(publicNotificationRequest)
    }

    "return a failed future with wrapped HttpVerb NotFoundException when external service returns 404" in {
      setupPublicNotificationServiceToReturn(NOT_FOUND)

      val caught = intercept[RuntimeException](await(connector.send(publicNotificationRequest)))

      caught.getCause.getClass shouldBe classOf[NotFoundException]
    }

    "return a failed future with wrapped HttpVerbs BadRequestException when external service returns 400" in {
      setupPublicNotificationServiceToReturn(BAD_REQUEST)

      val caught = intercept[RuntimeException](await(connector.send(publicNotificationRequest)))

      caught.getCause.getClass shouldBe classOf[BadRequestException]
    }

    "return a failed future with Upstream5xxResponse when external service returns 500" in {
      setupPublicNotificationServiceToReturn(INTERNAL_SERVER_ERROR)

      intercept[Upstream5xxResponse](await(connector.send(publicNotificationRequest)))
    }

    "return a failed future with wrapped HttpVerbs BadRequestException when it fails to connect the external service" in
      withoutWireMockServer {
        val caught = intercept[RuntimeException](await(connector.send(publicNotificationRequest)))

        caught.getCause.getClass shouldBe classOf[BadGatewayException]
      }
  }

}
