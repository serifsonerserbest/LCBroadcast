package com.epfl.da.PerfectLink;

import java.io.IOException;
import java.net.InetAddress;

public class PerfectLink {

    private SendEvent sendEvent;
    private DeliverEvent deliverEvent;

    public PerfectLink() {
        sendEvent = new SendEvent();
        deliverEvent = new DeliverEvent();
    }

    public void Send(int message, InetAddress destAddress, int destPort){
        sendEvent.SendDataMessage(message, destAddress, destPort);
    }

    public void Deliver(int port) throws IOException {
        deliverEvent.ReceiveMessage(port);
    }
}

