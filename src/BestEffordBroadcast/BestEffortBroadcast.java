package BestEffordBroadcast;

import Enums.ProtocolTypeEnum;
import Models.Message;
import Models.ProcessModel;
import PerfectLink.PerfectLink;
import PerfectLink.SendEvent;
import Process.Process;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;


public class BestEffortBroadcast {

    private PerfectLink perfectlink;

    public BestEffortBroadcast(){
        perfectlink = new PerfectLink();
    }

    public synchronized void Broadcast(int message)
    {
        ArrayList<ProcessModel> processes = Process.getInstance().processes;
        int id = SendEvent.NextId();
        //System.out.println("URB: " + Process.Process.getInstance().Id + " Broadcast Message #" + id);
        for (int i = 0; i < processes.size(); i++) {
            perfectlink.Send(message, processes.get(i).address, processes.get(i).port, ProtocolTypeEnum.BestEffortBroadcast, id);
        }
    }
    //** For UniformReliableBroadcast *//*
    public synchronized void Broadcast(int content, int originalProcessId, int originalMessageId, ProtocolTypeEnum protocol, int messageId )
    {

        ArrayList<ProcessModel> processes = Process.getInstance().processes;
        for (int i = 0; i < processes.size(); i++) {
            perfectlink.Send(content, processes.get(i).address, processes.get(i).port, protocol, originalProcessId , originalMessageId, messageId);
        }
    }
    public synchronized boolean Deliver(Message message, int content, int portReceived, InetAddress addressReceived) throws IOException {
        return perfectlink.Deliver(message, content, portReceived, addressReceived);
    }
}

