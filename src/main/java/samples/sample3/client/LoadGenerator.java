package samples.sample3.client;

import util.Logger;

import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

public class LoadGenerator implements LoadGeneratorMBean {
    private final Semaphore requestLimiter = new Semaphore(10_000);

    private final HttpClient httpClient = HttpClient.newBuilder().executor(r -> FiberScope.background().schedule(r)).build();
    private final HttpRequest httpRequest = HttpRequest.newBuilder(URI.create("http://127.0.0.1:9080/hello")).build();
    private final HttpResponse.BodyHandler<Void> discardingBodyHandler = HttpResponse.BodyHandlers.discarding();

    private final AtomicLong maxResponseTime = new AtomicLong();
    private final AtomicLong minResponseTime = new AtomicLong(-1);
    private final AtomicLong successCount = new AtomicLong();
    private final AtomicLong failureCounts = new AtomicLong();
    private final AtomicLong successesPerSecond = new AtomicLong();
    private final AtomicLong failuresPerSecond = new AtomicLong();

    public static void main(String[] args) throws Exception {
        new LoadGenerator().start();
    }

    private void start() throws Exception {
        ManagementFactory.getPlatformMBeanServer().registerMBean(this, new ObjectName("loadGenerator:type=LoadGenerator"));
        FiberScope.background().schedule(this::resetMetrics);
        Logger.log("Sending requests...");
        try (var scope = FiberScope.open()) {
            while (true) {
                requestLimiter.acquire();
                scope.schedule(this::sendHttpRequest);
            }
        }
    }

    private void sendHttpRequest() {
        try {
            var started = System.currentTimeMillis();
            httpClient.send(httpRequest, discardingBodyHandler);
            var responseTime = System.currentTimeMillis() - started;
            recordSuccess(responseTime);
        } catch (IOException | InterruptedException exception) {
            failureCounts.incrementAndGet();
            exception.printStackTrace();
        } finally {
            requestLimiter.release();
        }
    }

    private void resetMetrics() {
        try {
            while (true) {
                Thread.sleep(1000);
                maxResponseTime.set(0);
                minResponseTime.set(-1);
                failuresPerSecond.set(failureCounts.getAndSet(0));
                successesPerSecond.set(successCount.getAndSet(0));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void recordSuccess(long responseTime) {
        successCount.incrementAndGet();
        minResponseTime.getAndUpdate(i -> i == -1 ? responseTime : Math.min(i, responseTime));
        maxResponseTime.getAndUpdate(i -> Math.max(i, responseTime));
    }

    @Override
    public long getMaxResponseTime() {
        return maxResponseTime.get();
    }

    @Override
    public long getMinResponseTime() {
        return minResponseTime.get();
    }

    @Override
    public long getSuccessesPerSecond() {
        return successesPerSecond.get();
    }

    @Override
    public long getFailuresPerSecond() {
        return failuresPerSecond.get();
    }
}
