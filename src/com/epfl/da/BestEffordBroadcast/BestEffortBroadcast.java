package com.epfl.da.BestEffordBroadcast;

import com.epfl.da.Enums.ProtocolTypeEnum;
import com.epfl.da.Interfaces.BaseHandler;
import com.epfl.da.Interfaces.MessageHandler;
import com.epfl.da.Listener;
import com.epfl.da.Models.Message;
import com.epfl.da.PerfectLink.PerfectLink;
import com.epfl.da.PerfectLink.SendEvent;
import com.epfl.da.Process;

import java.io.IOException;
import java.net.InetAddress;

public class BestEffortBroadcast {

    private PerfectLink perfectlink;

    public BestEffortBroadcast(){
        perfectlink = new PerfectLink();
    }

    public void Broadcast(int message)
    {
        var processes = Process.getInstance().processes;
        var id = SendEvent.NextId();
        for (int i = 0; i < processes.size(); i++) {
            perfectlink.Send(message, processes.get(i).address, processes.get(i).port, ProtocolTypeEnum.BestEffortBroadcast, id);
        }
    }
    //** For UniformReliableBroadcast *//*
    public void Broadcast(int content, int originalProcessId, int originalMessageId, ProtocolTypeEnum protocol, int messageId )
    {

        var processes = Process.getInstance().processes;
        for (int i = 0; i < processes.size(); i++) {
            perfectlink.Send(content, processes.get(i).address, processes.get(i).port, protocol, originalProcessId , originalMessageId, messageId);
        }
    }

    public boolean Deliver(Message message, int content, int portReceived, InetAddress addressReceived) throws IOException {
        return perfectlink.Deliver(message, content, portReceived, addressReceived);
    }
}

