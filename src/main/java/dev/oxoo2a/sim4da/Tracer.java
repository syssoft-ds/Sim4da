package dev.oxoo2a.sim4da;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintStream;

public class Tracer {
    
    public Tracer (String name, boolean ordered, boolean enableTracing, boolean useLog4j2, PrintStream alternativeDestination) {
        this.name = name;
        this.ordered = ordered;
        this.silent = !enableTracing;
        this.useLog4j2 = useLog4j2;
        this.alternativeDestination = alternativeDestination;

        log4j2Logger = LogManager.getFormatterLogger(name);
        log4j2Clock = LogManager.getFormatterLogger("clock");
    }

    public void emit (String format, String logType, Object ... args) {
        if (silent) return;
        if (useLog4j2) {
            if(logType == "main")
            {
                log4j2Logger.trace(format,args);
            }
            else {
                log4j2Clock.trace(format, args);
            }

        }
        if (alternativeDestination != null) {
            alternativeDestination.printf(format,args);
            alternativeDestination.println();
        }
    }

    private final String name;
    private final boolean ordered;
    private final boolean silent;
    private final boolean useLog4j2;
    private final PrintStream alternativeDestination;
    private final Logger log4j2Logger;
    private final Logger log4j2Clock;
}
