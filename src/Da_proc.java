import AppSettings.ApplicationSettings;
import BestEffordBroadcast.BestEffortBroadcast;
import FIFOBroadcast.FIFOBroadcast;
import Listener.Listener;
import LocalCausalBroadcast.LocalCausalBroadcast;
import PerfectLink.PerfectLink;
import Process.Process;
import UniformReliableBroadcast.UniformReliableBroadcast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

public class Da_proc {

    private static void TestSendPL(PerfectLink perfectLink) throws UnknownHostException {
        for (int x = 0; x < 1000; x++) {
            perfectLink.Send(x, InetAddress.getByName("127.0.0.1"), 20001);
        }
    }

    private static void TestSendBE(BestEffortBroadcast bestEffortBroadcast) throws UnknownHostException {
        for (int x = 0; x < 1000; x++) {
            bestEffortBroadcast.Broadcast(1);
        }
    }

    private static void TestSendUR(UniformReliableBroadcast uniformReliableBroadcast) throws UnknownHostException {
        for (int x = 0; x < 1000; x++) {
            uniformReliableBroadcast.Broadcast(1);
        }
    }

    private static void TestSendFIFO(FIFOBroadcast fifoBroadcast) throws UnknownHostException {
        for (int x = 0; x < 1000; x++) {
            fifoBroadcast.Broadcast(1);
        }
    }

    private static void TestSendLocalCausal(LocalCausalBroadcast localCausalBroadcast) throws UnknownHostException {
        for (int x = 0; x < 500; x++) {
            localCausalBroadcast.Broadcast(1);
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        //SYSTEM INPUTS
        int processId;
        String membershipFileName;
        int amountToSend;

        if (ApplicationSettings.getInstance().isDebug) {
            processId = 5;
            membershipFileName = "membership.txt";
            amountToSend = 0;
        } else {
            processId = Integer.parseInt(args[0]);
            membershipFileName = args[1];
            amountToSend = Integer.parseInt(args[2]);
        }

        // PROCESS MEMBERSHIP FILE
        Process.getInstance().Init(processId, membershipFileName, amountToSend);

        System.out.print("PROCESS NAME: " + Process.getInstance().Id);
        System.out.println("");

        // INITIALIZE PROTOCOLS
        PerfectLink perfectLink = new PerfectLink();
        BestEffortBroadcast bestEffortBroadcast = new BestEffortBroadcast();
        UniformReliableBroadcast uniformReliableBroadcast = new UniformReliableBroadcast();
        FIFOBroadcast fifoBroadcast = FIFOBroadcast.getInst();
        LocalCausalBroadcast localCausalBroadcast = LocalCausalBroadcast.getInst();


        // INITIALIZE LISTENER

        new Thread(() -> {
            Listener l = new Listener(perfectLink, bestEffortBroadcast, uniformReliableBroadcast, fifoBroadcast, localCausalBroadcast);

            try {
                l.Start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        if (ApplicationSettings.getInstance().isDebug) {

            // TEST PROTOCOL
            Thread.sleep(40000);
            //TestSendPL(perfectLink);
            //TestSendBE(bestEffortBroadcast);
            //TestSendUR(uniformReliableBroadcast);
            //TestSendFIFO(fifoBroadcast);

            TestSendLocalCausal(localCausalBroadcast);

            Thread.sleep(100000);
            LocalCausalBroadcast.getInst().PrintVC();
            LocalCausalBroadcast.getInst().PrintPending();
            System.out.println("Creating Log File");
            Process.getInstance().Logger.WriteLogToFile();
            System.out.println("Log File created");
        }

        while (true) {
            Thread.sleep(10000);
        }
    }
}
