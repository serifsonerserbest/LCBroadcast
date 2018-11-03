package Listener;

import BestEffordBroadcast.BestEffortBroadcast;
import Enums.ProtocolTypeEnum;
import FIFOBroadcast.FIFOBroadcast;
import Interfaces.MessageHandler;
import Models.Message;
import PerfectLink.PerfectLink;
import UniformReliableBroadcast.UniformReliableBroadcast;
import Process.Process;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Listener {
    DatagramSocket socketIn;
    PerfectLink perfectLink;
    BestEffortBroadcast bestEffortBroadcast;
    UniformReliableBroadcast uniformReliableBroadcast;
    FIFOBroadcast fifoBroadcast;
    public MessageHandler onMessageReceive;

    public Listener(PerfectLink perfectLink, BestEffortBroadcast bestEffortBroadcast, UniformReliableBroadcast uniformReliableBroadcast, FIFOBroadcast fifoBroadcast) {
        System.out.println("Listening ...");
        this.perfectLink = perfectLink;
        this.bestEffortBroadcast = bestEffortBroadcast;
        this.uniformReliableBroadcast = uniformReliableBroadcast;
        this.fifoBroadcast = fifoBroadcast;
    }

    public void Start() throws IOException {

        socketIn = new DatagramSocket(Process.getInstance().Port);
        byte[] packetReceived = new byte[1024];
        InetAddress addressReceived;
        byte[] messageReceived;
        int portReceived;

        //perfectLink.onMessageReceive = onMessageReceive;
        ExecutorService threadPool = Executors.newCachedThreadPool();

        while (true) {
            //receiving packet
            DatagramPacket receivedPacket = new DatagramPacket(packetReceived, packetReceived.length);
            socketIn.receive(receivedPacket);

            addressReceived = receivedPacket.getAddress();
            portReceived = receivedPacket.getPort();
            messageReceived = receivedPacket.getData();

            threadPool.submit(new RequestProcessing(Arrays.copyOf(messageReceived, messageReceived.length), portReceived, InetAddress.getByName(addressReceived.getHostAddress())));
        }
    }

    public class RequestProcessing implements Runnable {
        byte[] messageReceived;
        int portReceived;
        InetAddress addressReceived;

        public RequestProcessing(final byte[] messageReceived, final int portReceived, final InetAddress addressReceived) {
            this.messageReceived = messageReceived;
            this.portReceived = portReceived;
            this.addressReceived = addressReceived;
        }

        @Override
        public void run() {
            IntBuffer intBuf =
                    ByteBuffer.wrap(messageReceived)
                            .order(ByteOrder.BIG_ENDIAN)
                            .asIntBuffer();

            int[] messageArray = new int[intBuf.remaining()];
            intBuf.get(messageArray);

            //PRE PROCESSING THE MESSAGE
            int messageId = messageArray[0];
            int protocol = messageArray[1];
            int content = messageArray[2];
            int processId = messageArray[3];
            Message message = new Message(messageId, processId);

            //

            // DELIVER MESSAGE ACCORDING TO PROTOCOLS
            try {
                if (protocol == ProtocolTypeEnum.PerfectLink.ordinal()) {
                    perfectLink.Deliver(message, content, portReceived, addressReceived);
                }
                else if (protocol == ProtocolTypeEnum.BestEffortBroadcast.ordinal()) {
                    bestEffortBroadcast.Deliver(message, content, portReceived, addressReceived);
                }
                else if (protocol == ProtocolTypeEnum.UniformReliableBroadcast.ordinal()) {
                    int originalProcessId = messageArray[4];
                    int originalMessageId = messageArray[5];
                    Message messageOriginal = new Message(originalMessageId, originalProcessId);
                    int fifoId = messageArray[6];
                    uniformReliableBroadcast.Deliver(message, messageOriginal,content, portReceived, addressReceived, fifoId);
                }
                else if(protocol == ProtocolTypeEnum.FIFOBroadcast.ordinal()) {
                    int originalProcessId = messageArray[4];
                    int originalMessageId = messageArray[5];
                    Message messageOriginal = new Message(originalMessageId, originalProcessId);
                    int fifoId = messageArray[6];
                    fifoBroadcast.Deliver(message, messageOriginal,content, portReceived, addressReceived, fifoId);
                }
                else {
                    System.out.println("Unknown protocol " + protocol);
                    return;
                }
            } catch (IOException e) {
                System.out.println("RequestProcessing error: " + e);
            }
        }
    }
}
