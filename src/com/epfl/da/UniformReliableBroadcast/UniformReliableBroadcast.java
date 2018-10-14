package com.epfl.da.UniformReliableBroadcast;

import com.epfl.da.BestEffordBroadcast.BestEffortBroadcast;
import com.epfl.da.Enums.ProtocolTypeEnum;
import com.epfl.da.Interfaces.BaseHandler;
import com.epfl.da.Interfaces.MessageHandler;
import com.epfl.da.PerfectLink.PerfectLink;
import com.epfl.da.PerfectLink.SendEvent;
import com.epfl.da.Process;

import java.io.IOException;
import java.net.InetAddress;

public class UniformReliableBroadcast {
    private BestEffortBroadcast bestEffortBroadcast;
    public MessageHandler onMessageReceive;
    public BaseHandler receiveAcknowledgeHandler;
    int processesReceivedMessage = 0;
    boolean isHandlerCalled = false;

    public UniformReliableBroadcast()
    {
        bestEffortBroadcast = new BestEffortBroadcast();
    }
    public void Broadcast(int message){
        receiveAcknowledgeHandler = ()->{
            ++processesReceivedMessage;
            if(processesReceivedMessage >= Process.getInstance().processes.size() / 2 && !isHandlerCalled)
            {
                isHandlerCalled = true;
                onMessageReceive.handle(message);
            }
        };
        bestEffortBroadcast.receiveAcknowledgeHandler = receiveAcknowledgeHandler;
        bestEffortBroadcast.Broadcast(message, ProtocolTypeEnum.UniformReliableBroadcast);
    }
    public void Broadcast(int message, int originalProcessId, int originalMessageId ) {
        receiveAcknowledgeHandler = ()->{
            ++processesReceivedMessage;
            if(processesReceivedMessage >= Process.getInstance().processes.size() / 2 && !isHandlerCalled)
            {
                isHandlerCalled = true;
                onMessageReceive.handle(message);
            }
        };
        bestEffortBroadcast.receiveAcknowledgeHandler = receiveAcknowledgeHandler;
        bestEffortBroadcast.Broadcast(message, originalProcessId, originalMessageId, ProtocolTypeEnum.UniformReliableBroadcast);
    }
    public void Deliver(int port, InetAddress address, int messageId, int content) throws IOException {
        bestEffortBroadcast.Deliver(port, address, messageId, content);
    }
}
