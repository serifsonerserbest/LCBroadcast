package com.epfl.da.BestEffordBroadcast;

import com.epfl.da.PerfectLink.PerfectLink;

import java.io.IOException;
import java.net.InetAddress;

public class BestEffortBroadcast {

    private PerfectLink perfectlink;

    public BestEffortBroadcast(){
        perfectlink = new PerfectLink();
    }

    public void Broadcast(int message, InetAddress[] addresses, int[] ports)
    {
        //TODO: One must also send the mesage to itself in broadcasting !!!
        //perfectlink.Send(message, OWN_ADDRESS, OWN_PORT);

        for (int i = 0; i < addresses.length; i++) {
            perfectlink.Send(message, addresses[i], ports[i]);
        }
    }

    public void Deliver(int port) throws IOException {
        //perfectlink.Deliver(port);
    }
}

