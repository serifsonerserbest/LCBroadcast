package LocalCausalBroadcast;

import Models.MessageModel;
import UniformReliableBroadcast.UniformReliableBroadcast;
import Process.Process;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class LocalCausalBroadcast {

    private volatile static LocalCausalBroadcast localCausalBroadcast = new LocalCausalBroadcast();

    private UniformReliableBroadcast uniformReliableBroadcast;
    private  ThreadSafeArray vectorClock;

    private LinkedList<int[]>[] pending; // Keeps vector clocks according to process id
    private boolean[] dependencyMask;

    private LocalCausalBroadcast(){
        int numOfProcesses = Process.getInstance().processes.size();

        uniformReliableBroadcast = new UniformReliableBroadcast();
        vectorClock = new ThreadSafeArray(numOfProcesses + 1);
        pending = new LinkedList[numOfProcesses + 1];
        dependencyMask = Process.getInstance().dependencies;

        for (int i = 0; i <= numOfProcesses; i++){
            vectorClock.IncOrCopy(true, i);
            pending[i] = new LinkedList<>();
        }
    }

    public static LocalCausalBroadcast getInst() {

        return localCausalBroadcast;
    }

    public synchronized void PrintVC(){
        int[] copy = vectorClock.IncOrCopy(false, 0);
        for (int i = 0; i < vectorClock.Length(); i++) {
            if (i > 0) {
                System.out.print(", ");
            }
            System.out.print(copy[i]);
        }
        System.out.println("");
    }

    private synchronized boolean isEqual(ThreadSafeArray real, int[]copy){
        for (int i = 0; i < real.Length(); i++){
            if(real.Get(i) != copy[i])
                return false;
        }
        return true;
    }

    public synchronized  void PrintPending(){
        int numOfProcesses = Process.getInstance().processes.size();
        System.out.println("");
        System.out.println("PENDING:");
        System.out.println("");
        for (int i = 0; i <numOfProcesses + 1; i++) {
            if (i > 0) {
                System.out.print(", ");
            }
            System.out.print(pending[i].size());
        }
        System.out.println("");
    }

    public synchronized void Broadcast(int content){

        // Create new vector clock to only send dependencies
        // TODO: Change it with elementwise multiplication of two matrices
        int numOfProcesses = Process.getInstance().processes.size();
        int[] VCBroadcast = vectorClock.IncOrCopy(false, 0);

        if(!isEqual(vectorClock, VCBroadcast))
            System.out.println("VECTOR CLOCKS ARE NOT THE SAME !!");
        //System.out.println("b " + Process.getInstance().Id + " " + vectorClock.Get(Process.getInstance().Id));
        //PrintVC();

        Process.getInstance().Logger.WriteToLog("b " + Process.getInstance().Id);

        uniformReliableBroadcast.Broadcast(content, VCBroadcast);
        //vectorClock.incrementAndGet(Process.getInstance().Id);
        vectorClock.IncOrCopy(true, Process.getInstance().Id);

        //TODO: DELIVER CURRENT MESSAGE (NEED TO CALL DELIVER PENDING SINCE VC IS CHANGING)
    }

    public synchronized void Deliver(MessageModel message, MessageModel originalMessage, int content, int portReceived, InetAddress addressReceived, int[] vectorClock) throws IOException {
        if(uniformReliableBroadcast.Deliver(message, originalMessage, content, portReceived, addressReceived, vectorClock)){

            int originalProcessId = originalMessage.getProcessId();
            if(Process.getInstance().Id != originalProcessId){
                pending[originalProcessId].add(vectorClock);
                DeliverOrPend();
            }
        }
    }

    private synchronized void DeliverOrPend(){
        int numOfProcesses = Process.getInstance().processes.size();

        for (int pId = 1; pId <= numOfProcesses; pId++){

            //Check pending VCs for each process
            Iterator<int[]> it = pending[pId].iterator();
            while (it.hasNext()) {
                int[] copyVC = vectorClock.IncOrCopy(false, 0);
                int[] VCx = it.next();
                boolean deliver = true;

                //Compare VC with VCx to decide delivery action
                for (int j= 1; j <= numOfProcesses; j++){
                    if( copyVC[j] < VCx[j])
                        deliver = false;
                }
                if (deliver) {
                    // remove current message and initialize iterator
                    it.remove();
                    it = pending[pId].iterator();

                    System.out.println("d " + pId + " " + vectorClock.Get(pId));
                    PrintVC();
                    Process.getInstance().Logger.WriteToLog("d " + pId + " " + vectorClock.Get(pId));
                    //vectorClock.incrementAndGet(pId);
                    vectorClock.IncOrCopy(true, pId);
                }
            }
        }
    }
}
