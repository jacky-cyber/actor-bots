package im.actor.bots.util;

import im.actor.bots.parser.MessageType;

public interface SyncInputCheck {

    boolean checkInput(MessageType message);
}
