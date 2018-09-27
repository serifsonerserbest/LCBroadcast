package com.epfl.da;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class Receiver {


    HashMap<Integer, Integer> receivedMessage;
    Semaphore s;

    public Receiver() {
        s = new Semaphore(1);
        receivedMessage = new HashMap<>();
    }
    public void ReceiveMessage(int port) throws IOException, InterruptedException {
        DatagramSocket socket = new DatagramSocket(port);
        boolean lastMessageFlag = false;
        boolean lastMessage = false;
        byte[] message = new byte[1024];
        InetAddress address;
        while(!lastMessage) {
            s.acquire();
            DatagramPacket receivedPacket = new DatagramPacket(message, message.length);
            socket.receive(receivedPacket);
            message = receivedPacket.getData();

            address = receivedPacket.getAddress();
            port = receivedPacket.getPort();

        }

    }
    /*private class InThread extends Thread {
        private DatagramSocket sk_in;
        public void run () {

        }
    }
    private class OutThread extends Thread {

    }*/
}
