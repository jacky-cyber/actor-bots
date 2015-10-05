package im.actor.bots.magic;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import im.actor.bot.BotMessages;
import im.actor.botkit.RemoteBot;
import im.actor.bots.parser.MessageCommand;
import im.actor.bots.parser.MessageText;
import im.actor.bots.parser.ParsedMessage;
import shardakka.ShardakkaExtension;
import shardakka.keyvalue.SimpleKeyValueJava;

public class MagicForkBot extends UntypedActor {

    private static final String COMMAND_CANCEL = "cancel";
    private static final String COMMAND_START = "start";
    private static final String COMMAND_HELP = "help";

    private boolean isCommandsSupported = true;

    private String welcomeMessage;
    private String startMessage;

    private RemoteBot baseBot;
    private BotMessages.OutPeer chatPeer;

    private ActorRef forked;
    private ActorRef forwarded;

    public MagicForkBot(RemoteBot baseBot, BotMessages.OutPeer chatPeer) {
        this.baseBot = baseBot;
        this.chatPeer = chatPeer;
    }

    public String getStartMessage() {
        return startMessage;
    }

    public void setStartMessage(String startMessage) {
        this.startMessage = startMessage;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public boolean isCommandsSupported() {
        return isCommandsSupported;
    }

    public void setIsCommandsSupported(boolean isCommandsSupported) {
        this.isCommandsSupported = isCommandsSupported;
    }

    public RemoteBot getBaseBot() {
        return baseBot;
    }

    public BotMessages.OutPeer getChatPeer() {
        return chatPeer;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();

        if (welcomeMessage != null) {
            sendTextMessage(welcomeMessage);
        }

        onStarted();
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if (o instanceof Message) {
            Message msg = (Message) o;

            if (forwarded != null) {

                // If Actor forwarded
                forwarded.forward(o, context());
            } else if (forked != null) {

                // If Actor forked
                forked.forward(o, context());
            } else {

                if (msg.getMessage() instanceof MessageCommand) {
                    MessageCommand command = (MessageCommand) msg.getMessage();
                    if (COMMAND_CANCEL.equals(command.getCommand())) {
                        cancelFork();
                        return;
                    } else if (COMMAND_HELP.equals(command.getCommand()) || COMMAND_START.equals(command.getCommand())) {
                        if (startMessage != null) {
                            sendTextMessage(startMessage);
                            return;
                        }
                    }
                }

                // If current actor
                onReceive(msg.getMessage(), msg.getSender());
            }
        } else if (o instanceof Terminated) {
            Terminated t = (Terminated) o;

            if (forked == t.getActor()) {
                forked = null;
                onForkClosed();
                context().parent().tell(new ChildForkClosed(), self());
            }
        } else if (o instanceof Runnable) {
            ((Runnable) o).run();
        } else if (o instanceof Cancel) {
            onCancelled();
            self().tell(PoisonPill.getInstance(), self());
        } else if (o instanceof Dismiss) {
            onDismissed();
            self().tell(PoisonPill.getInstance(), self());
        } else if (o instanceof ChildForked) {
            onChildForked(sender());
        } else if (o instanceof ChildForkClosed) {
            onChildForkClosed(sender());
        } else {
            unhandled(o);
        }
    }

    public void onStarted() {

    }

    public final void onReceive(ParsedMessage message, BotMessages.UserOutPeer sender) {
        if (message instanceof MessageCommand) {
            if (!isCommandsSupported) {
                sendCommandsUnsupported();
            } else {
                MessageCommand command = (MessageCommand) message;
                List<String> args = command.getArgs();
                if (!onReceive(command.getCommand(), args.toArray(new String[args.size()]), command.getData(), sender)) {
                    sendUnknownCommand();

                }
            }
        } else if (message instanceof MessageText) {
            onReceive(((MessageText) message).getText(), sender);
        }
    }

    public void onReceive(@NotNull String text, @NotNull BotMessages.UserOutPeer sender) {

    }

    public boolean onReceive(@NotNull String command, @NotNull String[] args, @NotNull String text, @NotNull BotMessages.UserOutPeer sender) {
        return false;
    }

    public void onCancelled() {

    }

    public void onDismissed() {

    }

    public void onForked() {

    }

    public void onForkClosed() {

    }

    public void onChildForked(ActorRef ref) {

    }

    public void onChildForkClosed(ActorRef ref) {

    }

    public void startForwardingTo(ActorRef ref) {
        forwarded = ref;
    }

    public void stopForwarding() {
        forwarded = null;
    }

    public void fork(Class clazz, Object... args) {
        Object[] nargs = new Object[args.length + 2];
        System.arraycopy(args, 0, nargs, 0, args.length);
        nargs[nargs.length - 2] = getBaseBot();
        nargs[nargs.length - 1] = getChatPeer();
        fork(Props.create(clazz, nargs));
    }

    public void fork(Props props) {
        if (forked != null) {
            throw new RuntimeException("Actor is forked!");
        }
        forked = context().actorOf(props);
        context().watch(forked);
        onForked();
        context().parent().tell(new ChildForked(), self());
    }

    public final void cancelFork() {
        self().tell(new Cancel(), self());
    }

    public final void dismissFork() {
        self().tell(new Dismiss(), self());
    }

    public void sendCommandsUnsupported() {
        sendTextMessage("Please, don't try to send commands. If you want to cancel bot creation send /cancel.");
    }

    public void sendUnknownCommand() {
        sendTextMessage(Strings.unknown());
    }

    public void sendTextMessage(String text) {
        baseBot.sendTextMessage(chatPeer, text);
    }

    public SimpleKeyValueJava<String> createKeyValue(String name) {
        return ShardakkaExtension.get(context().system()).simpleKeyValue(name, context().system()).asJava();
    }

    public static class Message {

        private BotMessages.UserOutPeer sender;
        private ParsedMessage message;

        public Message(ParsedMessage message, BotMessages.UserOutPeer sender) {
            this.message = message;
            this.sender = sender;
        }

        public BotMessages.UserOutPeer getSender() {
            return sender;
        }

        public ParsedMessage getMessage() {
            return message;
        }
    }

    private static class Cancel {

    }

    private static class Dismiss {

    }

    private static class ChildForked {

    }

    private static class ChildForkClosed {

    }
}
