package com.epfl.da.PerfectLink;

import com.epfl.da.Enums.ProtocolTypeEnum;
import com.epfl.da.Interfaces.BaseHandler;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SendEvent {

    static final int timeoutVal = 300;		// 300ms until timeout
    private static int messageId = 0;

    public BaseHandler receiveAcknowledgeHandler;
    ExecutorService service;
    public SendEvent() {
        service = Executors.newCachedThreadPool();
    }


    public void SendMessage(int message, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol)
    {
        DatagramSocket socketOut;

        try {
            socketOut = new DatagramSocket();                // outgoing channel
            socketOut.setSoTimeout(timeoutVal);
            ++messageId;
             service.submit(new ThreadSend(socketOut, destPort, destAddress, message, messageId, protocol));

            //ThreadSend th_out = new ThreadSend(socketOut, destPort, destAddress, message, messageId, receiveAcknowledgeHandler);
            //th_out.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private class ThreadSend extends Thread {
        private DatagramSocket socketOut;
        private int destPort;
        private InetAddress destAddress;
        int content;
        int messageId;
        ProtocolTypeEnum protocol;

        // ThreadSend constructor
        public ThreadSend(DatagramSocket socketOut, int destPort, InetAddress destAddress, int content, int messageId, ProtocolTypeEnum protocol) {
            this.socketOut = socketOut;
            this.destPort = destPort;
            this.destAddress = destAddress;
            this.content = content;
            this.messageId = messageId;
            this.protocol = protocol;
        }

        public void run() {

            byte[] in_data = new byte[32];    // ack packet with no data
            int[] data = {this.messageId, protocol.ordinal(), this.content};
            ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            intBuffer.put(data);
            byte[] out_data = byteBuffer.array();

            DatagramPacket sendingPacket = new DatagramPacket(out_data, out_data.length, destAddress, destPort);
            DatagramPacket receivePacket =  new DatagramPacket(in_data, in_data.length);
            boolean result = false;
            try {
                    result = SendDataMessage(sendingPacket, receivePacket);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                socketOut.close();        // close outgoing socket
                System.out.println("SendEvent: socketOut closed!");
            }
            if(receiveAcknowledgeHandler != null && result) {
                receiveAcknowledgeHandler.handle();
            }
        }

        private boolean SendMessage(DatagramPacket sendingPacket, DatagramPacket receivePacket, int attempts) throws IOException {
            int counter = 0;
            while(attempts == -1 || counter <  attempts) {
                socketOut.send(sendingPacket);
                System.out.println("SendEvent: Sent " + messageId);
                try {
                    socketOut.receive(receivePacket);
                    ByteBuffer wrapped = ByteBuffer.wrap(receivePacket.getData()); // big-endian by default
                    int messageId = wrapped.getInt();
                    System.out.println("SendEvent: Received Ack " + messageId);
                    if (this.messageId == messageId) {
                        return true;
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Timeout reached!!! " + e);
                }
                ++counter;
            }
            return false;
        }

        private boolean SendDataMessage(DatagramPacket sendingPacket, DatagramPacket receivePacket) throws IOException {
            return SendMessage(sendingPacket,receivePacket, -1);
        }
    }
}
