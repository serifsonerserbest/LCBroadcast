package com.epfl.da.PerfectLink;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class DeliverEvent {

    public synchronized void sendAck(int port, InetAddress address, int messageId) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        int[] data = {messageId};
        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(data);
        byte[] sentData = byteBuffer.array();
        socket.send(new DatagramPacket(sentData, sentData.length, address, port));
        socket.close();
    }
}
