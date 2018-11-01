package com.epfl.da.PerfectLink;

import com.epfl.da.Enums.ProtocolTypeEnum;
import com.epfl.da.Models.Message;
import com.epfl.da.Process;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;

public class PerfectLink {

    private SendEvent sendEvent;
    private DeliverEvent deliverEvent;

    private static HashSet<Message> receivedMessages;

    public PerfectLink() {
        sendEvent = new SendEvent();
        deliverEvent = new DeliverEvent();
        receivedMessages = new HashSet<>();
    }

    /** For PerfectLink */
    public void Send(int content, InetAddress destAddress, int destPort){
        int id = SendEvent.NextId();
        System.out.println("PL: " + Process.getInstance().Id + " Message #" + id + " is sent");
        sendEvent.SendMessage(content, destAddress, destPort, ProtocolTypeEnum.PerfectLink, 0 , 0, id);
    }

    /** For BestEffordBroadcast */
    public void Send(int content, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol, int messageId){
        sendEvent.SendMessage(content, destAddress, destPort, protocol, 0 , 0, messageId);
    }
    /** For UniformReliableBroadcast */
    public void Send(int content, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol, int originalProcessId, int originalMessageId, int messageId){
        System.out.println("PL: " + Process.getInstance().Id + " Message #" + messageId + " is sent");
        sendEvent.SendMessage(content, destAddress, destPort, protocol, originalProcessId, originalMessageId, messageId );
    }

    public boolean Deliver(Message message, int content, int port, InetAddress address) throws IOException {

        if (receivedMessages.contains(message)) {
            //System.out.println("Message #" + message.getMessageId() + ": " + content + " duplicate");
        } else {
            System.out.println("PL: " + Process.getInstance().Id + " Message #" + message.getMessageId() + ":From Process: " + message.getProcessId() + " is delivered");

            receivedMessages.add(message);
            deliverEvent.sendAck(port, address, message.getMessageId());
            return true;
        }
        return false;
    }
}

