import AppSettings.ApplicationSettings;
import FIFOBroadcast.FIFOBroadcast;
import Listener.Listener;
import Process.Process;

import java.io.IOException;
import java.net.UnknownHostException;

public class Da_proc {

    private static void TestSendFIFO(FIFOBroadcast fifoBroadcast) throws UnknownHostException {

        for (int x = 0; x < 10000; x++){
            fifoBroadcast.Broadcast(1);
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        // SYSTEM INPUTS
        int processId;
        String membershipFileName;
        int amountToSend;
        if(ApplicationSettings.getInstance().isDebug) {
            processId = 3;
            membershipFileName = "membership.txt";
            amountToSend = 0;
        }
        else {
            processId = Integer.parseInt(args[0]);
            membershipFileName = args[1];
            amountToSend = Integer.parseInt(args[2]);
        }

        // PROCESS MEMBERSHIP FILE
        Process.getInstance().Init(processId, membershipFileName, amountToSend);

        // INITIALIZE LISTENER
        FIFOBroadcast fifoBroadcast = FIFOBroadcast.getInst();
        new Thread(()->{
            Listener l = new Listener(fifoBroadcast);
            try {
                l.Start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        //TEST
        Thread.sleep(20000);
        TestSendFIFO(fifoBroadcast);

        Thread.sleep(100000);
        System.out.println("Creating Log File ...");
        Process.getInstance().Logger.WriteLogToFile();
        System.out.println("Log File created");

        while(true){
            Thread.sleep(1000);
        }
    }
}
