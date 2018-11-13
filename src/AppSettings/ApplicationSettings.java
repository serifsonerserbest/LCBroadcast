package AppSettings;

public class ApplicationSettings {


    private static final ApplicationSettings applicationSettings = new ApplicationSettings();
    public final boolean isDebug;
    public final int timeoutVal = 300; // 300ms until timeout

    private ApplicationSettings() {
        isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
    }

    public static ApplicationSettings getInstance() {
        return applicationSettings;
    }
}
