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

package helpers

import org.apache.pekko.actor.ActorSystem
import play.api.libs.ws.StandaloneWSRequest
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import play.api.libs.ws.DefaultBodyWritables.writeableOf_String

import scala.concurrent.Future

trait HttpClient {
  implicit val actorSystem: ActorSystem = ActorSystem()

  private val wsClient: StandaloneAhcWSClient = StandaloneAhcWSClient()

  def get(url: String, headers: Seq[(String, String)] = Seq()): Future[StandaloneWSRequest#Response] =
    wsClient
      .url(url)
      .withHttpHeaders(headers*)
      .get()

  def post(
    url: String,
    requestBody: String,
    headers: Seq[(String, String)]
  ): Future[StandaloneWSRequest#Response] = wsClient.url(url).withHttpHeaders(headers*).post(requestBody)
}
