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

package unit.controllers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.OptionValues
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.payedesstub.controllers.Binders

class UtrBinderSpec extends AnyWordSpecLike with Matchers with OptionValues {

  "a valid utr '2234567890'" should {
    "be transformed to an SaUtr object" in {
      val utr = "2234567890"
      Binders.saUtrBinder.bind("utr", utr) shouldBe Right(SaUtr(utr))
    }
  }

  "an invalid utr 'invalid'" should {
    "be transformed to a String error message" in {
      val utr = "invalid"
      Binders.saUtrBinder.bind("utr", utr) shouldBe Left("ERROR_SA_UTR_INVALID")
    }
  }

}
