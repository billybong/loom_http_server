package samples.sample3.client;

import samples.sample3.client.mbean.ResponseTimes;
import util.Logger;

import javax.management.*;
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
public class LoadGenerator {
    private static final int MAX_CONCURRENCY = 1000;

    public final Semaphore requestLimiter = new Semaphore(0);

    final HttpClient httpClient = HttpClient.newBuilder().executor(r -> FiberScope.background().schedule(r)).build();
    final HttpRequest httpRequest = HttpRequest.newBuilder(URI.create("http://127.0.0.1:9080/hello")).build();
    final HttpResponse.BodyHandler<Void> discardingBodyHandler = HttpResponse.BodyHandlers.discarding();

    private final AtomicLong responseCount = new AtomicLong();
    public final AtomicLong maxResponseTime = new AtomicLong();
    public final AtomicLong minResponseTime = new AtomicLong(Integer.MAX_VALUE);
    public final AtomicLong requestsPerSecond = new AtomicLong();

    public static void main(String[] args) throws Exception {
        new LoadGenerator().start();
    }

    public void start() throws Exception {
        registerResponseTimesMBean();
        scheduleMetricResets();
        scheduleRampup();

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
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            requestLimiter.release();
        }
    }

    private void scheduleRampup() {
        FiberScope.background().schedule(() -> {
            var currentConcurrency = 0;
            while (currentConcurrency < MAX_CONCURRENCY) {
                sleep(1000);
                var rampup = Math.min(50, MAX_CONCURRENCY - currentConcurrency);
                currentConcurrency = currentConcurrency + rampup;
                Logger.log("Ramping up to " + currentConcurrency + " concurrent requests.");
                requestLimiter.release(rampup);
            }
        });
    }


    private void scheduleMetricResets() {
        FiberScope.background().schedule(() -> {
            while (true) {
                sleep(1000);
                maxResponseTime.set(0);
                minResponseTime.set(Integer.MAX_VALUE);
                requestsPerSecond.set(responseCount.getAndSet(0));
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
        responseCount.incrementAndGet();
        minResponseTime.getAndUpdate(i -> Math.min(i, responseTime));
        maxResponseTime.getAndUpdate(i -> Math.max(i, responseTime));
    }

    private void registerResponseTimesMBean() throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException, MalformedObjectNameException {
        ManagementFactory.getPlatformMBeanServer().registerMBean(
                new ResponseTimes(this),
                new ObjectName("responseTimes:type=ResponseTimeReporter")
        );
        Logger.log("Registered MBean");
    }
}
