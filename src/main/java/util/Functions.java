package util;

import java.util.concurrent.Callable;

public final class Functions {
    private Functions() {
    }

    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface SupplierWithException<T> {
        T produce() throws Exception;
    }

    public static <T> Callable<T> unchecked(SupplierWithException<T> supplier) {
        return () -> {
            try {
                return supplier.produce();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static Runnable unchecked(RunnableWithException r) {
        return () -> {
            try {
                r.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
