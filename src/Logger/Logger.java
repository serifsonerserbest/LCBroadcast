package Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Logger {
    private ConcurrentLinkedQueue<String> Log;
    public final String outputFileName;

    public Logger(int processId) {
        Log = new ConcurrentLinkedQueue<String>();
        outputFileName = "da_proc_" + processId + ".out";
    }

    public void WriteToLog(final String message) {

        if (message != null && !message.trim().isEmpty()) {
            Log.add(message);
        }
    }

    public void WriteLogToFile() {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))) {
            for (String message : Log) {
                writer.write(message + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
