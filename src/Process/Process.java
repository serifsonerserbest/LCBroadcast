package Process;

import AppSettings.ApplicationSettings;
import FIFOBroadcast.FIFOBroadcast;
import LocalCausalBroadcast.LocalCausalBroadcast;
import Models.ProcessModel;

import PerfectLink.SendEvent;
import sun.misc.SignalHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import Logger.Logger;
import SignalHandler.DiagnosticSignalHandler;
import sun.nio.ch.ThreadPool;

public class Process {

    private volatile static Process process = new Process();

    private volatile ConcurrentLinkedQueue<DatagramSocket> socketQueue = new ConcurrentLinkedQueue <>();

    int amountMessageToSend;

    public int Id;
    public int Port;
    public Logger Logger;
    public ArrayList<ProcessModel> processes = new ArrayList<ProcessModel>();
    public boolean[] dependencies;

    private Process() {
    }

    public static Process getInstance() {
        return process;
    }

    public void Init(int id, String membershipFileName, int amountMessageToSend) {

        Id = id;
        this.amountMessageToSend = amountMessageToSend;
        Logger = new Logger(Id);
        this.amountMessageToSend = amountMessageToSend;
        ReadSettingFile(membershipFileName);
        if (!ApplicationSettings.getInstance().isDebug) {
            SetupSignalHandlers();
        }
    }

    public ProcessModel GetProcessById(int id) {

        for (int i = 0; i < processes.size(); i++) {
            ProcessModel currentProcess = processes.get(i);
            if (currentProcess.id == id) {
                return currentProcess;
            }
        }
        return null;
    }

    public DatagramSocket GetSocketFromQueue()
    {
        DatagramSocket socket = socketQueue.poll();

        if (socket == null) {
            try {
                socket = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        return socket;
    }

    public void PutSocketToQuery(DatagramSocket socket)
    {
        socketQueue.add(socket);
    }
    //region Private Methods
    private void SetupSignalHandlers() {

        DiagnosticSignalHandler.install("TERM", GetTermHandler());
        DiagnosticSignalHandler.install("INT", GetIntHandler());
        DiagnosticSignalHandler.install("USR2", GetUsr2Handler());
    }

    private void ReadSettingFile(String settingFileName) {

        BufferedReader buff = null;
        try {
            buff = new BufferedReader(new FileReader(settingFileName));

            String num = buff.readLine();
            int numOfProcesses = Integer.parseInt(num);
            for (int i = 0; i < numOfProcesses; i++) {
                String process = buff.readLine();
                String[] splitted = process.split("\\s+");
                if (Integer.parseInt(splitted[0]) == Id) {
                    Port = Integer.parseInt(splitted[2]);
                }

                processes.add(new ProcessModel(Integer.parseInt(splitted[0]), InetAddress.getByName(splitted[1]), Integer.parseInt(splitted[2])));
            }
            dependencies = new boolean[numOfProcesses + 1];

            for (int i = 0; i < numOfProcesses; i++) {
                String process = buff.readLine();
                String[] splitted = process.split("\\s+");
                if (Integer.parseInt(splitted[0]) == Id){
                    for(int j =1; j < splitted.length; j++ ){
                        int processId = Integer.parseInt(splitted[j]);
                        dependencies[processId] = true;
                    }
                }
            }
            dependencies[Id] = true;
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

    private SignalHandler GetUsr2Handler() {

        return sig -> {
            System.out.println("USR2");
            for (int i = 1; i <= amountMessageToSend; i++) {
                LocalCausalBroadcast.getInst().Broadcast(i);
            }
        };
    }
    //endregion
}
