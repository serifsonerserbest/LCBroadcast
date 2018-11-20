package PerfectLink;

import AppSettings.ApplicationSettings;
import Enums.ProtocolTypeEnum;
import Process.Process;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;


public class SendEvent {

    public static int messageId = 0;

    static ExecutorService service = Executors.newCachedThreadPool();

    public SendEvent() {
    }

    public synchronized static int NextId() {
        return ++messageId;
    }

    public synchronized void SendMessage(int content, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol, int originalProcessId, int originalMessageId, int messageId, int fifoId, int[] vectorClock) {

        DatagramSocket socketOut;
        try {
            socketOut = new DatagramSocket();                // outgoing channel
            socketOut.setSoTimeout(ApplicationSettings.getInstance().timeoutVal);
            service.submit(new ThreadSend(socketOut, destPort, destAddress, content, messageId, protocol, originalProcessId, originalMessageId, fifoId, vectorClock));

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
        int originalProcessId;
        int originalMessageId;
        int fifoId;
        int[] vectorClock;

        // ThreadSend constructor
        public ThreadSend(DatagramSocket socketOut, int destPort, InetAddress destAddress, int content, int messageId, ProtocolTypeEnum protocol, int originalProcessId, int originalMessageId, int fifoId, int[] vectorClock) {
            this.socketOut = socketOut;
            this.destPort = destPort;
            this.destAddress = destAddress;
            this.content = content;
            this.messageId = messageId;
            this.protocol = protocol;
            this.originalMessageId = originalMessageId;
            this.originalProcessId = originalProcessId;
            this.fifoId = fifoId;
            this.vectorClock = vectorClock;
        }

        private int[] concatenate(int[]... arrays) {
            int length = 0;
            for (int[] array : arrays) {
                length += array.length;
            }
            int[] result = new int[length];
            int pos = 0;
            for (int[] array : arrays) {
                for (int element : array) {
                    result[pos] = element;
                    pos++;
                }
            }
            return result;
        }

        public void run() {

            byte[] in_data = new byte[32];    // ack packet with no data

            int[] data = {this.messageId, protocol.ordinal(), this.content, Process.getInstance().Id, originalProcessId, originalMessageId, fifoId};
            if (vectorClock != null){
                data = concatenate(data, vectorClock);
            }

            ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            intBuffer.put(data);
            byte[] out_data = byteBuffer.array();

            DatagramPacket sendingPacket = new DatagramPacket(out_data, out_data.length, destAddress, destPort);
            DatagramPacket receivePacket = new DatagramPacket(in_data, in_data.length);

            try {
                SendMessage(sendingPacket, receivePacket, -1);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                socketOut.close();        // close outgoing socket
                //System.out.println("SendEvent: socketOut closed!");
            }
        }

        private boolean SendMessage(DatagramPacket sendingPacket, DatagramPacket receivePacket, int attempts) throws IOException {

            int counter = 0;
            while (attempts == -1 || counter < attempts) {

                socketOut.send(sendingPacket);
                try {
                    socketOut.receive(receivePacket);
                    ByteBuffer wrapped = ByteBuffer.wrap(receivePacket.getData()); // big-endian by default
                    int messageId = wrapped.getInt();

                    if (this.messageId == messageId) {
                        return true;
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Timeout reached: From Process" + Process.getInstance().Id + " to Port:" + destPort + e);
                }
                ++counter;
            }
            return false;
        }
    }
}
