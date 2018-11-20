package LocalCausalBroadcast;

import UniformReliableBroadcast.UniformReliableBroadcast;
import Process.Process;

import java.util.LinkedList;

public class LocalCausalBroadcast {

    private UniformReliableBroadcast uniformReliableBroadcast;
    private int[] vectorClock;
    private LinkedList<Integer>[] pending; // Keeps vector clocks according to process id
    private boolean[] dependencies;

    public LocalCausalBroadcast(){
        uniformReliableBroadcast = new UniformReliableBroadcast();
        vectorClock = new int[Process.getInstance().processes.size()];
        pending = new LinkedList[Process.getInstance().processes.size()];
        dependencies = Process.getInstance().dependencies;
    }

    public synchronized void Broadcast(int content){
        //System.out.println("d " + Process.getInstance().Id + " " + vectorClock[Process.getInstance().Id]);
        Process.getInstance().Logger.WriteToLog("d " + Process.getInstance().Id + " " + vectorClock[Process.getInstance().Id]);

        uniformReliableBroadcast.Broadcast(content, vectorClock);
        ++vectorClock[Process.getInstance().Id];
    }

    public synchronized void Deliver(){


    }

    public synchronized void DeliverOrPend(){


    }



}
