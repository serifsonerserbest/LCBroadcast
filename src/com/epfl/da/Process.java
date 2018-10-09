package com.epfl.da;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class Process {


    private static final Process process = new Process();
    public int Id;
    public Logger Logger;

    private Process() {}

    public static Process getInstance() {
        return process;
    }


    public void Init(int id, ArrayList<InetSocketAddress> addresses)
    {
        Id = id;
        Logger = new Logger(Id);
        SetupSignalHandlers();
    }

    //region Private Methods
    private void SetupSignalHandlers(){
        DiagnosticSignalHandler.install("TERM", GetTermHandler());
        DiagnosticSignalHandler.install("INT", GetIntHandler());
        //DiagnosticSignalHandler.install("USR1", GetUsr1Handler());
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
