package im.actor.bots

import akka.actor.Props
import im.actor.bot.BotMessages
import im.actor.botkit.RemoteBot
import im.actor.bots.magic.MagicCommandFork
import im.actor.bots.magic.MagicForkBot
import im.actor.bots.magic.MagicMergedBot
import im.actor.bots.tools.any
import im.actor.bots.translate.Translator
import java.util.*

/**
 * Main Bot manager
 */

class ActoBot(baseBot: RemoteBot, chat: BotMessages.OutPeer) : MagicMergedBot(baseBot, chat) {

    init {
        startMessage = "Hi! I am Actor Bot and i can do various funny and usefull stuff for you. First of all, you can use this " +
                "conversation to store any personal information, no one will see this except you.\n" +
                "\n" +
                "I can remind you about important things to do, help in translating chats or you can" +
                "configure me to respond to your messages.\n" +
                "I have catalog of public groups where you can join (or create yours) and met some new people.\n" +
                "\n" +
                "For more info send what do you want to do:\n" +
                "- */public_help* - read how you can find public groups or create new one.\n" +
                "- */remind_help* - read more about how i can remind your things to do\n" +
                "- */poll_help* - I also can organize polls in your groups.\n" +
                "- */translate_help* - read more about my translation features\n" +
                "- */response_help* - read more how you can configure me to make funny responses\n" +
                "- */develper_help* - if you can write programs, you can ask me to tell you a bit " +
                "more about how to work with me\n" +
                "\n" +
                "This is not full list of things that i can do, but most important ones."
    }

    override fun initCommands() {
        addBotMerge(context().actorOf(Props.create(Translator::class.java, baseBot, chatPeer)),
                "translate", "translate_start", "translate_langs", "translate_help")
    }
}

class ActoBot2(baseBot: RemoteBot, chat: BotMessages.OutPeer) : MagicForkBot(baseBot, chat) {

    var settings = createKeyValue("settings_" + chat.type() + "_" + chat.id())
    val context = ActoBotContext()

    init {
        startMessage = "Hi! I am simple actor bot, i am pretty stupid, but tries to became much more helpful."
    }

    override fun onReceive(text: String, sender: BotMessages.UserOutPeer) {
        for (s in context.keys) {
            if (text.contains(s)) {
                sendTextMessage(context.sentences.any())
                return
            }
        }
    }

    override fun onReceive(command: String, args: Array<out String>, text: String, sender: BotMessages.UserOutPeer): Boolean {
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

        override fun onReceive(command: String, args: Array<out String>, text: String, sender: BotMessages.UserOutPeer): Boolean {
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