package LocalCausalBroadcast;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class ThreadSafeArray {
    private AtomicIntegerArray array;
    private  int length;

    public ThreadSafeArray(int length){
        array = new AtomicIntegerArray(length);
        this.length = length;
    }

    public synchronized int[] IncOrCopy(boolean incOrCopy, int index){
        //Increment
        if(incOrCopy){
            array.incrementAndGet(index);
            return null;
        }
        //Copy
        else{
            int[] copy = new int[length];
            for(int i= 0; i < length; i++){
                //TODO: Check dependencies
                if (true)
                    copy[i] = array.get(i);
            }
            return copy;
        }
    }

    public synchronized int Get(int index){
        return array.get(index);
    }

    public synchronized int Length(){
        return length;
    }

}