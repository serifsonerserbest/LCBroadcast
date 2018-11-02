package Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Logger {
    private List<String> Log;
    public final String outputFileName;

    public Logger(int processId) {
        Log = new ArrayList<String>();
        outputFileName = "da_proc_" + processId + ".out";
    }

    public void WriteToLog (final String message) {
        // todo think about multithreading
        if(message != null && !message.trim().isEmpty())
        {
            Log.add(message);
        }
    }

    public void WriteLogToFile () {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))){
            for (String message : Log) {
                writer.write(message + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
