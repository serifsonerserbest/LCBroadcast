package com.epfl.da.UniformReliableBroadcast;

import com.epfl.da.BestEffordBroadcast.BestEffortBroadcast;
import com.epfl.da.Enums.ProtocolTypeEnum;
import com.epfl.da.Interfaces.BaseHandler;
import com.epfl.da.Interfaces.MessageHandler;
import com.epfl.da.Models.Message;
import com.epfl.da.PerfectLink.PerfectLink;
import com.epfl.da.PerfectLink.SendEvent;
import com.epfl.da.Process;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

public class UniformReliableBroadcast {
    private BestEffortBroadcast bestEffortBroadcast;

    private HashMap<Message, Integer> ack;
    private HashSet<Message> delivered;
    private HashSet<Message> forward;

    private volatile static UniformReliableBroadcast uniformReliableBroadcast = new UniformReliableBroadcast();


    private UniformReliableBroadcast()
    {
        bestEffortBroadcast = new BestEffortBroadcast();
        ack = new HashMap<Message, Integer>();
        delivered = new HashSet<Message>();
        forward = new HashSet<Message>();
    }

    public static UniformReliableBroadcast getInst(){
        return uniformReliableBroadcast;
    }

    public synchronized void Broadcast(int content){
        int messageId = SendEvent.NextId();

        int processId = Process.getInstance().Id;
        Message message = new Message(messageId, processId);
        forward.add(message);
        //System.out.println("URB: " + Process.getInstance().Id + " Broadcast Message #" + message.getMessageId());

        System.out.println("b " +  Process.getInstance().Id);


        bestEffortBroadcast.Broadcast(content, processId, messageId, ProtocolTypeEnum.UniformReliableBroadcast, messageId);
        Process.getInstance().Logger.WriteToLog("b " +  Process.getInstance().Id);
    }

    public synchronized boolean Deliver(Message message, Message originalMessage, int content, int portReceived, InetAddress addressReceived) throws IOException {

        boolean deliver = false;
        if(bestEffortBroadcast.Deliver(message, content, portReceived, addressReceived)){

            //System.out.println("BEB: " + Process.getInstance().Id + " Message #" + message.getMessageId() + ":From Process: " + message.getProcessId() + " is delivered");
            int count = ack.getOrDefault(originalMessage,0);
            ack.put(originalMessage, count + 1);

            if(!forward.contains(originalMessage)){
                forward.add(originalMessage);


                var id = SendEvent.NextId();


                bestEffortBroadcast.Broadcast(content,originalMessage.getProcessId(), originalMessage.getMessageId(),
                        ProtocolTypeEnum.UniformReliableBroadcast, id);
            }
        }
        if(forward.contains(originalMessage)){
            if(canDeliver(originalMessage) && !delivered.contains(originalMessage)){
                delivered.add(originalMessage);
                //System.out.println("URB: " + Process.getInstance().Id + " Message #" + message.getMessageId() + ":From Process: " + originalMessage.getProcessId() + " is delivered");

                System.out.println("d " +  originalMessage.getProcessId() + " " + originalMessage.getMessageId());

                Process.getInstance().Logger.WriteToLog("d " +  originalMessage.getProcessId() + " " + originalMessage.getMessageId());
                deliver = true;
            }
        }
        return deliver;
    }

    public synchronized boolean canDeliver(Message originalMessage){
        int numOfProc = Process.getInstance().processes.size();
        int count = ack.getOrDefault(originalMessage,0);
        return count > numOfProc / 2;
    }
}
