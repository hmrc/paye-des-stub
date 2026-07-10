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

package repositories

import models.*
import org.mongodb.scala.model.*
import org.mongodb.scala.model.Filters.*
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WinterFuelPaymentAmountRepository @Inject() (mongo: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[WinterFuelPaymentAmount](
      mongoComponent = mongo,
      collectionName = "WinterFuelPaymentAmount",
      domainFormat = formatWinterFuelPaymentAmount,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("nino", "taxYear"),
          IndexOptions().name("winter-fuel-payment-amount-nino-taxYear").unique(true)
        )
      )
    ) {

  private def filter(nino: String, taxYear: String) =
    Filters.and(
      Filters.equal("nino", nino),
      Filters.equal("taxYear", taxYear)
    )

  def store[T <: WinterFuelPaymentAmount](winterFuelPaymentAmount: T): Future[T] =
    collection
      .replaceOne(
        filter = filter(winterFuelPaymentAmount.nino, winterFuelPaymentAmount.taxYear),
        replacement = winterFuelPaymentAmount,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => winterFuelPaymentAmount)

  def fetch(nino: String, taxYear: String): Future[Option[WinterFuelPaymentAmount]] =
    collection.find(filter(nino, taxYear)).headOption()
}
