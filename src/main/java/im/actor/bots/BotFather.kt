package im.actor.bots

import im.actor.bot.BotMessages
import im.actor.bots.parser.MessageCommand
import im.actor.bots.parser.MessageType
import im.actor.bots.util.MagicBot
import im.actor.bots.util.StaticWizard

class BotFather(token: String, endpoint: String) : MagicBot(token, endpoint) {

    override fun onMessage(msg: MessageType?, baseMessage: BotMessages.TextMessage): Boolean {
        if (msg is MessageCommand) {
            when (msg.command) {
                "newbot" -> {
                    val wizard = StaticWizard(this, baseMessage.peer(), baseMessage.sender())
                    wizard.cancelMessage = "Oops, that's embarrassing. Creating of bot is cancelled.\n\nSend /help for a list of commands."
                    wizard.addStep("Новый бот? Хорошо. Как мы его назовем?")
                    wizard.addStep("Отлично. Теперь нужно придумать ему ник.")
                    wizard.addStep("Бот создан! Ваш токен для активации: <?????>")
                    startWizard(wizard)
                    return true
                }
            }
        }

        return false
    }

    override fun onHelpRequested(baseMessage: BotMessages.TextMessage) {
        sendTextMessage(baseMessage.peer(),
                "I can help you create and manage your Actor bots. " +
                        "Please, read [manual](https://actor.im) before we begin. " +
                        "Feel free to ask any questions about bots in our OSS Group.\n\n" +
                        "You can control me by sending these commands:\n" +
                        "/newbot - creating new bot\n" +
                        "/setavatar - setting bot's avatar\n" +
                        "/setname - setting bot's visible name\n" +
                        "/setabout - setting bot about info\n" +
                        "/cancel - cancelling current action")
    }
}