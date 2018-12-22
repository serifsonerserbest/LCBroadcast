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
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Da_proc {

    private static void TestSendPL(PerfectLink perfectLink) throws UnknownHostException {
        for (int x = 0; x < 100; x++) {
            perfectLink.Send(x, InetAddress.getByName("127.0.0.1"), 20002);
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

    public static void main(String[] args) throws InterruptedException, IOException {


        //SYSTEM INPUTS
        int processId;
        String membershipFileName;
        int amountToSend;

        if (ApplicationSettings.getInstance().isDebug) {
            processId = 5;
            membershipFileName = "membership.txt";
            amountToSend = 10;
        } else {
            processId = Integer.parseInt(args[0]);
            membershipFileName = args[1];
            amountToSend = Integer.parseInt(args[2]);
        }

        System.out.println("Process " + processId + " started" );

        // PROCESS MEMBERSHIP FILE
        Process.getInstance().Init(processId, membershipFileName, amountToSend);


        if (ApplicationSettings.getInstance().isDebug) {

            final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(0);
            executor.schedule(() -> {
                for (int i = 1; i <= amountToSend; i++) {
                    LocalCausalBroadcast.getInst().Broadcast(i);
                }
            }, 20, TimeUnit.SECONDS);
            executor.schedule(() -> {
                try {
                    LocalCausalBroadcast.getInst().PrintVC();
                    LocalCausalBroadcast.getInst().PrintPending();
                    System.out.println("Creating Log File");
                    Process.getInstance().Logger.WriteLogToFile();
                    System.out.println("Log File created");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 40, TimeUnit.SECONDS);
        }


        System.out.println("Process " + processId + " listener started" );
        // INITIALIZE LISTENER
        Listener l = new Listener();

        try {
                l.Start();
        } catch (IOException e) {
                e.printStackTrace();
        }

    }
}
