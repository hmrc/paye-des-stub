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

package repositories

import models._
import org.mongodb.scala.model.Filters.{and, equal}
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.mongo.MongoComponent

import scala.concurrent.{ExecutionContext, Future}
import org.mongodb.scala.{ObservableFuture, SingleObservableFuture}

@Singleton
class IndividualTaxRepository @Inject() (mongo: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[IndividualTax](
      mongoComponent = mongo,
      collectionName = "individualTax",
      domainFormat = formatIndividualTax,
      indexes = Seq.empty
    ) {

  def store[T <: IndividualTax](IndividualTax: T): Future[T] =
    collection.insertOne(IndividualTax).toFuture().map(_ => IndividualTax)

  def fetch(utr: String, taxYear: String): Future[Option[IndividualTax]] =
    collection.find(and(equal("utr", utr), equal("taxYear", taxYear))).headOption()
}
