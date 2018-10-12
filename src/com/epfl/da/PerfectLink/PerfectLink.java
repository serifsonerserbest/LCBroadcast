package com.epfl.da.PerfectLink;

import com.epfl.da.Enums.ProtocolTypeEnum;
import com.epfl.da.Interfaces.MessageHandler;

import java.io.IOException;
import java.net.InetAddress;

public class PerfectLink {

    private SendEvent sendEvent;
    private DeliverEvent deliverEvent;
    public MessageHandler onMessageReceive;

    public PerfectLink() {
        sendEvent = new SendEvent();
        deliverEvent = new DeliverEvent();
    }

    public void Send(int message, InetAddress destAddress, int destPort){
        sendEvent.SendMessage(message, destAddress, destPort, ProtocolTypeEnum.PerfectLink);
    }

    public void Send(int message, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol){
        sendEvent.SendMessage(message, destAddress, destPort, protocol);
    }

    public void Deliver(int port, InetAddress address, int messageId, int content) throws IOException {
        deliverEvent.sendAck(port, address, messageId);
        if(onMessageReceive != null)
        {
            onMessageReceive.handle(content);
        }
    }
}

