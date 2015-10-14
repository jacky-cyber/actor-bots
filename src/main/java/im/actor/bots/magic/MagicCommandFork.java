package im.actor.bots.magic;

import im.actor.botkit.RemoteBot;
import im.actor.bots.BotMessages;

public class MagicCommandFork extends MagicForkBot {

    private String commandName;

    private String cancelMessage;

    public MagicCommandFork(String commandName, RemoteBot baseBot, BotMessages.OutPeer chatPeer) {
        super(baseBot, chatPeer);
        this.setIsCommandsSupported(false);
        this.commandName = commandName;
        this.cancelMessage = "Oops, that's embarrassing. Command /" + commandName + " is cancelled.\\n\\nSend /help for a list of commands.";
    }

    public String getCancelMessage() {
        return cancelMessage;
    }

    public void setCancelMessage(String cancelMessage) {
        this.cancelMessage = cancelMessage;
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        if (cancelMessage != null) {
            sendTextMessage(cancelMessage);
        }
    }
}
