package im.actor.bots.translate

import im.actor.bot.BotMessages
import im.actor.botkit.RemoteBot
import im.actor.bots.magic.MagicCommandFork
import im.actor.bots.magic.MagicForkBot
import shardakka.keyvalue.SimpleKeyValueJava
import java.util.*

/**
 * Translator bot
 */
class Translator(baseBot: RemoteBot, chat: BotMessages.OutPeer) : MagicForkBot(baseBot, chat) {

    lateinit var context: TranslatingContext

    override fun preStart() {
        super.preStart()

        context = TranslatingContext(createKeyValue("translating"))

        startMessage = "Hi! I am here to help you with translating chat messages to up to ${context.supportedLanguages.count()} languages." +
                "I can do it in two different modes: translating sentence when you explicitly ask" +
                " me about this (with */translate* command) or auto translating all messages in conversation " +
                "(with */start_translate* command).\n" +
                "\n" +
                "All supported commands:\n" +
                "- */translate <text>* - translate <text> to english\n" +
                "- */translate(language) <text>* - translate <text> to specific language where 'language' is specific language code\n" +
                "- */translate_start* - starting translating every message to english\n" +
                "- */translate_start(language)* - starting translating every message to 'language'\n" +
                "- */translate_langs* - list all available languages"

        val clientId = context.settings.syncGet("clientId").orElse(null)
        val clientSecret = context.settings.syncGet("clientSecret").orElse(null)

        if (clientId != null && clientSecret != null) {
            context.engine = tryCreateTranslator(clientId, clientSecret)
        }

        checkEngine()
    }

    override fun onReceive(command: String, args: Array<out String>, text: String, sender: BotMessages.UserOutPeer): Boolean {

        val lang: String
        when (args.count()) {
            1 -> lang = args[0]
            else -> lang = "en"
        }

        if (!context.supportedLanguages.contains(lang)) {
            sendTextMessage("Unknown language code *$lang*. Please, type /translate_langs to list all available languages.")
            return true
        }

        when (command) {
            "translate" -> sendTextMessage(context.engine!!.translate(text, lang))
            "translate_start" -> fork(ContinuousTranslation::class.java, context, lang)
            "translate_langs" -> {
                val langs = context.supportedLanguages.toList().sortedBy { a -> a.second }
                var message = "Available languages and it's codes:"
                for (i in langs) {
                    message += "\n- ${i.second} - *${i.first}*"
                }
                sendTextMessage(message)
            }
            "translate_help" -> sendTextMessage(startMessage)
            else -> return false
        }

        return true
    }

    override fun onForkClosed() {
        checkEngine()
    }

    fun checkEngine() {
        if (context.engine == null) {
            fork(NotRegisteredFork::class.java, context)
        }
    }

    class ContinuousTranslation(val context: TranslatingContext, val lang: String, baseBot: RemoteBot?, chatPeer: BotMessages.OutPeer?) : MagicForkBot(baseBot, chatPeer) {

        init {
            welcomeMessage = "Success! I will translate everything you write to ${context.supportedLanguages[lang]} language until you send /cancel to stop."
        }

        override fun onReceive(text: String, sender: BotMessages.UserOutPeer) {
            sendTextMessage(context.engine!!.translate(text, lang))
        }

        override fun onReceive(command: String, args: Array<out String>, text: String, sender: BotMessages.UserOutPeer): Boolean {
            return false
        }

        override fun onCancelled() {
            super.onCancelled()

            sendTextMessage("Good. Stopping translation.")
        }
    }

    class NotRegisteredFork(val context: TranslatingContext, baseBot: RemoteBot?, chatPeer: BotMessages.OutPeer?) : MagicForkBot(baseBot, chatPeer) {
        override fun onReceive(command: String, args: Array<out String>, text: String, sender: BotMessages.UserOutPeer): Boolean {
            when (command) {
                "translate_configure" -> {
                    fork(RegisterCommand::class.java, context)
                    return true
                }
                else -> {
                    sendTextMessage("Before translation, you need to provide credentials. Senf */translate_configure* to start registration.")
                    return false
                }
            }
        }
    }

    class RegisterCommand(val context: TranslatingContext, baseBot: RemoteBot?, chatPeer: BotMessages.OutPeer?) : MagicCommandFork("translate_configure", baseBot, chatPeer) {

