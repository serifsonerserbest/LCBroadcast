package Process;

import AppSettings.ApplicationSettings;
import FIFOBroadcast.FIFOBroadcast;
import Models.ProcessModel;

import sun.misc.SignalHandler;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetAddress;
import java.util.ArrayList;
import Logger.Logger;
import SignalHandler.DiagnosticSignalHandler;

public class Process {

    private volatile static Process process = new Process();
    int amountMessageToSend;

    public int Id;
    public int Port;
    public Logger Logger;
    public ArrayList<ProcessModel> processes = new ArrayList<ProcessModel>();

    private Process() {}

    public static Process getInstance() {
        return process;
    }

    public void Init(int id, String membershipFileName, int amountMessageToSend) {

        Id = id;
        this.amountMessageToSend = amountMessageToSend;
        Logger = new Logger(Id);
        this.amountMessageToSend = amountMessageToSend;
        ReadSettingFile(membershipFileName);
        if(!ApplicationSettings.getInstance().isDebug) {
            SetupSignalHandlers();
        }
    }

    public ProcessModel GetProcessById(int id){

        for (int i = 0; i < processes.size(); i++) {
            ProcessModel currentProcess = processes.get(i);
            if(currentProcess.id == id) {
                return currentProcess;
            }
        }
        return null;
    }

    //region Private Methods
    private void SetupSignalHandlers(){

       DiagnosticSignalHandler.install("TERM", GetTermHandler());
       DiagnosticSignalHandler.install("INT", GetIntHandler());
       DiagnosticSignalHandler.install("USR2", GetUsr1Handler());
    }

    private void ReadSettingFile(String settingFileName){

        BufferedReader buff = null;
        try {
            buff = new BufferedReader(new FileReader(settingFileName));

            String num = buff.readLine();
            int processNum = Integer.parseInt(num);
            for(int i = 0; i < processNum; i++)
            {
                String process = buff.readLine();
                String [] splitted = process.split("\\s+");
                if(Integer.parseInt(splitted[0]) == Id)
                {
                    Port = Integer.parseInt(splitted[2]);
                }

                processes.add(new ProcessModel(Integer.parseInt(splitted[0]), InetAddress.getByName(splitted[1]), Integer.parseInt(splitted[2])));
            }
        } catch (Exception e) {
            System.out.println("Exception while parsing file:" + e);
        }
    }
    //endregion

    //region Signal Handlers
    private SignalHandler GetTermHandler() {

         return sig -> {
             System.out.println("TERM");
             Logger.WriteLogToFile();
             System.exit(-1);
         };
    }

    private SignalHandler GetIntHandler() {

        return sig -> {
            System.out.println("INT");
            Logger.WriteLogToFile();
            System.exit(-1);
        };
    }

    private SignalHandler GetUsr1Handler() {

        return sig -> {
            System.out.println("USR2");
            for(int i = 1; i <= amountMessageToSend; i ++) {
                FIFOBroadcast.getInst().Broadcast(i);
            }
        };
    }
    //endregion
}
