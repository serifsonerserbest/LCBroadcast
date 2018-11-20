package PerfectLink;

import Enums.ProtocolTypeEnum;
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

    /**
     * For PerfectLink
     */
    public synchronized void Send(int content, InetAddress destAddress, int destPort) {

        int id = SendEvent.NextId();
        sendEvent.SendMessage(content, destAddress, destPort, ProtocolTypeEnum.PerfectLink, 0, 0, id, 0, null);
    }

    /**
     * For BestEffordBroadcast
     */
    public synchronized void Send(int content, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol, int messageId) {

        sendEvent.SendMessage(content, destAddress, destPort, protocol, 0, 0, messageId, 0,null);
    }

    /**
     * For UniformReliableBroadcast
     */
    public synchronized void Send(int content, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol, int originalProcessId, int originalMessageId, int messageId) {

        sendEvent.SendMessage(content, destAddress, destPort, protocol, originalProcessId, originalMessageId, messageId, 0,null);
    }

    /**
     * For FIFOBroadcast
     */
    public synchronized void Send(int content, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol, int originalProcessId, int originalMessageId, int messageId, int fifoId) {

        sendEvent.SendMessage(content, destAddress, destPort, protocol, originalProcessId, originalMessageId, messageId, fifoId,null);
    }

    /**
     * For LocalCausalBroadcast
     */
    public synchronized void Send(int content, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol, int originalProcessId, int originalMessageId, int messageId, int[] vectorClock) {

        sendEvent.SendMessage(content, destAddress, destPort, protocol, originalProcessId, originalMessageId, messageId, 0, vectorClock);
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

