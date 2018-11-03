package PerfectLink;

import Enums.ProtocolTypeEnum;
import Models.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class PerfectLink {

    private SendEvent sendEvent;
    private DeliverEvent deliverEvent;
    private static volatile Set<Message> receivedMessages;


    public PerfectLink() {
        sendEvent = new SendEvent();
        deliverEvent = new DeliverEvent();
        receivedMessages = new HashSet<>();
        //receivedMessages = ConcurrentHashMap.newKeySet();

    }

    /** For PerfectLink */
    public void Send(int content, InetAddress destAddress, int destPort){

        int id = SendEvent.NextId();

        //System.out.println("PL: " + Process.Process.getInstance().Id + " Message #" + id + " is sent");
        sendEvent.SendMessage(content, destAddress, destPort, ProtocolTypeEnum.PerfectLink, 0 , 0, id, 0);
    }
    /** For BestEffordBroadcast */
    public  void Send(int content, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol, int messageId){
        sendEvent.SendMessage(content, destAddress, destPort, protocol, 0 , 0, messageId, 0);
    }
    /** For UniformReliableBroadcast */
    public void Send(int content, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol, int originalProcessId, int originalMessageId, int messageId){
        //System.out.println("PL: " + Process.Process.getInstance().Id + " Message #" + messageId + " is sent");
        sendEvent.SendMessage(content, destAddress, destPort, protocol, originalProcessId, originalMessageId, messageId, 0);
    }
    //For FIFOBroadcast
    public void Send(int content, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol, int originalProcessId, int originalMessageId, int messageId, int fifoId){
        //System.out.println("PL: " + Process.Process.getInstance().Id + " Message #" + messageId + " is sent");
        sendEvent.SendMessage(content, destAddress, destPort, protocol, originalProcessId, originalMessageId, messageId, fifoId);
    }

    public boolean Deliver(Message message, int content, int port, InetAddress address) throws IOException {

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

