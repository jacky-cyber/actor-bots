package im.actor.bots

import im.actor.bot.BotMessages
import im.actor.botkit.RemoteBot
import im.actor.bots.magic.MagicCommandFork
import im.actor.bots.magic.MagicForkBot

/**
 * Main Bot manager
 */
class BotFather(baseBot: RemoteBot, chat: BotMessages.OutPeer) : MagicForkBot(baseBot, chat) {

    init {
        startMessage = "I can help you create and manage your Actor bots. " +
                "Please, read [manual](https://actor.im) before we begin. " +
                "Feel free to ask any questions about bots in our OSS Group.\n\n" +
                "You can control me by sending these commands:\n" +
                "/newbot - creating new bot\n" +
                //            "/setavatar - setting bot's avatar\n" +
                "/setname - setting bot's visible name\n" +
                //            "/setabout - setting bot about info\n" +
                "/cancel - cancelling current action"
    }

    override fun onReceive(command: String, args: Array<out String>, text: String, sender: BotMessages.UserOutPeer): Boolean {

        when (command) {
            "newbot" -> {
                fork(NewBotFork::class.java)
                return true
            }
            "setname" -> {
                fork(AskNameFork::class.java)
                return true
            }
        }

        return false
    }

    /**
     * Create new bot fork
     */
    class NewBotFork(baseBot: RemoteBot, chat: BotMessages.OutPeer) : MagicCommandFork("newbot", baseBot, chat) {

        var name: String? = null
        var nickname: String? = null

        init {
            this.welcomeMessage = "New bot? Alright. How we will name it?"
        }

        override fun onReceive(text: String, sender: BotMessages.UserOutPeer) {
            if (name == null) {
                if (text.length() < 3) {
                    sendTextMessage("Sorry, but bot name might be at least 3 letters")
                    return
                }
                name = text
                sendTextMessage("Good. Now let's choose a username for your bot.")
            } else if (nickname == null) {
                if (text.length() < 5) {
                    sendTextMessage("Sorry, but bot username might be at least 5 letters")
                    return
                }
                if (text.length() > 32) {
                    sendTextMessage("Sorry, but bot username can't be longer than 32 letters")
                    return
                }

                // TODO: Implement registration
                dismissFork()

                sendTextMessage("Success!")
            }
        }
    }

    /**
     * Bot rename fork
     */
    class AskNameFork(baseBot: RemoteBot, chat: BotMessages.OutPeer) : MagicCommandFork("setname", baseBot, chat) {

        var username: String? = null

        init {
            this.welcomeMessage = "Which bot need to be renamed? Please enter nickname with @ symbol."
        }

        override fun onReceive(text: String, sender: BotMessages.UserOutPeer) {
            if (username == null) {

                // Asking user name
                if (text != "@jane") {
                    sendTextMessage("Can't find this bot")
                } else {
                    username = text.substring(1)
                    sendTextMessage("Ok, new name for `Jane`?")
                }
            } else {

                // Asking for new name
                if (text.length() < 3) {
                    sendTextMessage("Sorry, but bot name might be at least 3 letters")
                    return
                }

                // TODO: Perform rename

                dismissFork()

                sendTextMessage("Success!")
            }
        }
    }
}