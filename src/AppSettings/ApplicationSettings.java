package AppSettings;

public class ApplicationSettings {


    private static final ApplicationSettings applicationSettings = new ApplicationSettings();
    public final boolean isDebug;
    public final int timeoutVal = 300; // 300ms until timeout
    public final int ListenerThreadPoolSize = 1 /*(Runtime.getRuntime().availableProcessors() + 1) * 30*/;
    public final int SenderThreadPoolSize = 1/*(Runtime.getRuntime().availableProcessors() + 1) * 30*/;


    private ApplicationSettings() {
        isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
    }

    public static ApplicationSettings getInstance() {
        return applicationSettings;
    }
}
