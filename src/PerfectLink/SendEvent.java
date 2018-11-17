package PerfectLink;

import AppSettings.ApplicationSettings;
import Enums.ProtocolTypeEnum;
import Process.Process;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;


public class SendEvent {

    public volatile static AtomicInteger messageId ;

    volatile ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool( ApplicationSettings.getInstance().SenderThreadPoolSize);

    public SendEvent() {
        messageId = new AtomicInteger(0);
    }

    public static int NextId() {
        return messageId.incrementAndGet();
    }

    public void SendMessage(int content, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol, int originalProcessId, int originalMessageId, int messageId, int fifoId) {

        threadPool.submit(new ThreadSend(destPort, destAddress, content, messageId, protocol, originalProcessId, originalMessageId, fifoId));

    }

    private class ThreadSend extends Thread {

        private int destPort;
        private InetAddress destAddress;

        int content;
        int messageId;
        ProtocolTypeEnum protocol;
        int originalProcessId;
        int originalMessageId;
        int fifoId;

        // ThreadSend constructor
        public ThreadSend(int destPort, InetAddress destAddress, int content, int messageId, ProtocolTypeEnum protocol, int originalProcessId, int originalMessageId, int fifoId) {
            this.destPort = destPort;
            this.destAddress = destAddress;
            this.content = content;
            this.messageId = messageId;
            this.protocol = protocol;
            this.originalMessageId = originalMessageId;
            this.originalProcessId = originalProcessId;
            this.fifoId = fifoId;
            this.setName("Send Thread " + messageId);
        }

        public void run() {


            byte[] in_data = new byte[32];    // ack packet with no data

            int[] data = {this.messageId, protocol.ordinal(), this.content, Process.getInstance().Id, originalProcessId, originalMessageId, fifoId};
            ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            intBuffer.put(data);
            byte[] out_data = byteBuffer.array();

            DatagramPacket sendingPacket = new DatagramPacket(out_data, out_data.length, destAddress, destPort);
            DatagramPacket receivePacket = new DatagramPacket(in_data, in_data.length);
            DatagramSocket socketOut = null;
            try {
                socketOut = Process.getInstance().GetSocketFromQueue();
                System.out.println("s " + messageId + " port " + socketOut.getLocalPort());

                socketOut.setSoTimeout(ApplicationSettings.getInstance().timeoutVal);
                SendMessage(socketOut, sendingPacket, receivePacket, -1);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Process.getInstance().PutSocketToQuery(socketOut);
                //System.out.println("SendEvent: socketOut added to query!");
            }
        }

        private boolean SendMessage(DatagramSocket socketOut, DatagramPacket sendingPacket, DatagramPacket receivePacket, int attempts) throws IOException {

            int counter = 0;
            while (attempts == -1 || counter < attempts) {

                socketOut.send(sendingPacket);
                try {
                    socketOut.receive(receivePacket);
                    ByteBuffer wrapped = ByteBuffer.wrap(receivePacket.getData()); // big-endian by default
                    int messageId = wrapped.getInt();
                    System.out.println("Ack receive id: " + messageId + " expected :" + this.messageId + " port " + socketOut.getLocalPort());
                    if (this.messageId == messageId) {
                        return true;
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Timeout reached: From Process" + Process.getInstance().Id + " MessageId:" + messageId + e);
                }
                ++counter;
            }
            return false;
        }
    }
}
