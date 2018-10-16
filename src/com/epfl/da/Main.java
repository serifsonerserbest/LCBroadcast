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

    private static void TestSendPL() throws UnknownHostException {
        PerfectLink client = new PerfectLink();
        for (int x = 0; x < 2; x++){
            client.Send(x, InetAddress.getByName("127.0.0.1"), 20000);
        }
    }

    private static void TestSendBE() throws UnknownHostException {
        BestEffortBroadcast client = new BestEffortBroadcast();
        client.Broadcast(1);
    }

    private static void TestSendUR() throws UnknownHostException {
        UniformReliableBroadcast u = new UniformReliableBroadcast();
        u.Broadcast(1);
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        ArrayList<InetSocketAddress> adr = new  ArrayList<InetSocketAddress>();
        //adr.add(new InetSocketAddress(Inet4Address.getByName("127.0.0.1"),20003));

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

        //TestSendPL();
        TestSendBE();
        //TestSendUR();

        while(true){
            Thread.sleep(10000);
        }


    }
}
