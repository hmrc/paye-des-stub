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

import scala.util.matching.Regex
import scala.util.matching.Regex.Match

case class TaxYear(ty: String) {
  if (!TaxYear.isValid(ty)) throw new IllegalArgumentException

  val startYr: String = ty.split("-")(0)
}

object TaxYear {

  private val taxYearRegex = """^(\d{4})-(\d{2})$"""

  private val matchTaxYear: String => Option[Match] = new Regex(taxYearRegex, "first", "second") findFirstMatchIn _

  def isValid(taxYearReference: String): Boolean = matchTaxYear(taxYearReference) exists { r =>
    (r.group("first").toInt + 1) % 100 == r.group("second").toInt
  }

}
