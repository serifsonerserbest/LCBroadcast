package Models;

public class Message {
    private int messageId;
    private int processId;
    private int content;

    public Message(int messageId, int processId, int content) {
        this.messageId = messageId;
        this.processId = processId;
        this.content = content;
    }

    public int getMessageId() {
        return messageId;
    }

    public int getProcessId() {
        return processId;
    }

    public int getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
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