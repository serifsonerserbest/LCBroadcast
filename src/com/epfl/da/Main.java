package com.epfl.da;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;



public class Main {

    private static void TestReceivePL() throws IOException {
        PerfectLink server = new PerfectLink();
        server.Deliver(20002);
    }

    private static void TestSendPL() throws UnknownHostException {
        PerfectLink client = new PerfectLink();
        for (int x = 1; x <= 1000; x++){
            client.Send(x, InetAddress.getByName("127.0.0.1"), 20002);
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        Process process = new Process(1);

        //TestReceivePL();
        //TestSendPL();
    }
}
