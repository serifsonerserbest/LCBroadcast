package FIFOBroadcast;

import UniformReliableBroadcast.UniformReliableBroadcast;
import BestEffordBroadcast.BestEffortBroadcast;
import Enums.ProtocolTypeEnum;
import Models.Message;
import PerfectLink.SendEvent;
import Process.Process;
import UniformReliableBroadcast.UniformReliableBroadcast;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;

public class FIFOBroadcast {
    private volatile static FIFOBroadcast fifoBroadcast = new FIFOBroadcast();
    private UniformReliableBroadcast uniformReliableBroadcast;
    int lsn;
    HashMap<Message, Integer> pending;
    int next[];
    private FIFOBroadcast()
    {
        uniformReliableBroadcast = new UniformReliableBroadcast();
        lsn = 0;
        pending = new HashMap<>();
        int numberOfProcesses = Process.getInstance().processes.size();
        next = new int[numberOfProcesses + 1];
        for(int i = 0; i <= numberOfProcesses; ++ i)
            next[i] = 1;
    }

    public static FIFOBroadcast getInst(){
        return fifoBroadcast;
    }


    public synchronized void Broadcast(int content){
        System.out.println("b " +  Process.getInstance().Id);
        lsn ++;
        uniformReliableBroadcast.Broadcast(content, lsn);
        Process.getInstance().Logger.WriteToLog("b " +  Process.getInstance().Id);
    }

    public synchronized void Deliver(Message message, Message originalMessage, int content, int portReceived, InetAddress addressReceived, int fifoId) throws IOException {
        //System.out.println("Inside deliver");
        if(uniformReliableBroadcast.Deliver(message, originalMessage, content, portReceived, addressReceived, fifoId)) {
            //System.out.println("URB Delivered");
            int originalProcessId = originalMessage.getProcessId();
            Message fifoMessage = new Message(fifoId, originalProcessId);
            pending.put(fifoMessage, originalMessage.getMessageId());
            while(true) {
                int nextId = next[originalProcessId];
                Message fifoKey = new Message(nextId, originalProcessId);
                if(pending.containsKey(fifoKey)) {
                    pending.remove(fifoKey);
                    System.out.println("d " +  originalMessage.getProcessId() + " " + next[originalProcessId]);
                    //System.out.println("FIFO: " + Process.getInstance().Id + " Message #" + message.getMessageId() + ":From Process: " + originalMessage.getProcessId() + " is delivered");
                    Process.getInstance().Logger.WriteToLog("d " +  originalMessage.getProcessId() + " " + next[originalProcessId]);
                    next[originalProcessId] ++;
                }
                else break;
            }
        }
    }
}
