package im.actor.bots

import im.actor.bot.BotMessages
import im.actor.botkit.RemoteBot

class JennyBot(token: String, endpoint: String): RemoteBot(token, endpoint) {

    override fun onTextMessage(msg: BotMessages.TextMessage) {
        val message = msg.text()
        var peer = outPeer(msg.sender())

        sendTextMessage(peer, translate(message))
    }
}