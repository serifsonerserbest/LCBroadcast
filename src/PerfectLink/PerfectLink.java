package PerfectLink;

import Enums.ProtocolTypeEnum;
import Models.MessageModel;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class PerfectLink {

    private SendEvent sendEvent;
    private DeliverEvent deliverEvent;
    private static volatile ConcurrentHashMap<MessageModel, Boolean> receivedMessages;

    public PerfectLink() {

        sendEvent = new SendEvent();
        deliverEvent = new DeliverEvent();
        receivedMessages = new ConcurrentHashMap<>();
    }

    /**
     * For PerfectLink
     */
    public void Send(int content, InetAddress destAddress, int destPort) {
        int id = SendEvent.NextId();
        sendEvent.SendMessage(content, destAddress, destPort, ProtocolTypeEnum.PerfectLink, 0, 0, id, 0);
    }

    /**
     * For BestEffordBroadcast
     */
    public void Send(int content, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol, int messageId) {

        sendEvent.SendMessage(content, destAddress, destPort, protocol, 0, 0, messageId, 0);
    }

    /**
     * For UniformReliableBroadcast
     */
    public void Send(int content, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol, int originalProcessId, int originalMessageId, int messageId) {

        sendEvent.SendMessage(content, destAddress, destPort, protocol, originalProcessId, originalMessageId, messageId, 0);
    }

    /**
     * For FIFOBroadcast
     */
    public void Send(int content, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol, int originalProcessId, int originalMessageId, int messageId, int fifoId) {

        sendEvent.SendMessage(content, destAddress, destPort, protocol, originalProcessId, originalMessageId, messageId, fifoId);
    }

    public boolean Deliver(MessageModel message, int content, int port, InetAddress address) {

        deliverEvent.sendAck(port, address, message.getMessageId());
        return receivedMessages.putIfAbsent(message, true) == null;
    }
}

