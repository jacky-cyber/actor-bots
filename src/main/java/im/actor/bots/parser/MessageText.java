package im.actor.bots.parser;

public class MessageText extends ParsedMessage {

    private String text;

    public MessageText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
