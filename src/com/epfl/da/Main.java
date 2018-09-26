package com.epfl.da;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.InputStream;
import java.io.OutputStream;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Process process = new Process(1);

        int i = 0;
        while (true) {
            ++i;
            if (i == 4) {
                Signal.raise(new Signal("INT"));
            }
            Thread.sleep(1000);
        }
    }
}
