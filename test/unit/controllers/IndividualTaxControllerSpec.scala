/*
 * Copyright 2021 HM Revenue & Customs
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

package unit.controllers

import akka.stream.Materializer
import common.LogSuppressing
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, OptionValues, WordSpecLike}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{stubControllerComponents, _}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.payedesstub.controllers.IndividualTaxController
import uk.gov.hmrc.payedesstub.models._
import uk.gov.hmrc.payedesstub.services.{IndividualTaxSummaryService, ScenarioLoader}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IndividualTaxControllerSpec extends WordSpecLike with Matchers with OptionValues
  with MockitoSugar with ScalaFutures with GuiceOneAppPerSuite with LogSuppressing {

  trait Setup {
    implicit lazy val materializer: Materializer = fakeApplication.materializer
    implicit val hc: HeaderCarrier = HeaderCarrier()

    def createIndividualTaxRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      .withHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Content-Type" -> "application/vnd.hmrc.1.0+json")


    val underTest = new IndividualTaxController(mock[ScenarioLoader], mock[IndividualTaxSummaryService],
      stubControllerComponents()
    )

    def createSummaryRequest(scenario: String): FakeRequest[JsValue] = {
      createIndividualTaxRequest.withBody[JsValue](Json.parse(s"""{ "scenario": "$scenario" }"""))
    }

    def emptyRequest: FakeRequest[JsValue] = {
      createIndividualTaxRequest.withBody[JsValue](Json.parse("{}"))
    }

    val validUtrString = "2234567890"
    val validTaxYearString = "2016-17"
    val utr: SaUtr = SaUtr(validUtrString)
    val taxYear: TaxYear = TaxYear(validTaxYearString)
    val individualTaxResponse: IndividualTaxResponse = IndividualTaxResponse(StateBenefits(0.0, 0.0), Refund(0.0), Nil)
    val individualTax: IndividualTax = IndividualTax("", "", individualTaxResponse)
  }

  "find" should {
    "return 200 (Ok) with the happy path response when called with a utr and taxYear that are found" in new Setup {

      given(underTest.service.fetch(validUtrString, validTaxYearString))
        .willReturn(Future(Some(IndividualTax("", "", IndividualTaxResponse(StateBenefits(0.0, 0.0), Refund(0.0), Nil)))))

      val result: Future[Result] = Future(underTest.find(validUtrString, validTaxYearString)(createIndividualTaxRequest)).futureValue

      status(result) shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(individualTaxResponse)
    }

    "return 404 (NotFound) when called with a utr and taxYear that are not found" in new Setup {

      given(underTest.service.fetch(validUtrString, validTaxYearString)).willReturn(Future(None))

      val result: Future[Result] = Future(underTest.find(validUtrString, validTaxYearString)(createIndividualTaxRequest)).futureValue

      status(result) shouldBe NOT_FOUND
    }
  }

  "create" should {

    "return a created response and store the Individual Tax summary" in new Setup {

      given(underTest.scenarioLoader.loadScenario[IndividualTaxResponse](anyString, anyString)(any()))
        .willReturn(Future.successful(individualTaxResponse))
      given(underTest.service.create(anyString, anyString, any[IndividualTaxResponse]))
        .willReturn(Future.successful(individualTax))

      val result: Future[Result] = Future(underTest.create(utr, taxYear)(createSummaryRequest("HAPPY_PATH_1"))).futureValue

      status(result) shouldBe CREATED
      verify(underTest.scenarioLoader).loadScenario[IndividualTaxResponse]("individual-tax", "HAPPY_PATH_1")
      verify(underTest.service).create(validUtrString, taxYear.startYr, individualTaxResponse)
    }

    "default to Happy Path Scenario 1 when no scenario is specified in the request" in new Setup {

      given(underTest.scenarioLoader.loadScenario[IndividualTaxResponse](anyString, anyString)(any()))
        .willReturn(Future.successful(individualTaxResponse))
      given(underTest.service.create(anyString, anyString, any[IndividualTaxResponse]))
        .willReturn(Future.successful(individualTax))

      val result: Future[Result] = Future(underTest.create(utr, taxYear)(emptyRequest)).futureValue

      status(result) shouldBe CREATED
      verify(underTest.scenarioLoader).loadScenario[IndividualTaxResponse]("individual-tax", "HAPPY_PATH_1")
      verify(underTest.service).create(validUtrString, taxYear.startYr, individualTaxResponse)
    }

    "return an invalid server error when the repository fails" in new Setup {

      given(underTest.scenarioLoader.loadScenario[IndividualTaxResponse](anyString, anyString)(any()))
        .willReturn(Future.successful(individualTaxResponse))
      given(underTest.service.create(anyString, anyString, any[IndividualTaxResponse]))
        .willReturn(Future.failed(new RuntimeException("expected test error")))

      val result: Future[Result] = Future(underTest.create(utr, taxYear)(createSummaryRequest("HAPPY_PATH_1"))).futureValue

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return a bad request when the scenario is invalid" in new Setup {

      given(underTest.scenarioLoader.loadScenario[IndividualTaxResponse](anyString, anyString)(any()))
        .willReturn(Future.failed(new InvalidScenarioException("INVALID")))

      val result: Future[Result] = Future(underTest.create(utr, taxYear)(createSummaryRequest("INVALID"))).futureValue

      status(result) shouldBe BAD_REQUEST
      (contentAsJson(result) \ "code").as[String] shouldBe "UNKNOWN_SCENARIO"
    }
  }
}
