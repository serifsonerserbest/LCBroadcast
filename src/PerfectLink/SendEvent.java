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
import java.util.LinkedHashMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class SendEvent {

    class MessageWrapper{
       public int destPort;
       public InetAddress destAddress;
       public int content;
       public int messageId;
       public ProtocolTypeEnum protocol;
       public int originalProcessId;
       public int originalMessageId;
       public int fifoId;
       public int[] vectorClock;

       public MessageWrapper(int destPort, InetAddress destAddress, int content, int messageId, ProtocolTypeEnum protocol, int originalProcessId, int originalMessageId, int fifoId, int[] vectorClock)
       {
            this.destPort = destPort;
            this.destAddress = destAddress;
            this.content = content;
            this.messageId = messageId;
            this.protocol = protocol;
            this.originalProcessId = originalProcessId;
            this.originalMessageId = originalMessageId;
            this.fifoId = fifoId;
            this.vectorClock = vectorClock;
       }
    }


    public volatile static AtomicInteger messageId ;

    public static volatile LinkedHashMap<InetSocketAddress, ConcurrentLinkedDeque<MessageWrapper>> messageBuckets= new LinkedHashMap<>();

    public volatile static AtomicInteger currentBucketToSend;

    public static ThreadPoolExecutor threadPool = (ThreadPoolExecutor)Executors.newFixedThreadPool(ApplicationSettings.getInstance().SenderThreadPoolSize);

    //volatile ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool( ApplicationSettings.getInstance().SenderThreadPoolSize);

    public SendEvent() {
        messageId = new AtomicInteger(0);
        currentBucketToSend = new AtomicInteger(0);
        for(int i = 0; i< ApplicationSettings.getInstance().SenderThreadPoolSize; i ++)
        {
            threadPool.getQueue().add(new SenderThread());
        }
    }

    public static int NextId() {
        return messageId.incrementAndGet();
    }

    public void SendMessage(int content, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol, int originalProcessId, int originalMessageId, int messageId, int fifoId, int[] vectorClock) {

        InetSocketAddress key = new InetSocketAddress(destAddress, destPort);
        if(!messageBuckets.containsKey(key))
        {
            ConcurrentLinkedDeque<MessageWrapper> dequeu = new ConcurrentLinkedDeque<>();
            messageBuckets.put(key, dequeu);
            //threadPool.setKeepAliveTime(5, TimeUnit.MINUTES);
        }

        MessageWrapper message = new MessageWrapper(destPort, destAddress, content, messageId, protocol, originalProcessId, originalMessageId, fifoId, vectorClock);
        messageBuckets.get(key).add(message);
    }

    private class SenderThread extends Thread{
        public void run() {
           while(true)
           {
               int currentBucket = currentBucketToSend.getAndUpdate(x -> x >= messageBuckets.size() - 1 ? 0 : currentBucketToSend.get() + 1);
               MessageWrapper message = ((ConcurrentLinkedDeque<MessageWrapper>)messageBuckets.values().toArray()[currentBucket]).poll();
               if(message == null)
                {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                Send s = new Send(message);
                s.run();
           }
        }
    }



    private class Send {

        MessageWrapper message;

        // ThreadSend constructor
        public Send(MessageWrapper message) {
            this.message = message;
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

            int[] data = {message.messageId,
                    message.protocol.ordinal(),
                    message.content,
                    Process.getInstance().Id,
                    message.originalProcessId,
                    message.originalMessageId,
                    message.fifoId};

            if (message.vectorClock != null){
                data = concatenate(data, message.vectorClock);
            }

            ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            intBuffer.put(data);
            byte[] out_data = byteBuffer.array();

            DatagramPacket sendingPacket = new DatagramPacket(out_data, out_data.length, message.destAddress, message.destPort);
            DatagramPacket receivePacket = new DatagramPacket(in_data, in_data.length);
            DatagramSocket socketOut = null;
            try {
                socketOut = Process.getInstance().GetSocketFromQueue();
                //System.out.println("s " + messageId + " port " + socketOut.getLocalPort());

                socketOut.setSoTimeout(ApplicationSettings.getInstance().timeoutVal);
                boolean isMessageSent = SendMessage(socketOut, sendingPacket, receivePacket, 3);


                if(!isMessageSent)
                {
                    InetSocketAddress key = new InetSocketAddress(message.destAddress, message.destPort);
                    messageBuckets.get(key).addFirst(message);
                }

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
                    if (message.messageId == messageId) {
                        return true;
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Timeout reached: From Process" + Process.getInstance().Id + " to: " + message.destPort  + " MessageId:" + messageId + e);
                }
                ++counter;
            }
            return false;
        }
    }
}
