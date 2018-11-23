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
    private  AtomicIntegerArray vectorClock;

    private LinkedList<int[]>[] pending; // Keeps vector clocks according to process id
    private boolean[] dependencyMask;

    private LocalCausalBroadcast(){
        int numOfProcesses = Process.getInstance().processes.size();

        uniformReliableBroadcast = new UniformReliableBroadcast();
        vectorClock = new AtomicIntegerArray(numOfProcesses + 1);
        pending = new LinkedList[numOfProcesses + 1];
        dependencyMask = Process.getInstance().dependencies;

        for (int i = 0; i <= numOfProcesses; i++){
            vectorClock.set(i, 1);
            pending[i] = new LinkedList<>();
        }

    }

    public static LocalCausalBroadcast getInst() {

        return localCausalBroadcast;
    }

    private synchronized void PrintVC(AtomicIntegerArray VC){
        for (int i = 0; i < VC.length(); i++) {
            if (i > 0) {
                System.out.print(", ");
            }
            System.out.print(VC.get(i));
        }
        System.out.println("");
    }

    public synchronized void Broadcast(int content){

        // Create new vector clock to only send dependencies
        // TODO: Change it with elementwise multiplication of two matrices
        int numOfProcesses = Process.getInstance().processes.size();
        int[] VCBroadcast = new int[numOfProcesses + 1];
        for(int i= 1; i <= numOfProcesses; i++){
            if ( true/*dependencyMask[i]*/)
                VCBroadcast[i] = vectorClock.get(i);
        }

        System.out.println("b " + Process.getInstance().Id + " " + vectorClock.get(Process.getInstance().Id));
        PrintVC(vectorClock);

        Process.getInstance().Logger.WriteToLog("b " + Process.getInstance().Id);

        uniformReliableBroadcast.Broadcast(content, VCBroadcast);
        vectorClock.incrementAndGet(Process.getInstance().Id);
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

                int[] VCx = it.next();
                boolean deliver = true;

                //Compare VC with VCx to decide delivery action
                for (int j= 1; j <= numOfProcesses; j++){
                    if( vectorClock.get(j) < VCx[j])
                        deliver = false;
                }
                if (deliver) {
                    it.remove();
                    System.out.println("d " + pId + " " + vectorClock.get(pId));
                    PrintVC(vectorClock);

                    Process.getInstance().Logger.WriteToLog("d " + pId + " " + vectorClock.get(pId));
                    vectorClock.incrementAndGet(pId);
                }
                else{
//                    System.out.println("No delivery: VC and VCx:");
//                    PrintVC(vectorClock);
//                    PrintVC(VCx);
                }
            }
        }
    }
}
