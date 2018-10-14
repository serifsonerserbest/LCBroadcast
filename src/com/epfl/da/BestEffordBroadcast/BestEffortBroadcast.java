package com.epfl.da.BestEffordBroadcast;

import com.epfl.da.Enums.ProtocolTypeEnum;
import com.epfl.da.Interfaces.BaseHandler;
import com.epfl.da.Interfaces.MessageHandler;
import com.epfl.da.PerfectLink.PerfectLink;
import com.epfl.da.Process;

import java.io.IOException;
import java.net.InetAddress;

public class BestEffortBroadcast {

    private PerfectLink perfectlink;
    public MessageHandler onMessageReceive;
    public BaseHandler receiveAcknowledgeHandler;


    public BestEffortBroadcast(){
        perfectlink = new PerfectLink();
    }

    public void Broadcast(int message)
    {
        var processes = Process.getInstance().processes;
        for (int i = 0; i < processes.size(); i++) {
            perfectlink.Send(message, processes.get(i).address, processes.get(i).port, ProtocolTypeEnum.BestEffortBroadcast);
        }
    }
 /** For UniformReliableBroadcast */
    public void Broadcast(int message, int originalProcessId, int originalMessageId, ProtocolTypeEnum protocol )
    {
        perfectlink.receiveAcknowledgeHandler = receiveAcknowledgeHandler;
        var processes = Process.getInstance().processes;
        for (int i = 0; i < processes.size(); i++) {
            perfectlink.Send(message, processes.get(i).address, processes.get(i).port, protocol, originalProcessId , originalMessageId);
        }
    }


    public void Deliver(int port, InetAddress address, int messageId, int content) throws IOException {
       perfectlink.Deliver(port, address, messageId, content);
    }
}

