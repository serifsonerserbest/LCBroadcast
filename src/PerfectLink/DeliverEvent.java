package PerfectLink;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import Process.Process;

public class DeliverEvent {

    public synchronized void sendAck(int port, InetAddress address, int messageId) {

        DatagramSocket socket = null;
        try {

            socket = Process.getInstance().GetSocketFromQueue();

            int[] data = {messageId};
            ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            intBuffer.put(data);
            byte[] sentData = byteBuffer.array();
            //System.out.println("Ack Send Id: " + messageId);
            socket.send(new DatagramPacket(sentData, sentData.length, address, port));
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            Process.getInstance().PutSocketToQuery(socket);
        }
    }
}
