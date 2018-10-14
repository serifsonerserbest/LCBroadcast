package com.epfl.da.UniformReliableBroadcast;

import com.epfl.da.BestEffordBroadcast.BestEffortBroadcast;
import com.epfl.da.Enums.ProtocolTypeEnum;
import com.epfl.da.Interfaces.BaseHandler;
import com.epfl.da.Interfaces.MessageHandler;
import com.epfl.da.PerfectLink.PerfectLink;
import com.epfl.da.Process;

public class UniformReliableBroadcast {
    private BestEffortBroadcast bestEffortBroadcast;
    public MessageHandler onMessageReceive;
    public BaseHandler receiveAcknowledgeHandler;
    int processesReceivedMessage = 0;

    public UniformReliableBroadcast()
    {
        bestEffortBroadcast = new BestEffortBroadcast();
        receiveAcknowledgeHandler = ()->{
            ++processesReceivedMessage;
            if(processesReceivedMessage >= Process.getInstance().processes.size())
            {
                onMessageReceive.handle();
            }
        };
    }

    public void Broadcast(int message, int originalProcessId, int originalMessageId ) {
        bestEffortBroadcast.receiveAcknowledgeHandler = receiveAcknowledgeHandler;
        bestEffortBroadcast.Broadcast(message, originalProcessId, originalMessageId, ProtocolTypeEnum.UniformReliableBroadcast);
    }
}
