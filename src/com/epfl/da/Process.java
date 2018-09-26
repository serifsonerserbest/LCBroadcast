package com.epfl.da;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class Process {
    public final int Id;
    public final Logger Logger;
    public Process(int id)
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
         };
    }
    private SignalHandler GetIntHandler()
    {
        return sig -> {
            System.out.println("Int");
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
