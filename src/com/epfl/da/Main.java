package com.epfl.da;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Process;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        SignalHandler signalHandler = new SignalHandler() {
            @Override
            public void handle(Signal sig) {
                System.out.println("Abort");
            }
        };


        DiagnosticSignalHandler.install("TERM", signalHandler);
        DiagnosticSignalHandler.install("INT", signalHandler);
        DiagnosticSignalHandler.install("USR1", signalHandler);

        System.out.println("Started");
        int i = 0;
        while (true) {
            ++ i;
            if( i == 4 ) {
                Signal.raise(new Signal("INT"));
            }
            Thread.sleep(1000);
        }
    }
}
