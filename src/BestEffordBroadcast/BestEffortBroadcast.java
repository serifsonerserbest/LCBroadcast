package BestEffordBroadcast;

import Models.MessageModel;
import Models.ProcessModel;
import PerfectLink.PerfectLink;
import Process.Process;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;


public class BestEffortBroadcast {

    private PerfectLink perfectlink;

    public BestEffortBroadcast() {
        perfectlink = new PerfectLink();
    }

    //** For FIFOBroadcast *//*
    public synchronized void Broadcast(int content, int originalProcessId, int originalMessageId, int messageId, int fifoId) {
        ArrayList<ProcessModel> processes = Process.getInstance().processes;
        for (int i = 0; i < processes.size(); i++) {
            perfectlink.Send(content, processes.get(i).address, processes.get(i).port, originalProcessId, originalMessageId, messageId, fifoId);
        }
    }

    public synchronized boolean Deliver(MessageModel message, int content, int portReceived, InetAddress addressReceived) throws IOException {
        return perfectlink.Deliver(message, content, portReceived, addressReceived);
    }
}

