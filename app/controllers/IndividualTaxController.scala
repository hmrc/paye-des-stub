/*
 * Copyright 2022 HM Revenue & Customs
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

import models.{CreateSummaryRequest, IndividualTaxResponse, InvalidScenarioException, JsonErrorResponse, TaxYear}

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.{IndividualTaxSummaryService, ScenarioLoader}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton
class IndividualTaxController @Inject() (
  val scenarioLoader: ScenarioLoader,
  val service: IndividualTaxSummaryService,
  val cc: ControllerComponents
) extends BackendController(cc)
    with HeaderValidator {

  implicit val ec: ExecutionContext = cc.executionContext

  final def find(utr: String, taxYear: String): Action[AnyContent] = Action async {
    service.fetch(utr, taxYear) map {
      case Some(result) => Ok(Json.toJson(result.individualTaxResponse))
      case _            => NotFound
    } recover { case _ =>
      InternalServerError
    }
  }

  final def create(utr: SaUtr, taxYear: TaxYear): Action[JsValue] =
    (cc.actionBuilder andThen validateAcceptHeader("1.0")).async(parse.json) { implicit request =>
      withJsonBody[CreateSummaryRequest] { createSummaryRequest =>
        val scenario = createSummaryRequest.scenario.getOrElse("HAPPY_PATH_1")

        for {
          individualTax <- scenarioLoader.loadScenario[IndividualTaxResponse]("individual-tax", scenario)
          _             <- service.create(utr.utr, taxYear.startYr, individualTax)
        } yield Created(Json.toJson(individualTax))

      } recover {
        case _: InvalidScenarioException => BadRequest(JsonErrorResponse("UNKNOWN_SCENARIO", "Unknown test scenario"))
        case _                           => InternalServerError
      }
    }

}
