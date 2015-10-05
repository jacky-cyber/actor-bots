package im.actor.bots.magic;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import akka.actor.ActorRef;
import im.actor.bot.BotMessages;
import im.actor.botkit.RemoteBot;
import im.actor.bots.parser.MessageCommand;

/**
 * Basic Bot that can be easily separated to multiple different bots based on command for features.
 */
public class MagicMergedBot extends MagicForkBot {

    private ArrayList<BotMerge> merges = new ArrayList<BotMerge>();

    public MagicMergedBot(RemoteBot baseBot, BotMessages.OutPeer chatPeer) {
        super(baseBot, chatPeer);
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();

        initCommands();
    }

    public void initCommands() {

    }

    @Override
    public boolean onReceive(@NotNull String command, @NotNull String[] args, @NotNull String text, @NotNull BotMessages.UserOutPeer sender) {
        for (BotMerge m : merges) {
            for (String c : m.commands) {
                if (c.equals(command)) {
                    m.fork.tell(new Message(new MessageCommand(command, new ArrayList<String>(), text), sender), self());
                    return true;
                }
            }
        }
        return false;
    }

    public void addBotMerge(ActorRef forkRef, String... commands) {
        merges.add(new BotMerge(forkRef, commands));
    }

    @Override
    public void onChildForked(ActorRef ref) {
        startForwardingTo(ref);
    }
    
    @Override
    public void onChildForkClosed(ActorRef ref) {
        stopForwarding();
    }

    public class BotMerge {

        private ActorRef fork;
        private String[] commands;

        public BotMerge(ActorRef fork, String[] commands) {
            this.fork = fork;
            this.commands = commands;
        }
    }
}