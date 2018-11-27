package PerfectLink;

import AppSettings.ApplicationSettings;
import Enums.ProtocolTypeEnum;
import Process.Process;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class SendEvent {

    public volatile static AtomicInteger messageId ;

    public static volatile HashMap<InetSocketAddress, ThreadPoolExecutor> threadPoolLst = new HashMap<>();

    //volatile ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool( ApplicationSettings.getInstance().SenderThreadPoolSize);

    public SendEvent() {
        messageId = new AtomicInteger(0);
    }

    public static int NextId() {
        return messageId.incrementAndGet();
    }

    public void SendMessage(int content, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol, int originalProcessId, int originalMessageId, int messageId, int fifoId, int[] vectorClock) {

        InetSocketAddress key = new InetSocketAddress(destAddress, destPort);
        if(!threadPoolLst.containsKey(key))
        {
            ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool( ApplicationSettings.getInstance().SenderThreadPoolSize);
            threadPool.setCorePoolSize(1);
            threadPool.setKeepAliveTime(200, TimeUnit.MILLISECONDS);
            /*threadPool.setRejectedExecutionHandler((r, executor) -> {
                System.out.println("sssss");
            });*/
            threadPoolLst.put(key, threadPool);
            //threadPool.setKeepAliveTime(5, TimeUnit.MINUTES);
        }

        ThreadSend thread = new ThreadSend(destPort, destAddress, content, messageId, protocol, originalProcessId, originalMessageId, fifoId, vectorClock);
        if(Process.getInstance().IsRunning) {
            threadPoolLst.get(key).submit(thread);
        }
        else {
            threadPoolLst.get(key).getQueue().offer(thread);
        }
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
        int[] vectorClock;

        // ThreadSend constructor
        public ThreadSend(int destPort, InetAddress destAddress, int content, int messageId, ProtocolTypeEnum protocol, int originalProcessId, int originalMessageId, int fifoId, int[] vectorClock) {
            this.destPort = destPort;
            this.destAddress = destAddress;
            this.content = content;
            this.messageId = messageId;
            this.protocol = protocol;
            this.originalMessageId = originalMessageId;
            this.originalProcessId = originalProcessId;
            this.fifoId = fifoId;
            this.setName("Send Thread " + messageId);
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
            DatagramSocket socketOut = null;
            try {
                socketOut = Process.getInstance().GetSocketFromQueue();
                //System.out.println("s " + messageId + " port " + socketOut.getLocalPort());

                socketOut.setSoTimeout(ApplicationSettings.getInstance().timeoutVal);
                boolean isMessageSent = SendMessage(socketOut, sendingPacket, receivePacket, -1);


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
                   // System.out.println("Ack receive id: " + messageId + " expected :" + this.messageId + " port " + socketOut.getLocalPort());
                    if (this.messageId == messageId) {
                        threadPoolLst.computeIfPresent(new InetSocketAddress(destAddress, destPort), (x, y) ->{
                            if(y.getCorePoolSize() < ApplicationSettings.getInstance().SenderThreadPoolSize)
                            {
                               // y.setCorePoolSize(y.getCorePoolSize() + 1);
                            }
                            return y;
                        });
                        return true;
                    }
                } catch (SocketTimeoutException e) {
                    threadPoolLst.computeIfPresent(new InetSocketAddress(destAddress, destPort), (x, y) ->{
                       // y.submit(new ThreadSend(destPort, destAddress, content, messageId, protocol, originalProcessId, originalMessageId, fifoId));
                        if(y.getCorePoolSize() > 1)
                        {
                           // y.setCorePoolSize(y.getCorePoolSize() - 1);
                        }
                        return y;
                    });
                    System.out.println("Timeout reached: From Process" + Process.getInstance().Id + " to: " + destPort  + " MessageId:" + messageId + e);
                }
                ++counter;
            }
            return false;
        }
    }
}
