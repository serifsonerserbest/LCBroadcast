package com.epfl.da;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Timer;
import java.util.concurrent.Semaphore;

public class Sender {

    static final int timeoutVal = 300;		// 300ms until timeout
    Timer timer;				// for timeouts
    Semaphore s;				// guard CS for base, nextSeqNum
    boolean isTransferComplete;	// if receiver has completely received the file
    private static int messageId = 0;

    public Sender() {
        s = new Semaphore(1);
        isTransferComplete = false;
    }


    public void SendMessage(int message, InetAddress dst_addr, int sk1_dst_port, int sk4_dst_port)
    {
        DatagramSocket sk1, sk4;

        try {
            sk1 = new DatagramSocket();                // outgoing channel
            sk4 = new DatagramSocket(sk4_dst_port);    // incoming channel
            ++messageId;
            InThread th_in = new InThread(sk4);
            OutThread th_out = new OutThread(sk1, sk1_dst_port, sk4_dst_port, dst_addr, message, messageId);
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
        private int recv_port;
        int message;

        // OutThread constructor
        public OutThread(DatagramSocket sk_out, int dst_port, int recv_port, InetAddress dst_addr, int message, int messageId) {
            this.sk_out = sk_out;
            this.dst_port = dst_port;
            this.recv_port = recv_port;
            this.dst_addr = dst_addr;
            this.message = message;
        }


        public void run(){
            try{
                // create byte stream

                try {
                    // while there are still packets yet to be received by receiver
                    while (!isTransferComplete){
                        // send packets if window is not yet full

                            s.acquire();	/***** enter CS *****/
                            setTimer(true);	// if first packet of window, start timer

                            //TODO: Convert message ID and message to array of bytes.
                            int[] data = {messageId, message};
                            ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
                            IntBuffer intBuffer = byteBuffer.asIntBuffer();
                            intBuffer.put(data);
                            byte[] out_data = byteBuffer.array();

                            boolean isFinalSeqNum = false;

                            // send the packet
                            sk_out.send(new DatagramPacket(out_data, out_data.length, dst_addr, dst_port));
                            System.out.println("Sender: Sent seqNum " + nextSeqNum);

                            // update nextSeqNum if currently not at FinalSeqNum
                            if (!isFinalSeqNum) nextSeqNum++;
                            s.release();	/***** leave CS *****/

                        sleep(5);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    setTimer(false);	// close timer
                    sk_out.close();		// close outgoing socket
                    fis.close();		// close FileInputStream
                    System.out.println("Sender: sk_out closed!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        private byte[] intToBytes( final int i ) {
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.putInt(i);
            return bb.array();
        }


    }


    private class InThread extends Thread {
        private DatagramSocket sk_in;

        // InThread constructor
        public InThread(DatagramSocket sk_in) {
            this.sk_in = sk_in;
        }
    }


}
