/*
 * Copyright 2025 HM Revenue & Customs
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

package models

import play.api.http.Status.NOT_ACCEPTABLE
import play.api.libs.json.{Json, Writes}

sealed case class ErrorResponse(httpStatusCode: Int, errorCode: String, message: String)

object ErrorAcceptHeaderInvalid
    extends ErrorResponse(NOT_ACCEPTABLE, "ACCEPT_HEADER_INVALID", "The accept header is missing or invalid")

object ErrorResponse {
  implicit val errorResponseWrites: Writes[ErrorResponse] = (e: ErrorResponse) =>
    Json.obj("code" -> e.errorCode, "message" -> e.message)

}
