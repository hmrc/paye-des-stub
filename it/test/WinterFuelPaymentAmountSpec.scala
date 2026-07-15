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

import helpers.BaseSpec
import org.mongodb.scala.SingleObservableFuture
import play.api.http.HeaderNames
import play.api.http.Status.{CREATED, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.libs.ws.StandaloneWSRequest
import repositories.WinterFuelPaymentAmountRepository

import scala.concurrent.Await.result

class WinterFuelPaymentAmountSpec extends BaseSpec {

  override def postEndpoint(endpoint: String, payload: String): StandaloneWSRequest#Response =
    result(
      awaitable = post(
        s"$serviceUrl/$endpoint",
        payload,
        Seq((HeaderNames.CONTENT_TYPE, "application/json"), (HeaderNames.ACCEPT, "application/vnd.hmrc.2.1+json"))
      ),
      atMost = timeout
    )
  Feature("Fetch winter fuel payment data") {
    Scenario("No data is returned because nino and taxYear are not found") {
      When("I request winter fuel payment data for a given nino and taxYear")
      val response = fetchWinterFuelPaymentAmountData("AA111111A", "2016-17")

      Then("The response should indicate that no data was found")
      response.status shouldBe NOT_FOUND
    }

    Scenario("Benefits summary data can be primed") {
      When("I post winter fuel payment summary data for a given nino and taxYear")
      val response = primeWinterFuelPaymentAmountData("AA111111A", "2016-17", """{ "scenario": "HAPPY_PATH_1" }""")

      Then("The response should indicate that the summary has been created")
      response.status shouldBe CREATED
    }

    Scenario(
      "Individual winter fuel payment summary data is returned for the given nino and taxYear when primed with the default scenario"
    ) {
      When("I prime tax data for a given nino and taxYear")
      val primeResponse = primeWinterFuelPaymentAmountData("AA111111A", "2016-17", "{}")

      Then("The response should contain individual winter fuel payment data")
      primeResponse.status shouldBe CREATED

      And("I request employment data for a given nino and taxYear")
      val fetchResponse = fetchWinterFuelPaymentAmountData("AA111111A", "2016")

      And("The response should contain individual winter fuel payment data")
      fetchResponse.status shouldBe OK
    }

    Scenario(
      "Individual winter fuel payment summary data is returned for the given ut and taxYear when primed with a specific scenario"
    ) {
      When("I prime tax data for a given nino, taxYear and test scenario")
      val primeResponse = primeWinterFuelPaymentAmountData("AA111111A", "2016-17", """{"scenario":"HAPPY_PATH_1"}""")

      Then("The response should contain winter fuel payment data")
      primeResponse.status shouldBe CREATED

      And("I request winter fuel payment data for a given nino and taxYear")
      val fetchResponse = fetchWinterFuelPaymentAmountData("AA111111A", "2016")

      And("The response should contain individual winter fuel payment data")
      fetchResponse.status shouldBe OK
    }
    Scenario(
      "Individual winter fuel payment summary data is returned for the given ut and taxYear when primed with a specific UNHAPPY path scenario"
    ) {
      When("I prime tax data for a given nino, taxYear and test scenario")
      val primeResponse =
        primeWinterFuelPaymentAmountData("AA111111A", "2016-17", """{"scenario":"UNHAPPY_PATH_500"}""")

      Then("The response should contain individual winter fuel payment data")
      primeResponse.status shouldBe CREATED

      And("I request employment data for a given nino and taxYear")
      val fetchResponse = fetchWinterFuelPaymentAmountData("AA111111A", "2016")

      And("The response should contain individual winter fuel payment data")
      fetchResponse.status shouldBe INTERNAL_SERVER_ERROR
    }
  }

  private def primeWinterFuelPaymentAmountData(
    nino: String,
    taxYear: String,
    payload: String
  ): StandaloneWSRequest#Response =
    postEndpoint(s"$nino/winter-fuel-payment-amount/annual-summary/$taxYear", payload)

  val deductionType                                                                                         = "Winter Payment Amount (132)"
  private def fetchWinterFuelPaymentAmountData(nino: String, taxYear: String): StandaloneWSRequest#Response =
    getEndpoint(s"paye/iabd/taxpayer/$nino/tax-year/$taxYear/deductions?type=$deductionType")

  override protected def beforeEach(): Unit = {
    val repository = app.injector.instanceOf[WinterFuelPaymentAmountRepository]
    result(repository.collection.drop().toFuture(), timeout)
    result(repository.ensureIndexes(), timeout)
  }
}