        var clientId: String? = null
        var clientSecret: String? = null

        init {
            welcomeMessage = "Translator Bot is not configured. Please provide required credentials to access Microsoft Translator API. First of all we need your Client Id."
        }

        override fun onStarted() {
            super.onStarted()

            // Resetting client
            context.engine = null
        }

        override fun onReceive(text: String, sender: BotMessages.UserOutPeer) {
            if (clientId == null) {
                clientId = text
                sendTextMessage("OK, ClientId '$clientId' set. Now Client Secret.")
            } else if (clientSecret == null) {
                clientSecret = text
                val translator = tryCreateTranslator(clientId!!, clientSecret!!)
                if (translator == null) {
                    clientId = null
                    clientSecret = null
                    sendTextMessage("Unable to log in to Microsoft translator service.\nPlease, enter Client Id again or /cancel to abort registration.")
                } else {
                    context.settings.syncUpsert("clientId", clientId)
                    context.settings.syncUpsert("clientSecret", clientSecret)
                    context.engine = translator
                    sendTextMessage("Success! Now you can to translations. Type /help for more information.")
                    dismissFork()
                }
            }
        }
    }
}

/**
 * Translation context
 */
class TranslatingContext(val settings: SimpleKeyValueJava<String>) {

    val supportedLanguages = HashMap<String, String>()

    init {
        supportedLanguages.put("ar", "Arabic")
        supportedLanguages.put("bs-Latn", "Bosnian (Latin)")
        supportedLanguages.put("bg", "Bulgarian")
        supportedLanguages.put("ca", "Catalan")
        supportedLanguages.put("zh-CHS", "Chinese Simplified")
        supportedLanguages.put("zh-CHT", "Chinese Traditional")
        supportedLanguages.put("hr", "Croatian")
        supportedLanguages.put("cs", "Czech")
        supportedLanguages.put("da", "Danish")
        supportedLanguages.put("nl", "Dutch")
        supportedLanguages.put("en", "English")
        supportedLanguages.put("et", "Estonian")
        supportedLanguages.put("fi", "Finnish")
        supportedLanguages.put("fr", "French")
        supportedLanguages.put("de", "German")
        supportedLanguages.put("el", "Greek")
        supportedLanguages.put("ht", "Haitian Creole")
        supportedLanguages.put("he", "Hebrew")
        supportedLanguages.put("hi", "Hindi")
        supportedLanguages.put("mww", "Hmong Daw")
        supportedLanguages.put("hu", "Hungarian")
        supportedLanguages.put("id", "Indonesian")
        supportedLanguages.put("it", "Italian")
        supportedLanguages.put("ja", "Japanese")
        supportedLanguages.put("tlh", "Klingon")
        supportedLanguages.put("tlh-Qaak", "Klingon (pIqaD)")
        supportedLanguages.put("ko", "Korean")
        supportedLanguages.put("lv", "Latvian")
        supportedLanguages.put("lt", "Lithuanian")
        supportedLanguages.put("ms", "Malay")
        supportedLanguages.put("mt", "Maltese")
        supportedLanguages.put("no", "Norwegian")
        supportedLanguages.put("fa", "Persian")
        supportedLanguages.put("pl", "Polish")
        supportedLanguages.put("pt", "Portuguese")
        supportedLanguages.put("otq", "Querétaro Otomi")
        supportedLanguages.put("ro", "Romanian")
        supportedLanguages.put("ru", "Russian")
        supportedLanguages.put("sr-Cyrl", "Serbian (Cyrillic)")
        supportedLanguages.put("sr-Latn", "Serbian (Latin)")
        supportedLanguages.put("sk", "Slovak")
        supportedLanguages.put("sl", "Slovenian")
        supportedLanguages.put("es", "Spanish")
        supportedLanguages.put("sv", "Swedish")
        supportedLanguages.put("th", "Thai")
        supportedLanguages.put("tr", "Turkish")
        supportedLanguages.put("uk", "Ukrainian")
        supportedLanguages.put("ur", "Urdu")
        supportedLanguages.put("vi", "Vietnamese")
        supportedLanguages.put("cy", "Welsh")
        supportedLanguages.put("yua", "Yucatec Maya")
    }

    var engine: TranslateEngine? = null
}