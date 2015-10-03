package im.actor.bots

import im.actor.bot.BotMessages
import im.actor.botkit.RemoteBot
import im.actor.bots.magic.MagicCommandFork
import im.actor.bots.magic.MagicForkBot
import im.actor.bots.tools.any
import java.util.*

/**
 * Main Bot manager
 */

class ActoBot(baseBot: RemoteBot, chat: BotMessages.OutPeer) : MagicForkBot(baseBot, chat) {

    var settings = createKeyValue("settings_" + chat.type() + "_" + chat.id())
    val context = ActoBotContext()

    init {
        startMessage = "Hi! I am simple actor bot, i am pretty stupid, but tries to became much more helpful."
    }

    override fun onReceive(text: String, sender: BotMessages.UserOutPeer) {
        for (s in context.keys) {
            if (text.contains(s)){
                sendTextMessage(context.sentences.any())
                return
            }
        }
    }

    override fun onReceive(command: String, args: String?, sender: BotMessages.UserOutPeer): Boolean {
        when (command) {
            "update_rules" -> {
                fork(UpdateRulesFork::class.java, context)
            }
        }

        // Not showing unsupported message
        return true
    }

    class UpdateRulesFork(val context: ActoBotContext, baseBot: RemoteBot, chat: BotMessages.OutPeer) : MagicCommandFork("update_rules", baseBot, chat) {

        var isEnteringKeys = true
        var keys: MutableList<String> = ArrayList<String>()
        var sentences: MutableList<String> = ArrayList<String>()

        init {
            welcomeMessage = "Please, enter keys"
            setIsCommandsSupported(true)
        }

        override fun onReceive(text: String, sender: BotMessages.UserOutPeer) {
            if (isEnteringKeys) {
                keys.add(text)
            } else {
                sentences.add(text)
            }
            sendTextMessage("Noted.")
        }

        override fun onReceive(command: String, args: String?, sender: BotMessages.UserOutPeer): Boolean {
            if (command == "end") {
                if (isEnteringKeys) {
                    if (keys.count() == 0) {
                        sendTextMessage("You haven't entered any keys. Try again.")
                    } else {
                        sendTextMessage("Success. Now Send me sentences.")
                        isEnteringKeys = false
                    }
                } else {
                    if (keys.count() == 0) {
                        sendTextMessage("You haven't entered any sentences. Try again.")
                    } else {
                        sendTextMessage("Success.")

                        // TODO: Save sentences
                        context.keys = keys
                        context.sentences = keys

                        dismissFork()
                    }
                }
                return true
            }
            return false
        }
    }
}

class ActoBotContext {
    public var keys: List<String> = ArrayList<String>()
    public var sentences: List<String> = ArrayList<String>()
}