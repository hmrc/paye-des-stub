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

package controllers

import models.TaxYear
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class TaxYearBinderSpec extends AnyWordSpecLike with Matchers with OptionValues {

  "a valid tax year '2014-15'" should {
    "be transformed to a TaxYear object" in {
      val ty = "2014-15"
      Binders.taxYearBinder.bind("taxYear", ty) shouldBe Right(TaxYear("2014-15"))
    }
  }

  "unbinding a TaxYear object" should {
    "result in a tax year string" in {
      val ty = "2014-15"
      Binders.taxYearBinder.unbind("taxYear", TaxYear(ty)) shouldBe ty
    }
  }

  "an invalid tax year 'invalid tax year'" should {
    "be transformed to a String error message" in {
      val ty = "invalid tax year"
      Binders.taxYearBinder.bind("taxYear", ty) shouldBe Left("ERROR_TAX_YEAR_INVALID")
    }
  }

}
