package com.epfl.da;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashSet;

public class DeliverEvent {

    class Message{
        int messageId;
        int port;
        public Message(int messageId, int port) {
            this.messageId = messageId;
            this.port = port;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Message message = (Message) o;
            return hashCode() == message.hashCode();
        }
        public int cantorPairing() {
            int sum = this.messageId + this.port;
            if(sum % 2 == 0) sum = sum / 2 * (sum + 1);
            else sum = (sum + 1) / 2 * sum;
            int cantorValue = sum + this.port;
            return cantorValue;
        }
        @Override
        public int hashCode() {
            return this.cantorPairing();
        }
    }

    HashSet<Message> isReceived;
    DatagramSocket socketIn;

    public DeliverEvent() {
        isReceived = new HashSet<>();
    }

    public void ReceiveMessage(int port) throws IOException {

        socketIn = new DatagramSocket(port);
        byte[] packetReceived = new byte[1024];
        InetAddress addressReceived;
        byte[] messageReceived;
        int portReceived;

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
            Message message = new Message(messageId, portReceived);
            if(isReceived.contains(message)) {
                System.out.println("Message #" + messageId + ": " + content + " duplicate");
            }
            else {
                System.out.println("Message #" + messageId + ": " + content + " is delivered");
                isReceived.add(message);
            }
            sendAck(socketIn, portReceived, addressReceived, messageId);
        }

    }

    public void sendAck(DatagramSocket socket, int port, InetAddress address, int messageId) throws IOException {
        int[] data = {messageId};
        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(data);
        byte[] sentData = byteBuffer.array();
        socket.send(new DatagramPacket(sentData, sentData.length, address, port));
    }
}
