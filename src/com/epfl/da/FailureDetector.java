package com.epfl.da;

import com.epfl.da.PerfectLink.SendEvent;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;

public class FailureDetector {
    private final int pingTimeout = 500;
    private final int checkTimeout = 10000;

    private final HashMap <InetSocketAddress, Boolean> processesStatuses;
    public FailureDetector(ArrayList<InetSocketAddress> addresses)
    {
        processesStatuses = new HashMap<InetSocketAddress, Boolean>();
        for (InetSocketAddress address:addresses) {
            processesStatuses.put(address, false);
        }

        new Thread(() -> {
            PingProcesses();
        }).start();
    }

    public HashMap <InetSocketAddress, Boolean> GetprocessesStatuses()
    {
        return  processesStatuses;
    }
    private void PingProcesses() {
        while (true) {
            try {
                SendEvent sender = new SendEvent();
                for (InetSocketAddress address : processesStatuses.keySet()) {
                    processesStatuses.put(address, false);

                    sender.receiveAcknowledgeHandler = ()-> {
                        processesStatuses.put(address, true);
                    };
                    sender.SendPingMessage(address.getAddress(), address.getPort());
                }
                Thread.sleep(checkTimeout);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
