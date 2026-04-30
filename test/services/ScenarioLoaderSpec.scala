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

package services

import models.*
import org.scalatest.concurrent.Futures.whenReady
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import util.ResourceLoader.*

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class ScenarioLoaderSpec extends AnyWordSpec with Matchers {

  private val scenarioLoader: ScenarioLoader = new ScenarioLoader

  private val happyPath: String = loadResource("/public/scenarios/individual-tax/HAPPY_PATH_1.json")

  "ScenarioLoader" when {
    "loadScenario" should {
      "return Happy Path when valid scenario is supplied" in {
        val result = scenarioLoader.loadScenario[IndividualTaxResponse]("individual-tax", "HAPPY_PATH_1")
        result.map(r => r.toString shouldBe happyPath)
      }

      "return InvalidScenarioException when invalid scenario is supplied" in {
        val result = scenarioLoader.loadScenario[IndividualTaxResponse]("individual-tax", "HAPPY")
        result.map(_ shouldBe new InvalidScenarioException("HAPPY"))
      }
    }
    "loadScenarioWithTransformedPayload" should {
      "return Happy Path when valid scenario is supplied" in {
        val result = scenarioLoader.loadScenarioWithTransformedPayload("individual-child-benefits", "HAPPY_PATH_1")
        val res = Await.result(result, Duration.Inf)
        res._1.elements.headOption shouldBe Some(IndividualChildBenefitsResponseDetail(450.99))
        res._2.expectedJson shouldBe Some(Json.obj("childBenefitEntitlement" -> 450.99))
        res._2.expectedStatus shouldBe 200
      }
      "return Happy Path when valid scenario is supplied without json payload" in {
        val result = scenarioLoader.loadScenarioWithTransformedPayload("individual-child-benefits", "HAPPY_PATH_2")
        val res = Await.result(result, Duration.Inf)
        res._1.elements.headOption shouldBe Some(IndividualChildBenefitsResponseDetail(0.00))
        res._2.expectedJson shouldBe None
        res._2.expectedStatus shouldBe 404        
      }

      "return InvalidScenarioException when invalid scenario is supplied" in {
        val result = scenarioLoader.loadScenarioWithTransformedPayload("individual-tax", "HAPPY")
        result.map(_ shouldBe new InvalidScenarioException("HAPPY"))
      }
    }
  }
}
