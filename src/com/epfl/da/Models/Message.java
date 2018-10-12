package com.epfl.da.Models;

public class Message{
    int messageId;
    int port;
    public Message(int messageId, int port) {
        this.messageId = messageId;
        this.port = port;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return hashCode() == message.hashCode();
    }
    public int cantorPairing() {
        int sum = this.messageId + this.port;
        if(sum % 2 == 0) sum = sum / 2 * (sum + 1);
        else sum = (sum + 1) / 2 * sum;
        int cantorValue = sum + this.port;
        return cantorValue;
    }
    @Override
    public int hashCode() {
        return this.cantorPairing();
    }
}