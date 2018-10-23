package com.epfl.da;

import com.epfl.da.Models.ProcessModel;
import com.epfl.da.UniformReliableBroadcast.UniformReliableBroadcast;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Process {

    private volatile static Process process = new Process();
    public int Id;
    public int Port;
    public boolean Crashed = false;

    public Logger Logger;
    // todo change to hashset
    public ArrayList<ProcessModel> processes = new ArrayList<ProcessModel>();

    private Process() {}

    public static Process getInstance() {
        return process;
    }

    public void Init(int id, String membership)
    {
        Id = id;
        Logger = new Logger(Id);
        ReadSettingFile(membership);
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
        DiagnosticSignalHandler.install("STOP", GetStopHandler());
        DiagnosticSignalHandler.install("CONT", GetContHandler());
        DiagnosticSignalHandler.install("TERM", GetTermHandler());
        DiagnosticSignalHandler.install("INT", GetIntHandler());
        DiagnosticSignalHandler.install("USR1", GetUsr1Handler());
    }

    private void ReadSettingFile(String membership) {

        var splittedMem = membership.split("\n");
        int processNum = Integer.parseInt(splittedMem[0]);
        for (int i = 0; i < processNum; i++) {
            String process = splittedMem[i + 1];
            String[] splitted = process.split("\\s+");
            if (Integer.parseInt(splitted[0]) == Id) {
                Port = Integer.parseInt(splitted[2]);
            }
            try {
                processes.add(new ProcessModel(Integer.parseInt(splitted[0]), InetAddress.getByName(splitted[1]), Integer.parseInt(splitted[2])));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

        }
    }
    //endregion

    //region Signal Handlers
    private SignalHandler GetStopHandler()
    {
        return sig -> {
            System.out.println("STOP");
            Crashed = true;
        };
    }

    private SignalHandler GetContHandler()
    {
        return sig -> {
            System.out.println("CONT");
            Crashed = false;
        };
    }

    private SignalHandler GetTermHandler()
    {
         return sig -> {
             System.out.println("TERM");
             Logger.WriteLogToFile();
             System.exit(-1);
         };
    }

    private SignalHandler GetIntHandler()
    {
        return sig -> {
            System.out.println("INT");
            Logger.WriteLogToFile();
            System.exit(-1);
        };
    }
    private SignalHandler GetUsr1Handler()
    {
        return sig -> {
            System.out.println("USR1");
            UniformReliableBroadcast.getInst().Broadcast(1);
        };
    }
    //endregion
}
