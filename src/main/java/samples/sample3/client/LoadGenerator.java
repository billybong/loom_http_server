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
    private static final int CONCURRENT_REQUESTS = 10_000;

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
        LoadGenerator loadGenerator = new LoadGenerator();
        ManagementFactory.getPlatformMBeanServer().registerMBean(loadGenerator, new ObjectName("loadGenerator:type=LoadGenerator"));
        FiberScope.background().schedule(() -> {
            while (true) {
                sleep(1000);
                loadGenerator.resetMetrics();
            }
        });
        loadGenerator.start();
    }

    private void start() throws Exception {
        FiberScope scope = FiberScope.background();
        var requestLimiter = new Semaphore(CONCURRENT_REQUESTS);
        Logger.log("Sending requests...");
        while (true) {
            requestLimiter.acquire();
            scope.schedule(() -> {
                try {
                    var responseTime = sendHttpRequest();
                    recordSuccess(responseTime);
                } catch (Exception e) {
                    e.printStackTrace();
                    recordFailure();
                }
            });
        }
    }

    /**
     * @return response time in ms
     */
    private long sendHttpRequest() throws IOException, InterruptedException {
        var started = System.currentTimeMillis();
        httpClient.send(httpRequest, discardingBodyHandler);
        return System.currentTimeMillis() - started;
    }

    private void resetMetrics() {
        maxResponseTime.set(0);
        minResponseTime.set(-1);
        failuresPerSecond.set(failureCounts.getAndSet(0));
        successesPerSecond.set(successCount.getAndSet(0));
    }

    private void recordSuccess(long responseTime) {
        successCount.incrementAndGet();
        minResponseTime.getAndUpdate(i -> i == -1 ? responseTime : Math.min(i, responseTime));
        maxResponseTime.getAndUpdate(i -> Math.max(i, responseTime));
    }

    private void recordFailure() {
        this.failureCounts.getAndIncrement();
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

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
