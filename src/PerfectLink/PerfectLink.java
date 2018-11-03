package PerfectLink;

import Models.MessageModel;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

public class PerfectLink {

    private SendEvent sendEvent;
    private DeliverEvent deliverEvent;
    private static volatile Set<MessageModel> receivedMessages;


    public PerfectLink() {

        sendEvent = new SendEvent();
        deliverEvent = new DeliverEvent();
        receivedMessages = new HashSet<>();
    }

    //For FIFOBroadcast
    public synchronized void Send(int content, InetAddress destAddress, int destPort, int originalProcessId, int originalMessageId, int messageId, int fifoId) {
        sendEvent.SendMessage(content, destAddress, destPort,originalProcessId, originalMessageId, messageId, fifoId);
    }

    public synchronized boolean Deliver(MessageModel message, int content, int port, InetAddress address) throws IOException {

        if (receivedMessages.contains(message)) {
            //System.out.println("MessageModel #" + message.getMessageId() + ": " + content + " duplicate");
        } else {
            //System.out.println("PL: " + Process.Process.getInstance().Id + " MessageModel #" + message.getMessageId() + ":From Process.Process: " + message.getProcessId() + " is delivered");
            receivedMessages.add(message);
            deliverEvent.sendAck(port, address, message.getMessageId());
            return true;
        }
        return false;
    }
}

