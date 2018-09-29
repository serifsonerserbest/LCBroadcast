package com.epfl.da;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.Semaphore;

public class Receiver {


  /*  public class Message{
        int messageId;
        InetAddress address;
        public Message(int messageId, InetAddress address) {
            this.messageId = messageId;
            this.address = address;
        }
    }*/
    HashMap<String, HashSet<Integer>> isReceived;
    Semaphore s;
    DatagramSocket socketIn;
    public Receiver() {
        s = new Semaphore(1);
        isReceived = new HashMap<>();
    }
    public void ReceiveMessage(int port) throws IOException {
        socketIn = new DatagramSocket(port);
        byte[] packetReceived = new byte[1024];
        InetAddress addressReceived;
        byte[] messageReceived;
        int portReceived;
        int ackType;
        while(true) {
            //receiving packet
            DatagramPacket receivedPacket = new DatagramPacket(packetReceived, packetReceived.length);
            socketIn.receive(receivedPacket);
            addressReceived = receivedPacket.getAddress();
            portReceived = receivedPacket.getPort();
            messageReceived = receivedPacket.getData();
            IntBuffer intBuf =
                    ByteBuffer.wrap(messageReceived)
                            .order(ByteOrder.BIG_ENDIAN)
                            .asIntBuffer();

            int[] messageArray = new int[intBuf.remaining()];
            intBuf.get(messageArray);
            int messageId = messageArray[0];
            int content = messageArray[1];
            //Message message = new Message(messageId, addressReceived);
            if(!isReceived.containsKey(addressReceived.getHostAddress()))
            {
                isReceived.put(addressReceived.getHostAddress(),new HashSet<>());
            }
            if(isReceived.get(addressReceived.getHostAddress()).contains(messageId)) {
                ackType = 0;
                System.out.println("Message #" + messageId + ": " + content + " duplicate");
            }
            else {
                ackType = 1;
                System.out.println("Message #" + messageId + ": " + content + " was successfully received");
                isReceived.get(addressReceived.getHostAddress()).add(messageId);
            }
            sendAck(socketIn, portReceived, addressReceived, ackType, messageId);
        }

    }
    public void sendAck(DatagramSocket socket, int port, InetAddress address, int ackType, int messageId) throws IOException {
        int[] data = {/*ackType,*/ messageId};
        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(data);
        byte[] sentData = byteBuffer.array();
        socket.send(new DatagramPacket(sentData, sentData.length, address, port));
    }
}
