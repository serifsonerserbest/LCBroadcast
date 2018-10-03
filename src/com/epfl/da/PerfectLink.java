package com.epfl.da;

import java.io.IOException;
import java.net.InetAddress;

public class PerfectLink {

    private SendEvent sendEvent;
    private DeliverEvent deliverEvent;

    public PerfectLink() {
        sendEvent = new SendEvent();
        deliverEvent = new DeliverEvent();
    }

    public void send(int message, InetAddress destAddress, int destPort){
        sendEvent.SendMessage(message, destAddress, destPort);
    }

    public void deliver(int port) throws IOException {
        deliverEvent.ReceiveMessage(port);
    }
}

