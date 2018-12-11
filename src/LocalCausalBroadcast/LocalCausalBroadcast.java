package LocalCausalBroadcast;

import Models.MessageModel;
import UniformReliableBroadcast.UniformReliableBroadcast;
import Process.Process;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class LocalCausalBroadcast {

    private volatile static LocalCausalBroadcast localCausalBroadcast = new LocalCausalBroadcast();

    private UniformReliableBroadcast uniformReliableBroadcast;
    private CopyOnWriteArrayList<AtomicInteger> vectorClock;

    private ConcurrentLinkedQueue<int[]>[] pending; // Keeps vector clocks according to process id
    private boolean[] dependencyMask;
    private boolean[] copyMask;


    private LocalCausalBroadcast(){
        int numOfProcesses = Process.getInstance().processes.size();

        uniformReliableBroadcast = new UniformReliableBroadcast();
        vectorClock = new CopyOnWriteArrayList<AtomicInteger>();
        pending = new ConcurrentLinkedQueue[numOfProcesses + 1];
        dependencyMask = Process.getInstance().dependencies;
        copyMask = new boolean[numOfProcesses + 1];

        for (int i = 0; i <= numOfProcesses; i++){
            vectorClock.add(new AtomicInteger(1));
            pending[i] = new ConcurrentLinkedQueue<>();
            copyMask[i] = true;
        }

    }

    public static LocalCausalBroadcast getInst() {

        return localCausalBroadcast;
    }

    public int[] ArrayCopy(CopyOnWriteArrayList<AtomicInteger> array, boolean[] mask)
    {
        int[] copy = new int[array.size()];
        for(int i= 0; i < array.size(); i++){
            if (mask[i])
                copy[i] = array.get(i).get();
        }
        return copy;
    }

    public void PrintVC(){
        int[] copy = ArrayCopy(vectorClock, copyMask);
        for (int i = 0; i < vectorClock.size(); i++) {
            if (i > 0) {
                System.out.print(", ");
            }
            System.out.print(copy[i]);
        }
        System.out.println("");
    }

    private boolean isEqual(CopyOnWriteArrayList<AtomicInteger> real, int[]copy){
        for (int i = 0; i < real.size(); i++){
            if(real.get(i).get() != copy[i])
                return false;
        }
        return true;
    }

    public void PrintPending(){
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

    public void Broadcast(int content){

        // Create new vector clock to only send dependencies
        // TODO: Change it with elementwise multiplication of two matrices
        int numOfProcesses = Process.getInstance().processes.size();
        int[] VCBroadcast = ArrayCopy(vectorClock, dependencyMask);

       // if(!isEqual(vectorClock, VCBroadcast))
         //   System.out.println("VECTOR CLOCKS ARE NOT THE SAME !!");

        Process.getInstance().Logger.WriteToLog("b " + Process.getInstance().Id);
        System.out.println("b " + Process.getInstance().Id + " " + vectorClock.get(Process.getInstance().Id));
        Process.getInstance().Logger.WriteToLog("d " + Process.getInstance().Id + " " + vectorClock.get(Process.getInstance().Id));
        System.out.println("d " + Process.getInstance().Id + " " + vectorClock.get(Process.getInstance().Id));
        PrintVC();


        uniformReliableBroadcast.Broadcast(content, VCBroadcast);
        //vectorClock.incrementAndGet(Process.getInstance().Id);
        vectorClock.get(Process.getInstance().Id).incrementAndGet();
    }

    public void Deliver(MessageModel message, MessageModel originalMessage, int content, int portReceived, InetAddress addressReceived, int[] vectorClock) throws IOException {
        if(uniformReliableBroadcast.Deliver(message, originalMessage, content, portReceived, addressReceived, vectorClock)){

            int originalProcessId = originalMessage.getProcessId();
            if(Process.getInstance().Id != originalProcessId){
                pending[originalProcessId].add(vectorClock);
                DeliverOrPend();
            }
        }
    }

    private void DeliverOrPend(){
        int numOfProcesses = Process.getInstance().processes.size();

        for (int pId = 1; pId <= numOfProcesses; pId++){

            //Check pending VCs for each process
            Iterator<int[]> it = pending[pId].iterator();
            while (it.hasNext()) {
                int[] copyVC = ArrayCopy(vectorClock, copyMask);
                int[] VCx = it.next();
                boolean deliver = true;

                //Compare VC with VCx to decide delivery action
                for (int j= 1; j <= numOfProcesses; j++){
                    if( copyVC[j] < VCx[j])
                        deliver = false;
                }
                if (deliver) {
                    it.remove();
                    it = pending[pId].iterator();
                    System.out.println("d " + pId + " " + vectorClock.get(pId));
                    PrintVC();

                    Process.getInstance().Logger.WriteToLog("d " + pId + " " + vectorClock.get(pId));
                    //vectorClock.incrementAndGet(pId);
                    vectorClock.get(pId).incrementAndGet();
                }
            }
        }
    }
}