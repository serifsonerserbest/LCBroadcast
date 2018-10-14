package com.epfl.da;

import com.epfl.da.BestEffordBroadcast.BestEffortBroadcast;
import com.epfl.da.PerfectLink.PerfectLink;
import com.epfl.da.UniformReliableBroadcast.UniformReliableBroadcast;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;


public class Main {

    private static void TestReceivePL() throws IOException {
        PerfectLink server = new PerfectLink();
        //server.Deliver(20002);
    }

    private static void TestSendPL() throws UnknownHostException {
        PerfectLink client = new PerfectLink();
        for (int x = 1; x <= 1000; x++){
            client.Send(x, InetAddress.getByName("127.0.0.1"), 20002);
        }
    }


    private static void TestSendBE() throws UnknownHostException {
        BestEffortBroadcast client = new BestEffortBroadcast();

        int[] ports = {20001,20002,20003};
        InetAddress[] addresses = {InetAddress.getByName("127.0.0.1"),InetAddress.getByName("127.0.0.1"),InetAddress.getByName("127.0.0.1")};
        //client.Broadcast(1, addresses, ports);
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        ArrayList<InetSocketAddress> adr = new  ArrayList<InetSocketAddress>();
        adr.add(new InetSocketAddress(Inet4Address.getByName("127.0.0.1"),20000));
        Process.getInstance().Init(3, "membership.txt");

        /*long startTime = System.currentTimeMillis();

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                long stopTime = System.currentTimeMillis();
                long elapsedTime = stopTime - startTime;
                System.out.println(elapsedTime);
            }
        });*/




        new Thread(()->{
            Listener l = new Listener();
            l.onMessageReceive = (x)->{System.out.println("Main handler message content" + x);};
            try {
                l.Start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        Thread.sleep(1000);
        UniformReliableBroadcast u = new UniformReliableBroadcast();
        u.Broadcast(20);

        while(true){
            Thread.sleep(10000);
        }
       /* Thread.sleep(30000);
        UniformReliableBroadcast u = new UniformReliableBroadcast();
        u.Broadcast(20);*/

       /* PerfectLink client = new PerfectLink();
        for (int x = 0; x < 40000; x++){
            client.Send(x, InetAddress.getByName("127.0.0.1"), 20000);
        }*/

        //TestReceivePL();
        //TestSendPL();
        //BestEffortBroadcast server1 = new BestEffortBroadcast();
        //server1.Deliver(20001);
        //BestEffortBroadcast server2 = new BestEffortBroadcast();
        //server2.Deliver(20002);
        //BestEffortBroadcast server3 = new BestEffortBroadcast();
        //server3.Deliver(20003);

        //TestSendBE();


    }
}
