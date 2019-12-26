package samples.sample3.server.resilience;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class Retries {
    public static <T> Supplier<T> retry(Callable<T> callable) {
        return () -> {
            int triesLeft = 3;
            Exception lastException = null;
            while (triesLeft > 0) {
                try {
                    return callable.call();
                } catch (Exception e) {
                    --triesLeft;
                    lastException = e;
                }
            }
            throw new RuntimeException(lastException);
        };
    }
}
