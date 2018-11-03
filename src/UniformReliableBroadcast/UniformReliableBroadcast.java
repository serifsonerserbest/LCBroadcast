package UniformReliableBroadcast;

import BestEffordBroadcast.BestEffortBroadcast;
import Enums.ProtocolTypeEnum;
import Models.Message;
import PerfectLink.SendEvent;
import Process.Process;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;


public class UniformReliableBroadcast {
    private BestEffortBroadcast bestEffortBroadcast;

    private HashMap<Message, Integer> ack;
    private HashSet<Message> delivered;
    private HashSet<Message> forward;

    //private volatile static UniformReliableBroadcast uniformReliableBroadcast = new UniformReliableBroadcast();


    public UniformReliableBroadcast()
    {
        bestEffortBroadcast = new BestEffortBroadcast();
        ack = new HashMap<Message, Integer>();
        delivered = new HashSet<Message>();
        forward = new HashSet<Message>();
        ack = new HashMap<>();
        delivered = new HashSet<>();
        forward = new HashSet<>();

    }

    //public static UniformReliableBroadcast getInst(){
        //return uniformReliableBroadcast;
    //}


    public synchronized void Broadcast(int content){
        int messageId = SendEvent.NextId();
        int processId = Process.getInstance().Id;
        Message message = new Message(messageId, processId);
        forward.add(message);

        //System.out.println("URB: " + Process.Process.getInstance().Id + " Broadcast Message #" + message.getMessageId());

        //System.out.println("b " +  Process.getInstance().Id);
        bestEffortBroadcast.Broadcast(content, processId, messageId, ProtocolTypeEnum.UniformReliableBroadcast, messageId);
        //Process.getInstance().Logger.WriteToLog("b " +  Process.getInstance().Id);
    }
    //for FIFOBroadcast
    public synchronized void Broadcast(int content, int fifoId) {
        int messageId = SendEvent.NextId();
        int processId = Process.getInstance().Id;
        Message message = new Message(messageId, processId);
        forward.add(message);
        bestEffortBroadcast.Broadcast(content, processId, messageId, ProtocolTypeEnum.FIFOBroadcast, messageId, fifoId);
    }

    public synchronized boolean Deliver(Message message, Message originalMessage, int content, int portReceived, InetAddress addressReceived, int fifoId) throws IOException {

        boolean deliver = false;
        if(bestEffortBroadcast.Deliver(message, content, portReceived, addressReceived)){
            //System.out.println("Best Efford Delivered");
            //System.out.println("BEB: " + Process.Process.getInstance().Id + " Message #" + message.getMessageId() + ":From Process.Process: " + message.getProcessId() + " is delivered");
            int count = ack.getOrDefault(originalMessage,0);
            ack.put(originalMessage, count + 1);

            if(!forward.contains(originalMessage)){
                forward.add(originalMessage);
                int id = SendEvent.NextId();
                bestEffortBroadcast.Broadcast(content,originalMessage.getProcessId(), originalMessage.getMessageId(),
                        ProtocolTypeEnum.FIFOBroadcast, id, fifoId);
            }
        }
        if(forward.contains(originalMessage)){
            if(canDeliver(originalMessage) && !delivered.contains(originalMessage)){
                delivered.add(originalMessage);
                //System.out.println("URB: " + Process.Process.getInstance().Id + " Message #" + message.getMessageId() + ":From Process.Process: " + originalMessage.getProcessId() + " is delivered");

                //System.out.println("d " +  originalMessage.getProcessId() + " " + originalMessage.getMessageId());
                //System.out.println("URB: " + Process.getInstance().Id + " Message #" + message.getMessageId() + ":From Process: " + originalMessage.getProcessId() + " is delivered");
                //Process.getInstance().Logger.WriteToLog("d " +  originalMessage.getProcessId() + " " + originalMessage.getMessageId());

                //For Debugging
                //Process.Process.getInstance().Logger.Logger.WriteToLog(Integer.toString(originalMessage.getMessageId()));
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
