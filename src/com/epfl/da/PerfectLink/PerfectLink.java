package com.epfl.da.PerfectLink;

import com.epfl.da.Enums.ProtocolTypeEnum;
import com.epfl.da.Interfaces.BaseHandler;
import com.epfl.da.Interfaces.MessageHandler;
import com.epfl.da.Process;

import java.io.IOException;
import java.net.InetAddress;

public class PerfectLink {

    private SendEvent sendEvent;
    private DeliverEvent deliverEvent;
    public MessageHandler onMessageReceive;
    public BaseHandler receiveAcknowledgeHandler;


    public PerfectLink() {
        sendEvent = new SendEvent();
        deliverEvent = new DeliverEvent();
    }

    /** For PerfectLink */
    public void Send(int message, InetAddress destAddress, int destPort){
        var id = SendEvent.NextId();
        sendEvent.SendMessage(message, destAddress, destPort, ProtocolTypeEnum.PerfectLink, 0 , 0, id);
    }

    public void Send(int message, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol, int messageId){
        sendEvent.SendMessage(message, destAddress, destPort, protocol, 0 , 0, messageId);
    }
    /** For UniformReliableBroadcast */
    public void Send(int message, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol, int originalProcessId, int originalMessageId, int messageId){
        sendEvent.receiveAcknowledgeHandler = receiveAcknowledgeHandler;
        sendEvent.SendMessage(message, destAddress, destPort, protocol, originalProcessId, originalMessageId, messageId );
    }

    public void Deliver(int port, InetAddress address, int messageId, int content) throws IOException {
        deliverEvent.sendAck(port, address, messageId);
        if(onMessageReceive != null)
        {
            onMessageReceive.handle(content);
        }
    }
}

