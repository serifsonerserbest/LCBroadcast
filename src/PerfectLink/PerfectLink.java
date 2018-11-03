package PerfectLink;

import Enums.ProtocolTypeEnum;
import Models.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

public class PerfectLink {

    private SendEvent sendEvent;
    private DeliverEvent deliverEvent;
    private static volatile Set<Message> receivedMessages;


    public PerfectLink() {

        sendEvent = new SendEvent();
        deliverEvent = new DeliverEvent();
        receivedMessages = new HashSet<>();
    }

    //For FIFOBroadcast
    public synchronized void Send(int content, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol, int originalProcessId, int originalMessageId, int messageId, int fifoId) {
        sendEvent.SendMessage(content, destAddress, destPort, protocol, originalProcessId, originalMessageId, messageId, fifoId);
    }

    public synchronized boolean Deliver(Message message, int content, int port, InetAddress address) throws IOException {

        if (receivedMessages.contains(message)) {
            //System.out.println("Message #" + message.getMessageId() + ": " + content + " duplicate");
        } else {
            //System.out.println("PL: " + Process.Process.getInstance().Id + " Message #" + message.getMessageId() + ":From Process.Process: " + message.getProcessId() + " is delivered");
            receivedMessages.add(message);
            deliverEvent.sendAck(port, address, message.getMessageId());
            return true;
        }
        return false;
    }
}

