package im.actor.bots.magic;

import java.util.HashMap;

import akka.actor.ActorRef;
import akka.actor.Props;
import im.actor.botkit.RemoteBot;
import im.actor.bots.BotMessages;
import im.actor.bots.parser.ParsedMessage;

/**
 * Useful extension on basic bot that allows you to easier bot building
 */
public class MagicBot<T extends MagicForkBot> extends RemoteBot {

    private HashMap<String, ActorRef> dialogs = new HashMap<>();
    private Class<T> forkClass;

    public MagicBot(Class<T> forkClass, String token, String endpoint) {
        super(token, endpoint);

        this.forkClass = forkClass;
    }

    @Override
    public void onTextMessage(BotMessages.TextMessage textMessage) {
        ParsedMessage msg = ParsedMessage.matchType(textMessage.text());
        String peerId = textMessage.peer().type() + "_" + textMessage.peer().id();


        ActorRef dialogRef;
        if (dialogs.containsKey(peerId)) {
            dialogRef = dialogs.get(peerId);
        } else {
            dialogRef = context().actorOf(Props.create(forkClass, this, textMessage.peer()), peerId);
            dialogs.put(peerId, dialogRef);
        }

        dialogRef.tell(new MagicForkBot.Message(msg, textMessage.sender()), self());
    }

//
//    @Override
//    public void onTextMessage(BotMessages.TextMessage textMessage) {
//        ParsedMessage msg = ParsedMessage.matchType(textMessage.text());
//        String peerId = textMessage.peer().type() + "_" + textMessage.peer().id();
//
//
//
//        ActorRef dialogRef;
//        if (dialogs.containsKey(peerId)) {
//            dialogRef = dialogs.get(peerId);
//        } else {
//            dialogRef = context().actorOf(Props.create(forkClass, this, textMessage.peer()), peerId);
//            dialogs.put(peerId, dialogRef);
//        }
//
//        dialogRef.tell(new MagicForkBot.Message(msg, textMessage.sender()), self());
//    }
}
