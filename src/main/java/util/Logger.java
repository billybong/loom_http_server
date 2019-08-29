package util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Logger {
    private static NanoClock CLOCK = new NanoClock();
    private static boolean ENABLED = true;
    public static void log(String message) {
        if(!ENABLED){
            return;
        }
        var now = Instant.now(CLOCK).truncatedTo(ChronoUnit.MILLIS);
        var prefix = Fiber.current().map(Fiber::toString).orElse(Thread.currentThread().toString());
        System.out.println(now + " - [" + prefix + "] : " + message);
    }

    public static void enable(){
        ENABLED = true;
    }

    public static void disable() {
        ENABLED = false;
    }
}
