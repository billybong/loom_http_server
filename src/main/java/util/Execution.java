package util;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Execution {

    private Execution() {}

    public final static ExecutorService SINGLE_THREAD_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        var thread = new Thread(runnable);
        thread.setDaemon(true);
        return thread;
    });
    public final static Executor FIBER_EXECUTOR = command -> FiberScope.background().schedule(SINGLE_THREAD_EXECUTOR, command);
}
