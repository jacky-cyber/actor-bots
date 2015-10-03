package im.actor.bots.parser;

public class MessageCommand extends MessageType {

    private String command;
    private String args;

    public MessageCommand(String command, String args) {
        this.command = command;
        this.args = args;
    }

    public String getCommand() {
        return command;
    }

    public String getArgs() {
        return args;
    }
}
