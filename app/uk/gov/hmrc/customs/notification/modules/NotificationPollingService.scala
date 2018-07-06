/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.customs.notification.modules

import akka.actor.ActorSystem
import javax.inject._
import uk.gov.hmrc.customs.notification.repo.ClientNotificationRepo
import uk.gov.hmrc.customs.notification.services.NotificationDispatcher
import uk.gov.hmrc.customs.notification.services.config.ConfigService

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


@Singleton
class NotificationPollingService @Inject() (config: ConfigService,
                                            actorSystem: ActorSystem,
                                            clientNotificationRepo: ClientNotificationRepo,
                                            notificationDispatcher: NotificationDispatcher)(implicit executionContext: ExecutionContext) {

  actorSystem.scheduler.schedule(initialDelay = 0.seconds, interval = 5.seconds) {
    val eventualIds = clientNotificationRepo.fetchDistinctNotificationCSIDsWhichAreNotLocked()
    eventualIds.map(csIdSet => {
      val eventualUnit = notificationDispatcher.process(csIdSet)
      eventualUnit
    })
  }
}