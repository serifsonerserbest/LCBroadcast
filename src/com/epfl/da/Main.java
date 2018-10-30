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

    private static void TestSendPL(PerfectLink perfectLink) throws UnknownHostException {
        for (int x = 0; x < 10; x++){
            perfectLink.Send(x, InetAddress.getByName("127.0.0.1"), 20001);
        }
    }

    private static void TestSendBE(BestEffortBroadcast bestEffortBroadcast) throws UnknownHostException {
        bestEffortBroadcast.Broadcast(10);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bestEffortBroadcast.Broadcast(10);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bestEffortBroadcast.Broadcast(10);
    }

    private static void TestSendUR(UniformReliableBroadcast uniformReliableBroadcast) throws UnknownHostException {
        uniformReliableBroadcast.Broadcast(1);
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        int processId = 3;
        Process.getInstance().Init(processId, "membership.txt");


        PerfectLink perfectLink = new PerfectLink();
        BestEffortBroadcast bestEffortBroadcast = new BestEffortBroadcast();
        UniformReliableBroadcast uniformReliableBroadcast = UniformReliableBroadcast.getInst();

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
            Listener l = new Listener(perfectLink, bestEffortBroadcast, uniformReliableBroadcast);
            l.onMessageReceive = (x)->{System.out.println("Main handler message content" + x);};
            try {
                l.Start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        Thread.sleep(20000);

        //TestSendPL(perfectLink);
        //TestSendBE(bestEffortBroadcast);
        TestSendUR(uniformReliableBroadcast);

        while(true){
            Thread.sleep(1000);
        }


    }



}
