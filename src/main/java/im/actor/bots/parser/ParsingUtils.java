package im.actor.bots.parser;

public class ParsingUtils {

    public static String[] splitFirstWord(String text) {
        return text.trim().split(" ", 2);
    }
}
