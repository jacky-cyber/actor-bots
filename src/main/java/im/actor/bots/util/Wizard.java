package im.actor.bots.util;

import im.actor.bot.BotMessages;
import im.actor.botkit.RemoteBot;
import im.actor.bots.parser.MessageType;

public abstract class Wizard {

    private RemoteBot bot;
    private BotMessages.OutPeer peer;
    private BotMessages.UserOutPeer userPeer;

    /**
     * Creating wizard
     *
     * @param bot      wizard's bot
     * @param peer     wizard peer
     * @param userPeer wizard starter peer
     */
    public Wizard(RemoteBot bot, BotMessages.OutPeer peer, BotMessages.UserOutPeer userPeer) {
        this.bot = bot;
        this.peer = peer;
        this.userPeer = userPeer;
    }

    /**
     * Bot that is used by bot
     *
     * @return bot
     */
    public RemoteBot getBot() {
        return bot;
    }

    /**
     * Wizard conversation peer
     *
     * @return conversation peer
     */
    public BotMessages.OutPeer getPeer() {
        return peer;
    }

    /**
     * Wizard started peer
     *
     * @return peer of starter
     */
    public BotMessages.UserOutPeer getUserPeer() {
        return userPeer;
    }

    /**
     * Starting wizard
     */
    public abstract void startWizard();


    /**
     * Handling messages for wizard
     *
     * @param message message
     * @return is wizard completed
     */
    public abstract boolean handleMessage(MessageType message);

    /**
     * Sending text message
     *
     * @param text text to send
     */
    protected void sendMessage(String text) {
        bot.sendTextMessage(bot.outPeer(userPeer), text);
    }

    /**
     * Cancelling wizard
     */
    public abstract void cancelWizard();
}
