/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers

import models.*
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.BDDMockito.`given`
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{ScenarioLoader, WinterFuelPaymentAmountSummaryService}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{GatewayTimeoutException, HeaderCarrier}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class WinterFuelPaymentAmountControllerSpec
    extends AnyWordSpecLike
    with Matchers
    with OptionValues
    with ScalaFutures
    with GuiceOneAppPerSuite {

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    def createWinterFuelPaymentAmountRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      .withHeaders("Accept" -> "application/vnd.hmrc.2.1+json", "Content-Type" -> "application/vnd.hmrc.1.0+json")

    val underTest = new WinterFuelPaymentAmountController(
      Mockito.mock(classOf[ScenarioLoader]),
      Mockito.mock(classOf[WinterFuelPaymentAmountSummaryService]),
      stubControllerComponents()
    )

    def createSummaryRequest(scenario: String): FakeRequest[JsValue] =
      createWinterFuelPaymentAmountRequest.withBody[JsValue](Json.parse(s"""{ "scenario": "$scenario" }"""))

    def emptyRequest: FakeRequest[JsValue] =
      createWinterFuelPaymentAmountRequest.withBody[JsValue](Json.parse("{}"))

    val validNinoString                                                          = "AA111111A"
    val validTaxYearString                                                       = "2016-17"
    val nino: Nino                                                               = Nino(validNinoString)
    val taxYear: TaxYear                                                         = TaxYear(validTaxYearString)
    val winterFuelPaymentAmountResponse: WinterFuelPaymentAmountResponse         = WinterFuelPaymentAmountResponse(
      Seq(WinterFuelPaymentAmountResponseDetail(BigDecimal(23.33)))
    )
    val winterFuelPaymentAmountPostResponse: WinterFuelPaymentAmountPostResponse = WinterFuelPaymentAmountPostResponse(
      expectedStatus = 200,
      expectedJson = Some(Json.obj("test" -> "test"))
    )
    val winterFuelPaymentAmount500Response: WinterFuelPaymentAmountResponse      =
      WinterFuelPaymentAmountResponse(Nil, Some(500))
    val winterFuelPaymentAmount: WinterFuelPaymentAmount                         =
      WinterFuelPaymentAmount("", "", winterFuelPaymentAmountResponse)
  }

  "fetch" should {
    "return 200 (OK) with the happy path response when called with a nino and taxYear that are found" in new Setup {

      `given`(underTest.service.fetch(validNinoString, validTaxYearString))
        .willReturn(
          Future(
            Some(
              WinterFuelPaymentAmount(
                "",
                "",
                WinterFuelPaymentAmountResponse(Seq(WinterFuelPaymentAmountResponseDetail(BigDecimal(23.33))))
              )
            )
          )
        )

      val result: Future[Result] =
        Future(underTest.find(validNinoString, validTaxYearString)(createWinterFuelPaymentAmountRequest)).futureValue

      status(result) shouldBe OK
    }

    "return 404 (NOT_FOUND) when called with a nino and taxYear that are not found" in new Setup {

      `given`(underTest.service.fetch(validNinoString, validTaxYearString)).willReturn(Future(None))

      val result: Future[Result] =
        Future(underTest.find(validNinoString, validTaxYearString)(createWinterFuelPaymentAmountRequest)).futureValue

      status(result) shouldBe NOT_FOUND
    }

    "return 500 (INTERNAL_SERVER_ERROR) for failure from a GatewayTimeoutException" in new Setup {

      `given`(underTest.service.fetch(validNinoString, validTaxYearString))
        .willReturn(Future.failed(new GatewayTimeoutException("Expected timeout")))

      val result: Future[Result] =
        Future(underTest.find(validNinoString, validTaxYearString)(createWinterFuelPaymentAmountRequest)).futureValue

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "create" should {

    "return a created response and store the Individual Benefits summary" in new Setup {
      `given`(underTest.scenarioLoader.loadScenarioWithTransformedPayloadWFPA(anyString, anyString))
        .willReturn(Future.successful(Tuple2(winterFuelPaymentAmountResponse, winterFuelPaymentAmountPostResponse)))
      `given`(underTest.service.create(anyString, anyString, any[WinterFuelPaymentAmountResponse]))
        .willReturn(Future.successful(winterFuelPaymentAmount))

      val result: Future[Result] =
        Future(underTest.create(nino, taxYear)(createSummaryRequest("HAPPY_PATH_1"))).futureValue

      status(result)        shouldBe CREATED
      contentAsJson(result) shouldBe Json.toJson(winterFuelPaymentAmountPostResponse)
      verify(underTest.scenarioLoader).loadScenarioWithTransformedPayloadWFPA(anyString, anyString)
      verify(underTest.service).create(validNinoString, taxYear.startYr, winterFuelPaymentAmountResponse)
    }
    "return a created response and store the Individual Benefits summary for unhappy path" in new Setup {
      `given`(underTest.scenarioLoader.loadScenarioWithTransformedPayloadWFPA(anyString, anyString))
        .willReturn(Future.successful(Tuple2(winterFuelPaymentAmountResponse, winterFuelPaymentAmountPostResponse)))
      `given`(underTest.service.create(anyString, anyString, any[WinterFuelPaymentAmountResponse]))
        .willReturn(Future.successful(winterFuelPaymentAmount))

      val result: Future[Result] =
        Future(underTest.create(nino, taxYear)(createSummaryRequest("UNHAPPY_PATH_500"))).futureValue

      status(result)        shouldBe CREATED
      contentAsJson(result) shouldBe Json.toJson(WinterFuelPaymentAmountPostResponse(500))
      verify(underTest.service).create(validNinoString, taxYear.startYr, winterFuelPaymentAmount500Response)
    }

    "default to Happy Path Scenario 1 when no scenario is specified in the request" in new Setup {

      `given`(underTest.scenarioLoader.loadScenarioWithTransformedPayloadWFPA(anyString, anyString))
        .willReturn(Future.successful(Tuple2(winterFuelPaymentAmountResponse, winterFuelPaymentAmountPostResponse)))
      `given`(underTest.service.create(anyString, anyString, any[WinterFuelPaymentAmountResponse]))
        .willReturn(Future.successful(winterFuelPaymentAmount))

      val result: Future[Result] = Future(underTest.create(nino, taxYear)(emptyRequest)).futureValue

      status(result)        shouldBe CREATED
      contentAsJson(result) shouldBe Json.toJson(winterFuelPaymentAmountPostResponse)
      verify(underTest.scenarioLoader).loadScenarioWithTransformedPayloadWFPA(anyString, anyString)
      verify(underTest.service).create(validNinoString, taxYear.startYr, winterFuelPaymentAmountResponse)
    }

    "return an invalid server error when the repository fails" in new Setup {

      `given`(underTest.scenarioLoader.loadScenarioWithTransformedPayloadWFPA(anyString, anyString))
        .willReturn(Future.successful(Tuple2(winterFuelPaymentAmountResponse, winterFuelPaymentAmountPostResponse)))
      `given`(underTest.service.create(anyString, anyString, any[WinterFuelPaymentAmountResponse]))
        .willReturn(Future.failed(new RuntimeException("expected test error")))

      val result: Future[Result] =
        Future(underTest.create(nino, taxYear)(createSummaryRequest("HAPPY_PATH_1"))).futureValue

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return 406 (NOT_ACCEPTABLE) for an invalid accept header" in new Setup {

      `given`(underTest.scenarioLoader.loadScenarioWithTransformedPayloadWFPA(anyString, anyString))
        .willReturn(Future.successful(Tuple2(winterFuelPaymentAmountResponse, winterFuelPaymentAmountPostResponse)))
      `given`(underTest.service.create(anyString, anyString, any[WinterFuelPaymentAmountResponse]))
        .willReturn(Future.successful(winterFuelPaymentAmount))

      val result: Future[Result] = Future(
        underTest.create(nino, taxYear)(emptyRequest.withHeaders("Accept" -> "application/vnd.hmrc.0.9+json"))
      ).futureValue

      status(result) shouldBe NOT_ACCEPTABLE
    }

    "return a bad request when the scenario is invalid" in new Setup {

      `given`(underTest.scenarioLoader.loadScenarioWithTransformedPayloadWFPA(anyString, anyString))
        .willReturn(Future.failed(new InvalidScenarioException("INVALID")))

      val result: Future[Result] = Future(underTest.create(nino, taxYear)(createSummaryRequest("INVALID"))).futureValue

      status(result)                              shouldBe BAD_REQUEST
      (contentAsJson(result) \ "code").as[String] shouldBe "UNKNOWN_SCENARIO"
    }
  }
}
