package com.epfl.da;

import com.epfl.da.Enums.ProtocolTypeEnum;
import com.epfl.da.Interfaces.BaseHandler;
import com.epfl.da.Interfaces.MessageHandler;
import com.epfl.da.Models.Message;
import com.epfl.da.PerfectLink.PerfectLink;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Listener {
    HashSet<Message> receivedMessages;
    DatagramSocket socketIn;
    PerfectLink perfectLink;

    public MessageHandler onMessageReceive;

    public Listener() {
        receivedMessages = new HashSet<>();
        perfectLink = new PerfectLink();
    }

    public void Start(int port) throws IOException {

        socketIn = new DatagramSocket(port);
        byte[] packetReceived = new byte[1024];
        InetAddress addressReceived;
        byte[] messageReceived;
        int portReceived;

        perfectLink.onMessageReceive = onMessageReceive;
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
            int messageId = messageArray[0];
            int protocol = messageArray[1];
            int content = messageArray[2];
            Message message = new Message(messageId, portReceived);
            if (receivedMessages.contains(message)) {
                System.out.println("Message #" + messageId + ": " + content + " duplicate");
            } else {
                System.out.println("Message #" + messageId + ": " + content + " is delivered");
                receivedMessages.add(message);
                System.out.println(receivedMessages.size());
            }
            try {
                if (protocol == ProtocolTypeEnum.PerfectLink.ordinal()) {
                    perfectLink.Deliver(portReceived, addressReceived, messageId, content);

                } else if (protocol == ProtocolTypeEnum.PerfectLink.ordinal()) {

                } else {
                    System.out.println("Unknown protocol " + protocol);
                    return;
                }
            } catch (IOException e) {
                System.out.println("RequestProcessing error: " + e);
            }
        }
    }
}
