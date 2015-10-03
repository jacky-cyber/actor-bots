package im.actor.bots.parser;

public class StringMatcher {

    public static MessageType matchType(String message) {
        message = message.trim();
        if (message.startsWith("/")) {
            String[] data = ParsingUtils.splitFirstWord(message);
            if (data.length == 1) {
                return new MessageCommand(data[0].substring(1), "");
            } else if (data.length == 2) {
                return new MessageCommand(data[0].substring(1), data[1]);
            } else {
                throw new RuntimeException();
            }
        } else {
            return new MessageText(message);
        }
    }
}