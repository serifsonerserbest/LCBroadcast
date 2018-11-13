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


public class UniformReliableBroadcast {

    private BestEffortBroadcast bestEffortBroadcast;

    private HashMap<MessageModel, Integer> ack;
    private HashSet<MessageModel> delivered;
    private HashSet<MessageModel> forward;

    public UniformReliableBroadcast() {

        bestEffortBroadcast = new BestEffortBroadcast();
        ack = new HashMap<>();
        delivered = new HashSet<>();
        forward = new HashSet<>();

    }

    public void Broadcast(int content) {

        int messageId = SendEvent.NextId();
        int processId = Process.getInstance().Id;
        MessageModel message = new MessageModel(messageId, processId);
        forward.add(message);

        bestEffortBroadcast.Broadcast(content, processId, messageId, ProtocolTypeEnum.UniformReliableBroadcast, messageId);
    }

    //for FIFOBroadcast
    public void Broadcast(int content, int fifoId) {

        int messageId = SendEvent.NextId();
        int processId = Process.getInstance().Id;
        MessageModel message = new MessageModel(messageId, processId);
        forward.add(message);
        bestEffortBroadcast.Broadcast(content, processId, messageId, ProtocolTypeEnum.FIFOBroadcast, messageId, fifoId);
    }

    public boolean Deliver(MessageModel message, MessageModel originalMessage, int content, int portReceived, InetAddress addressReceived, int fifoId) throws IOException {

        boolean deliver = false;
        if (bestEffortBroadcast.Deliver(message, content, portReceived, addressReceived)) {
            int count = ack.getOrDefault(originalMessage, 0);
            ack.put(originalMessage, count + 1);

            if (!forward.contains(originalMessage)) {
                forward.add(originalMessage);
                int id = SendEvent.NextId();
                bestEffortBroadcast.Broadcast(content, originalMessage.getProcessId(), originalMessage.getMessageId(),
                        ProtocolTypeEnum.FIFOBroadcast, id, fifoId);
            }
        }
        if (forward.contains(originalMessage)) {
            if (canDeliver(originalMessage) && !delivered.contains(originalMessage)) {
                delivered.add(originalMessage);
                deliver = true;
            }
        }
        return deliver;
    }

    public boolean canDeliver(MessageModel originalMessage) {

        int numOfProc = Process.getInstance().processes.size();
        int count = ack.getOrDefault(originalMessage, 0);
        return count > numOfProc / 2;
    }
}
