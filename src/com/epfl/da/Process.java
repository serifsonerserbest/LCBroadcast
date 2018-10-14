package com.epfl.da;

import com.epfl.da.Models.ProcessModel;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class Process {


    private static final Process process = new Process();
    public int Id;
    public Logger Logger;
    // todo change to hashset
    public ArrayList<ProcessModel> processes = new ArrayList<ProcessModel>();

    private Process() {}

    public static Process getInstance() {
        return process;
    }


    public void Init(int id, String settingFileNAme)
    {
        Id = id;
        Logger = new Logger(Id);
        ReadSettingFile(settingFileNAme);
        SetupSignalHandlers();
    }

    public ProcessModel GetProcessById(int id){
        for (int i = 0; i < processes.size(); i++) {
            var currentProcess = processes.get(i);
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
        //DiagnosticSignalHandler.install("USR1", GetUsr1Handler());
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
            processes.add(new ProcessModel(Integer.parseInt(splitted[0]), InetAddress.getByName(splitted[1]), Integer.parseInt(splitted[2])));
        }
        } catch (Exception e) {
            System.out.println("Exception while parsing file:" + e);
        }
    }
    //endregion

    //region Signal Handlers
    private SignalHandler GetTermHandler()
    {
         return sig -> {
             System.out.println("Term");
             Logger.WriteLogToFile();
             System.exit(-1);
         };
    }
    private SignalHandler GetIntHandler()
    {
        return sig -> {
            System.out.println("Int");
            Logger.WriteLogToFile();
            System.exit(-1);
        };
    }
    private SignalHandler GetUsr1Handler()
    {
        return sig -> {
            System.out.println("Usr1");
        };
    }
    //endregion
}
