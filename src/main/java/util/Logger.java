package util;

public class Logger {
    public static void log(String message) {
        var prefix = Fiber.current().map(Fiber::toString).orElse(Thread.currentThread().toString());
        System.out.println("[" + prefix + "] : " + message);
    }
}
