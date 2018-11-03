package SignalHandler;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class DiagnosticSignalHandler implements SignalHandler {

    public static void install(String signalName, SignalHandler handler) {

        Signal signal = new Signal(signalName);
        DiagnosticSignalHandler diagnosticSignalHandler = new DiagnosticSignalHandler();
        Signal.handle(signal, diagnosticSignalHandler);
        diagnosticSignalHandler.setHandler(handler);
    }
    private SignalHandler handler;

    private DiagnosticSignalHandler() {
    }

    private void setHandler(SignalHandler handler) {
        this.handler = handler;
    }

    @Override
    public void handle(Signal sig) {
        System.out.println("Diagnostic Signal handler called for signal " + sig);
        try {
            handler.handle(sig);

        } catch (Exception e) {
            System.out.println("Signal handler failed, reason " + e);
        }
    }

}
