package Listener;

import FIFOBroadcast.FIFOBroadcast;
import Models.MessageModel;
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
    FIFOBroadcast fifoBroadcast;

    public Listener(FIFOBroadcast fifoBroadcast) {

        System.out.println("Listening ...");
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
            int content = messageArray[1];
            int processId = messageArray[2];
            MessageModel message = new MessageModel(messageId, processId, content);

            int originalProcessId = messageArray[3];
            int originalMessageId = messageArray[4];
            MessageModel messageOriginal = new MessageModel(originalMessageId, originalProcessId, content);

            int fifoId = messageArray[5];
            try {
                fifoBroadcast.Deliver(message, messageOriginal, content, portReceived, addressReceived, fifoId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
