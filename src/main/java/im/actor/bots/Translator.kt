package im.actor.bots

import im.actor.bot.BotMessages
import im.actor.botkit.RemoteBot
import im.actor.bots.magic.MagicCommandFork
import im.actor.bots.magic.MagicForkBot
import im.actor.bots.parser.MessageCommand
import im.actor.bots.parser.ParsedMessage
import im.actor.bots.translate.TranslatingContext
import im.actor.bots.translate.tryCreateTranslator

/**
 * Translator bot
 */
class Translator(baseBot: RemoteBot, chat: BotMessages.OutPeer) : MagicForkBot(baseBot, chat) {

    val context = TranslatingContext()

    override fun preStart() {
        super.preStart()

        context.engine = null;
    }

    override fun onReceive(message: ParsedMessage?, sender: BotMessages.UserOutPeer?) {

        if (message is MessageCommand) {
            when (message.command) {
                "register" -> fork(RegisterCommand::class.java, context)
                else -> sendTextMessage("Before translation, you need to provide credentials. Type /register to start registration.")
            }
        } else {
            sendUnknownCommand()
        }
    }

    override fun onForkClosed() {
        if (context.engine != null) {
            fork(RegisteredTranslator::class.java, context)
        }
    }

    class RegisteredTranslator(val context: TranslatingContext, baseBot: RemoteBot?, chatPeer: BotMessages.OutPeer?) : MagicForkBot(baseBot, chatPeer) {

        override fun onReceive(command: String, args: String?, sender: BotMessages.UserOutPeer): Boolean {
            if (context.engine == null) {
                dismissFork()
                return true
            }
            when (command) {
                "translate" -> {
                    sendTextMessage(context.engine!!.translate(args!!))
                    return true
                }
                "start" -> {
                    fork(ContiniousTranslation::class.java, context)
                    return true
                }
            }

            return false
        }
    }

    class ContiniousTranslation(val context: TranslatingContext, baseBot: RemoteBot?, chatPeer: BotMessages.OutPeer?) : MagicForkBot(baseBot, chatPeer) {

        init {
            welcomeMessage = "Continious translation started. Send /cancel to stop."
        }

        override fun onReceive(text: String, sender: BotMessages.UserOutPeer) {
            sendTextMessage(context.engine!!.translate(text))
        }

        override fun onReceive(command: String, args: String?, sender: BotMessages.UserOutPeer): Boolean {
            return false
        }
    }

    class RegisterCommand(val context: TranslatingContext, baseBot: RemoteBot?, chatPeer: BotMessages.OutPeer?) : MagicCommandFork("register", baseBot, chatPeer) {

        var clientId: String? = null
        var clientSecret: String? = null

        init {
            welcomeMessage = "Please provide required credentials. First of all Microsoft Translator Client Id."
        }

        override fun onStarted() {
            super.onStarted()

            // Resetting client
            context.engine = null
        }

        override fun onReceive(text: String, sender: BotMessages.UserOutPeer) {
            if (clientId == null) {
                clientId = text
                sendTextMessage("OK, ClientId '$clientId' set. Now Client Secret")
            } else if (clientSecret == null) {
                clientSecret = text
                val translator = tryCreateTranslator(clientId!!, clientSecret!!)
                if (translator == null) {
                    clientId = null
                    clientSecret = null
                    sendTextMessage("Unable to log in to Microsoft translator service.\nPlease, enter Client Id again or /cancel to abort registration.")
                } else {
                    context.engine = translator
                    sendTextMessage("Success! Now you can to translations. Type /help for more information.")
                    dismissFork()
                }
            }
        }
    }
}