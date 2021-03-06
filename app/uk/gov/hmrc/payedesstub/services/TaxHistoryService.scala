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

package uk.gov.hmrc.payedesstub.services

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.payedesstub.models.{TaxHistory, TaxYear}
import uk.gov.hmrc.payedesstub.repositories.TaxHistoryRepository

import scala.concurrent.Future

@Singleton
class TaxHistoryService @Inject()(val repository: TaxHistoryRepository) {

  def create(nino: Nino,
      taxYear: TaxYear,
      taxHistoryResponse: String
    ): Future[TaxHistory] = {
      repository.store(TaxHistory(nino.nino, taxYear.startYr, taxHistoryResponse))
  }

  def fetch(nino: Nino, taxYear: Int): Future[Option[TaxHistory]] = {
    repository.fetch(nino.nino, taxYear.toString)
  }
}