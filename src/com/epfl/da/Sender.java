package com.epfl.da;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.concurrent.Semaphore;
import java.util.Timer;
import java.util.TimerTask;

public class Sender {

    static final int timeoutVal = 300;		// 300ms until timeout
    Semaphore s;				// guard CS for base, nextSeqNum
    boolean isTransferComplete;	// if receiver has completely received the file
    private static int messageId = 0;

    public Sender() {
        s = new Semaphore(1);
        isTransferComplete = false;
    }


    public void SendMessage(int message, InetAddress dst_addr, int sk4_dst_port)
    {
        DatagramSocket sk1, sk4;

        try {
            sk1 = new DatagramSocket();                // outgoing channel
            sk4 = new DatagramSocket(sk4_dst_port);    // incoming channel
            ++messageId;
            InThread th_in = new InThread(sk4, messageId);
            OutThread th_out = new OutThread(sk1, sk4_dst_port, dst_addr, message, messageId);
            th_in.start();
            th_out.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private class OutThread extends Thread {
        private DatagramSocket sk_out;
        private int dst_port;
        private InetAddress dst_addr;
        int message;
        int messageId;

        // OutThread constructor
        public OutThread(DatagramSocket sk_out, int dst_port, InetAddress dst_addr, int message, int messageId) {
            this.sk_out = sk_out;
            this.dst_port = dst_port;
            this.dst_addr = dst_addr;
            this.message = message;
            this.messageId = messageId;
        }


        public void run(){
            try{
                // create byte stream

                try {
                    // while there are still packets yet to be received by receiver
                    while (!isTransferComplete){
                        // send packets if window is not yet full
                            s.acquire();	/***** enter CS *****/
                            int[] data = {this.messageId, this.message};
                            ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
                            IntBuffer intBuffer = byteBuffer.asIntBuffer();
                            intBuffer.put(data);
                            byte[] out_data = byteBuffer.array();


                            // send the packet
                            sk_out.send(new DatagramPacket(out_data, out_data.length, dst_addr, dst_port));
                            System.out.println("Sender: Sent seqNum ");
                            s.release();	/***** leave CS *****/
                            sleep(timeoutVal);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    sk_out.close();		// close outgoing socket
                    System.out.println("Sender: sk_out closed!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

    }


    private class InThread extends Thread {
        private DatagramSocket sk_in;
        int messageId;

        // InThread constructor
        public InThread(DatagramSocket sk_in, int messageId) {
            this.sk_in = sk_in;
            this.messageId = messageId;
        }


        // receiving process (updates base)
        public void run() {
            try {
                byte[] in_data = new byte[32];	// ack packet with no data
                DatagramPacket in_pkt = new DatagramPacket(in_data, in_data.length);
                try {
                    // while there are still packets yet to be received by receiver
                    while (!isTransferComplete) {
                        sk_in.receive(in_pkt);

                        ByteBuffer wrapped = ByteBuffer.wrap(in_data); // big-endian by default
                        int messageId = wrapped.getInt();

                        System.out.println("Sender: Received Ack " + messageId);

                        s.acquire();	/***** enter CS *****/
                        if(this.messageId == messageId)
                        {
                            isTransferComplete = true;
                        }
                        s.release();	/***** leave CS *****/

                        // else if ack corrupted, do nothing
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    sk_in.close();
                    System.out.println("Sender: sk_in closed!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }


    }


}
