package im.actor.bots

import im.actor.bot.BotMessages
import im.actor.bots.parser.MessageCommand
import im.actor.bots.parser.MessageType
import im.actor.bots.util.BaseBot
import im.actor.bots.util.StaticWizard

class BotFather(token: String, endpoint: String) : BaseBot(token, endpoint) {

    override fun onMessage(msg: MessageType?, baseMessage: BotMessages.TextMessage): Boolean {
        if (msg is MessageCommand) {
            when (msg.command) {
                "newbot" -> {
                    val wizard = StaticWizard(this, baseMessage.peer(), baseMessage.sender())
                    wizard.cancelMessage = "Oops, thats embarassing. Creating of bot is cancelled."
                    wizard.addStep("Ok, first of all we need some name for it. Please choose a name for it.")
                    wizard.addStep("Ok, Good name. Now we need a nickname.")
                    wizard.addStep("Bot created! Anything other?")
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