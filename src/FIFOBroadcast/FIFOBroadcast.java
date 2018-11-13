package FIFOBroadcast;

import Models.MessageModel;
import UniformReliableBroadcast.UniformReliableBroadcast;
import Process.Process;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;


public class FIFOBroadcast {

    private volatile static FIFOBroadcast fifoBroadcast = new FIFOBroadcast();
    private UniformReliableBroadcast uniformReliableBroadcast;

    int lsn;
    volatile ConcurrentHashMap<MessageModel, Integer> pending;
    int next[];

    private FIFOBroadcast() {

        uniformReliableBroadcast = new UniformReliableBroadcast();
        lsn = 0;
        pending = new ConcurrentHashMap<>();
        int numberOfProcesses = Process.getInstance().processes.size();
        next = new int[numberOfProcesses + 1];
        for (int i = 0; i <= numberOfProcesses; ++i)
            next[i] = 1;
    }

    public static FIFOBroadcast getInst() {

        return fifoBroadcast;
    }

    public void Broadcast(int content) {

        lsn++;
        //System.out.println("b " + lsn);
        uniformReliableBroadcast.Broadcast(content, lsn);
        Process.getInstance().Logger.WriteToLog("b " + lsn);
    }

    public void Deliver(MessageModel message, MessageModel originalMessage, int content, int portReceived, InetAddress addressReceived, int fifoId) throws IOException {

        if (uniformReliableBroadcast.Deliver(message, originalMessage, content, portReceived, addressReceived, fifoId)) {
            int originalProcessId = originalMessage.getProcessId();
            MessageModel fifoMessage = new MessageModel(fifoId, originalProcessId);
            pending.put(fifoMessage, originalMessage.getMessageId());
            while (true) {
                int nextId = next[originalProcessId];
                MessageModel fifoKey = new MessageModel(nextId, originalProcessId);
                Integer removed = pending.remove(fifoKey);
                if (removed != null) {
                    System.out.println("d " + originalMessage.getProcessId() + " " + next[originalProcessId]);
                    Process.getInstance().Logger.WriteToLog("d " + originalMessage.getProcessId() + " " + next[originalProcessId]);
                    next[originalProcessId]++;
                } else break;
            }
        }
    }
}
