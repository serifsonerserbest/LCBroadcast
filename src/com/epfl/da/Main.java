package com.epfl.da;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Process process = new Process(1);
        /*Receiver r = new Receiver();
        try {
            r.ReceiveMessage(20002);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
       /* Sender s = new Sender();
        try {
            s.SendMessage(1, InetAddress.getByName("127.0.0.1"), 20002);
            s.SendMessage(2, InetAddress.getByName("127.0.0.1"), 20002);
            s.SendMessage(3, InetAddress.getByName("127.0.0.1"), 20002);
            s.SendMessage(4, InetAddress.getByName("127.0.0.1"), 20002);
            s.SendMessage(5, InetAddress.getByName("127.0.0.1"), 20002);
            s.SendMessage(6, InetAddress.getByName("127.0.0.1"), 20002);
            s.SendMessage(7, InetAddress.getByName("127.0.0.1"), 20002);
            s.SendMessage(8, InetAddress.getByName("127.0.0.1"), 20002);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }*/
    }
}
