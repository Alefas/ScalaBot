/*
 * Copyright 2016 Nikolay Smelik
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

package scalabot.common.bot

import scalabot.common.message._
import scalabot.common.message.outcoming.{OutgoingMessage, TextMessage}

/**
  * Created by Nikolay.Smelik on 7/22/2016.
  */
trait BotState extends Serializable {
  val canChange: Boolean = true
  def handleIntent: Intent => Reply
  def apply(intent: Intent): Reply = handleIntent(intent)
}

case class StateWithDefaultReply(defaultReply: OutgoingMessage) extends BotState {
  override val canChange: Boolean = false

  override def handleIntent: Intent => Reply = {
    intent: Intent => Reply(Exit)
      .withIntent(ReplyMessageIntent(intent.sender, defaultReply))
  }
}

case object StateWithDefaultReply {
  def apply(defaultReply: String): StateWithDefaultReply = StateWithDefaultReply(TextMessage(defaultReply))
}


case object Exit extends BotState {
  override val canChange: Boolean = false

  def handleIntent: Intent => Reply = { intent: Intent => Reply(Exit)}
}

case class MoveToConversation(newConversation: Conversation, intent: Intent = null) extends BotState {
  override val canChange: Boolean = false

  override def handleIntent = throw new IllegalStateException("Unsupported operation for this state")
}

case object BotState {
  def apply(function: PartialFunction[Intent, Reply], isCanChange: Boolean = true, hasExit: Boolean = true): BotState = new BotState with Serializable {
    override val canChange: Boolean = isCanChange
    val exitIntentFunc: PartialFunction[Intent, Reply] = {
      case TextIntent(sender, text) if text.matches("(?:E|e)xit conversation") =>
        Reply(Exit).withIntent(ReplyMessageIntent(sender, "Conversation was interrupted"))
    }

    val handleIntentFunc: PartialFunction[Intent, Reply] = {
      case _ => Reply(this)
    }

    override def handleIntent: PartialFunction[Intent, Reply] = {
      if (hasExit) exitIntentFunc orElse function orElse handleIntentFunc
      else function orElse handleIntentFunc
    }
  }
}



