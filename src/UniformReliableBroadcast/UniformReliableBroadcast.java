package UniformReliableBroadcast;

import BestEffordBroadcast.BestEffortBroadcast;
import Enums.ProtocolTypeEnum;
import Models.MessageModel;
import PerfectLink.SendEvent;
import Process.Process;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class UniformReliableBroadcast {

    private BestEffortBroadcast bestEffortBroadcast;

    private volatile ConcurrentHashMap<MessageModel, AtomicInteger> ack;
    private volatile ConcurrentHashMap<MessageModel, AtomicInteger> delivered;
    private volatile ConcurrentHashMap<MessageModel, AtomicInteger> forward;

    public UniformReliableBroadcast() {

        bestEffortBroadcast = new BestEffortBroadcast();
        ack = new ConcurrentHashMap<>();
        delivered = new ConcurrentHashMap<>();
        forward = new ConcurrentHashMap<>();

    }

    public void Broadcast(int content) {

        int messageId = SendEvent.NextId();
        int processId = Process.getInstance().Id;
        MessageModel message = new MessageModel(messageId, processId);
        forward.put(message, new AtomicInteger(1));

        bestEffortBroadcast.Broadcast(content, processId, messageId, ProtocolTypeEnum.UniformReliableBroadcast, messageId);
    }

    //for FIFOBroadcast
    public void Broadcast(int content, int fifoId) {

        int messageId = SendEvent.NextId();
        int processId = Process.getInstance().Id;
        MessageModel message = new MessageModel(messageId, processId);
        forward.put(message, new AtomicInteger(1));
        bestEffortBroadcast.Broadcast(content, processId, messageId, ProtocolTypeEnum.FIFOBroadcast, messageId, fifoId);
    }

    public boolean Deliver(MessageModel message, MessageModel originalMessage, int content, int portReceived, InetAddress addressReceived, int fifoId) throws IOException {

        boolean deliver = false;
        if (bestEffortBroadcast.Deliver(message, content, portReceived, addressReceived)) {

            //int count = ack.getOrDefault(originalMessage, 0);
            ack.computeIfAbsent(originalMessage, x ->  new AtomicInteger(0));
            ack.computeIfPresent(originalMessage, (key, value) -> value).incrementAndGet();

            if (!forward.containsKey(originalMessage)) {
                forward.put(originalMessage, new AtomicInteger(1));
                int id = SendEvent.NextId();
                bestEffortBroadcast.Broadcast(content, originalMessage.getProcessId(), originalMessage.getMessageId(),
                        ProtocolTypeEnum.FIFOBroadcast, id, fifoId);
            }
        }
        if (forward.containsKey(originalMessage)) {
            if (canDeliver(originalMessage) && !delivered.containsKey(originalMessage)) {
                delivered.put(originalMessage, new AtomicInteger(1));
                deliver = true;
            }
        }
        return deliver;
    }

    public boolean canDeliver(MessageModel originalMessage) {

        int numOfProc = Process.getInstance().processes.size();
        AtomicInteger count = ack.getOrDefault(originalMessage, new AtomicInteger(0));
        //System.out.println(count + " " + numOfProc);
        //System.out.println(count.get() > numOfProc / 2);

        return count.get() > numOfProc / 2;
    }
}
