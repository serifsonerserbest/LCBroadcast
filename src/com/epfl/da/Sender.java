package com.epfl.da;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.concurrent.Semaphore;
import java.util.Timer;
import java.util.TimerTask;

public class Sender {

    static final int timeoutVal = 300;		// 300ms until timeout
    private static int messageId = 0;

    public Sender() {
    }


    public void SendMessage(int message, InetAddress dst_addr, int sk4_dst_port)
    {
        DatagramSocket sk1;

        try {
            sk1 = new DatagramSocket();                // outgoing channel
            sk1.setSoTimeout(timeoutVal);
            ++messageId;
            OutThread th_out = new OutThread(sk1, sk4_dst_port, dst_addr, message, messageId);
            th_out.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private class OutThread extends Thread {
        private DatagramSocket sk_out;
        private int dst_port;
        private InetAddress dst_addr;
        int content;
        int messageId;

        // OutThread constructor
        public OutThread(DatagramSocket sk_out, int dst_port, InetAddress dst_addr, int content, int messageId) {
            this.sk_out = sk_out;
            this.dst_port = dst_port;
            this.dst_addr = dst_addr;
            this.content = content;
            this.messageId = messageId;
        }


        public void run() {

            byte[] in_data = new byte[32];    // ack packet with no data
            int[] data = {this.messageId, this.content};
            ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            intBuffer.put(data);
            byte[] out_data = byteBuffer.array();

            DatagramPacket sendingPacket = new DatagramPacket(out_data, out_data.length, dst_addr, dst_port);
            DatagramPacket receivePacket =  new DatagramPacket(in_data, in_data.length);
            try {
                // while there are still packets yet to be received by receiver
                while (true) {

                    sk_out.send(sendingPacket);
                    System.out.println("Sender: Sent " + messageId);
                    try {
                        sk_out.receive(receivePacket);
                        ByteBuffer wrapped = ByteBuffer.wrap(in_data); // big-endian by default
                        int messageId = wrapped.getInt();
                        System.out.println("Sender: Received Ack " + messageId);
                        if (this.messageId == messageId) {
                            break;
                        }
                    } catch (SocketTimeoutException e) {
                        System.out.println("Timeout reached!!! " + e);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                sk_out.close();        // close outgoing socket
                System.out.println("Sender: sk_out closed!");
            }

        }

    }
}
