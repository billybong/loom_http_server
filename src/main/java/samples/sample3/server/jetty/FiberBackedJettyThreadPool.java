package samples.sample3.server.jetty;

import org.eclipse.jetty.util.thread.ThreadPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FiberBackedJettyThreadPool implements ThreadPool {
    private final ExecutorService executorService;

    public FiberBackedJettyThreadPool(int osThreads) {
        executorService =  Executors.newFixedThreadPool(osThreads);
    }

    @Override
    public void execute(Runnable command) {
        FiberScope.background().schedule(executorService, command);
    }

    @Override
    public void join() throws InterruptedException {
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }

    @Override
    public int getThreads() {
        return 0;
    }

    @Override
    public int getIdleThreads() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isLowOnThreads() {
        return false;
    }
}
