/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.payedesstub.controllers

import play.api.libs.json.Json
import play.api.mvc.{Request, Result, Results}
import uk.gov.hmrc.payedesstub.models.ErrorResponse

trait ErrorConversion {

  import Results._

  implicit def toResult[T](error: ErrorResponse)(implicit request: Request[T]): Result = Status(error.httpStatusCode)(Json.toJson(error))
}
