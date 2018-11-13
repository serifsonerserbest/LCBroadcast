package Models;

public class MessageModel {
    private int messageId;
    private int processId;

    public MessageModel(int messageId, int processId) {
        this.messageId = messageId;
        this.processId = processId;
    }

    public int getMessageId() {
        return messageId;
    }

    public int getProcessId() {
        return processId;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageModel message = (MessageModel) o;
        return hashCode() == message.hashCode();
    }

    public int cantorPairing() {

        int sum = this.messageId + this.processId;
        if (sum % 2 == 0) sum = sum / 2 * (sum + 1);
        else sum = (sum + 1) / 2 * sum;
        int cantorValue = sum + this.processId;
        return cantorValue;
    }

    @Override
    public int hashCode() {
        return this.cantorPairing();
    }
}