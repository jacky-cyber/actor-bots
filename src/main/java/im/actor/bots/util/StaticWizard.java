package im.actor.bots.util;

import java.util.ArrayList;

import im.actor.bot.BotMessages;
import im.actor.botkit.RemoteBot;
import im.actor.bots.parser.MessageType;

/**
 * Static Wizard
 */
public class StaticWizard extends Wizard {

    private String cancelMessage = Strings.CANCEL_MESSAGE;
    private int index = 0;
    private ArrayList<StaticStep> steps = new ArrayList<StaticStep>();
    private ArrayList<MessageType> records = new ArrayList<MessageType>();

    /**
     * Creating wizard
     *
     * @param bot      wizard's bot
     * @param peer     wizard peer
     * @param userPeer wizard starter peer
     */
    public StaticWizard(RemoteBot bot, BotMessages.OutPeer peer, BotMessages.UserOutPeer userPeer) {
        super(bot, peer, userPeer);
    }

    /**
     * Adding new step
     *
     * @param stepWelcome welcome message for step
     */
    public StaticStep addStep(String stepWelcome) {
        StaticStep res = new StaticStep(stepWelcome, null);
        steps.add(res);
        return res;
    }

    /**
     * Adding new step with static ckecking
     *
     * @param stepWelcome welcome message for step
     * @param check       checker for input
     */
    public StaticStep addStep(String stepWelcome, SyncInputCheck check) {
        StaticStep res = new StaticStep(stepWelcome, check);
        steps.add(res);
        return res;
    }

    /**
     * Getting cancel message
     *
     * @return cancel message
     */
    public String getCancelMessage() {
        return cancelMessage;
    }

    /**
     * Setting cancel message
     *
     * @param cancelMessage cancel message
     */
    public void setCancelMessage(String cancelMessage) {
        this.cancelMessage = cancelMessage;
    }

    @Override
    public void startWizard() {
        if (steps.size() == 0) {
            throw new RuntimeException("No steps in static wizard");
        }

        index = 0;
        sendMessage(steps.get(index).getStepWelcome());
    }

    @Override
    public boolean handleMessage(MessageType message) {

        StaticStep step = steps.get(index);

        if (step.getCheck() != null) {
            if (!step.getCheck().checkInput(message)) {
                sendMessage("Wrong input");
                return false;
            }
        }

        index++;
        records.add(message);

        if (index >= steps.size() - 1) {
            sendMessage(steps.get(index).getStepWelcome());
            return true;
        }

        sendMessage(steps.get(index).getStepWelcome());

        return false;
    }

    @Override
    public void cancelWizard() {
        sendMessage(cancelMessage);
    }

    public static class StaticStep {

        private String stepWelcome;
        private SyncInputCheck check;

        public StaticStep(String stepWelcome, SyncInputCheck check) {
            this.stepWelcome = stepWelcome;
            this.check = check;
        }

        public String getStepWelcome() {
            return stepWelcome;
        }

        public SyncInputCheck getCheck() {
            return check;
        }
    }
}
