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

/**
 * @noinspection WeakerAccess
 */
public class LoadGenerator implements LoadGeneratorMBean {
    private static final int MAX_CONCURRENCY = 1000;

    public final Semaphore requestLimiter = new Semaphore(MAX_CONCURRENCY);

    final HttpClient httpClient = HttpClient.newBuilder().executor(r -> FiberScope.background().schedule(r)).build();
    final HttpRequest httpRequest = HttpRequest.newBuilder(URI.create("http://127.0.0.1:9080/hello")).build();
    final HttpResponse.BodyHandler<Void> discardingBodyHandler = HttpResponse.BodyHandlers.discarding();

    private final AtomicLong successCount = new AtomicLong();
    private final AtomicLong failureCounts = new AtomicLong();
    private final AtomicLong maxResponseTime = new AtomicLong();
    private final AtomicLong minResponseTime = new AtomicLong(Integer.MAX_VALUE);
    private final AtomicLong requestsPerSecond = new AtomicLong();
    private final AtomicLong failuresPerSecond = new AtomicLong();

    public static void main(String[] args) throws Exception {
        new LoadGenerator().start();
    }

    public void start() throws Exception {
        ManagementFactory.getPlatformMBeanServer().registerMBean(this, new ObjectName("loadGenerator:type=LoadGenerator"));
        scheduleMetricResets();
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
            recordMetrics(responseTime);
        } catch (IOException | InterruptedException connectException){
            failureCounts.incrementAndGet();
        } finally {
            requestLimiter.release();
        }
    }

    private void scheduleMetricResets() {
        FiberScope.background().schedule(() -> {
            while (true) {
                sleep(1000);
                maxResponseTime.set(0);
                minResponseTime.set(Integer.MAX_VALUE);
                failuresPerSecond.set(failureCounts.getAndSet(0));
                requestsPerSecond.set(successCount.getAndSet(0));
            }
        });
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void recordMetrics(long responseTime) {
        successCount.incrementAndGet();
        minResponseTime.getAndUpdate(i -> Math.min(i, responseTime));
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
    public long getRequestsPerSecond() {
        return requestsPerSecond.get();
    }

    @Override
    public long getFailuresPerSecond() {
        return failuresPerSecond.get();
    }
}
