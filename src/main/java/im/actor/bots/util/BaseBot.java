package im.actor.bots.util;

import java.util.ArrayList;

import im.actor.bot.BotMessages;
import im.actor.botkit.RemoteBot;
import im.actor.bots.parser.MessageCommand;
import im.actor.bots.parser.MessageType;
import im.actor.bots.parser.StringMatcher;

public class BaseBot extends RemoteBot {

    private static final String COMMAND_START = "start";
    private static final String COMMAND_HELP = "help";
    private static final String COMMAND_CANCEL = "cancel";

    private Wizard currentWizard = null;

    private ArrayList<BotMessages.TextMessage> pendingMessages = new ArrayList<BotMessages.TextMessage>();
    private boolean isPaused = false;

    public BaseBot(String token, String endpoint) {
        super(token, endpoint);
    }

    @Override
    public final void onTextMessage(BotMessages.TextMessage textMessage) {

        // Saving in pending messages
        if (isPaused) {
            pendingMessages.add(textMessage);
            return;
        }

        MessageType msg = StringMatcher.matchType(textMessage.text());
        BotMessages.OutPeer peer = textMessage.peer();

        // Wizard cancelling


        if (msg instanceof MessageCommand) {
            MessageCommand cmd = (MessageCommand) msg;
            if (COMMAND_CANCEL.equals(cmd.getCommand())) {
                if (currentWizard != null) {
                    currentWizard.cancelWizard();
                    currentWizard = null;
                } else {
                    sendTextMessage(peer, "I am not doing anything...");
                }
                return;
            } else if (COMMAND_HELP.equals(cmd.getCommand()) || COMMAND_START.equals(cmd.getCommand())) {
                onHelpRequested(textMessage);
                return;
            }
        }
        // Processing wizards

        if (currentWizard != null) {
            if (currentWizard.handleMessage(msg)) {
                currentWizard = null;
            }
            return;
        }

        // Root implementation

        if (!onMessage(msg, textMessage)) {
            if (msg instanceof MessageCommand) {
                sendTextMessage(peer, Strings.unknown());
            } else {
                sendTextMessage(peer, Strings.NO_INPUT);
            }
        }
    }

    public boolean onMessage(MessageType msg, BotMessages.TextMessage baseMessage) {
        return false;
    }

    public void startWizard(Wizard wizard) {
        if (currentWizard != null) {
            currentWizard.cancelWizard();
        }
        currentWizard = wizard;
        currentWizard.startWizard();
    }

    public void pauseMessages() {
        if (isPaused) {
            throw new RuntimeException();
        }
        isPaused = true;
    }

    public void resumeMessages() {
        if (!isPaused) {
            throw new RuntimeException();
        }
        isPaused = false;

        BotMessages.TextMessage[] msg = pendingMessages.toArray(new BotMessages.TextMessage[0]);
        pendingMessages.clear();
        for (BotMessages.TextMessage t : msg) {
            onTextMessage(t);
        }
    }

    public void onHelpRequested(BotMessages.TextMessage baseMessage) {

    }
}
