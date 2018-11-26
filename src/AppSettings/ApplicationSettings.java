package AppSettings;

public class ApplicationSettings {


    private static final ApplicationSettings applicationSettings = new ApplicationSettings();
    public final boolean isDebug;
    public final int timeoutVal = 300; // 300ms until timeout
    public final int ListenerThreadPoolSize = Runtime.getRuntime().availableProcessors() / 2 /*(Runtime.getRuntime().availableProcessors() + 1) * 30*/;
    public final int SenderThreadPoolSize = Runtime.getRuntime().availableProcessors() / 4/*(Runtime.getRuntime().availableProcessors() + 1) * 30*/;


    private ApplicationSettings() {
        isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
    }

    public static ApplicationSettings getInstance() {
        return applicationSettings;
    }
}
