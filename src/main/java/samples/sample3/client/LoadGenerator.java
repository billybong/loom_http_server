package samples.sample3.client;

import samples.sample3.client.mbean.ResponseTimesMBean;
import util.Logger;

import javax.management.*;
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
    static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().executor(r -> FiberScope.background().schedule(r)).build();
    static final HttpRequest HTTP_REQUEST = HttpRequest.newBuilder(URI.create("http://127.0.0.1:8080/hello")).build();
    static final HttpResponse.BodyHandler<Void> DISCARDING_BODY_HANDLER = HttpResponse.BodyHandlers.discarding();

    static int CURRENT_CONCURRENCY = 0;
    static final int MAX_CONCURRENCY = 1_000;
    static final Semaphore REQUEST_LIMITER = new Semaphore(CURRENT_CONCURRENCY);

    static final AtomicLong MAX_RESPONSE_TIME = new AtomicLong();
    static final AtomicLong MIN_RESPONSE_TIME = new AtomicLong(Integer.MAX_VALUE);

    public static void main(String[] args) throws Exception {
        registerResponseTimesMBean();
        scheduleRampup();

        try (var scope = FiberScope.open()) {
            while (true) {
                REQUEST_LIMITER.acquire();
                scope.schedule(() -> {
                    var started = System.currentTimeMillis();
                    HTTP_CLIENT.send(HTTP_REQUEST, DISCARDING_BODY_HANDLER);
                    var responseTime  = System.currentTimeMillis() - started;
                    MIN_RESPONSE_TIME.getAndUpdate(i -> Math.min(i, responseTime));
                    MAX_RESPONSE_TIME.getAndUpdate(i -> Math.max(i, responseTime));
                    REQUEST_LIMITER.release();
                });
            }
        }
    }

    private static void registerResponseTimesMBean() throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException, MalformedObjectNameException {
        ManagementFactory.getPlatformMBeanServer().registerMBean(
                new ResponseTimes(),
                new ObjectName("samples.samples3:type=ResponseTimeReporter")
        );
    }

    private static void scheduleRampup() {
        FiberScope.background().schedule(() -> {
            while (true) {
                sleep(1000);
                if (CURRENT_CONCURRENCY < MAX_CONCURRENCY) {
                    var rampup = Math.min(100, MAX_CONCURRENCY - CURRENT_CONCURRENCY);
                    CURRENT_CONCURRENCY = CURRENT_CONCURRENCY + rampup;
                    Logger.log("Ramping up to " + CURRENT_CONCURRENCY + " concurrent requests.");
                    REQUEST_LIMITER.release(rampup);
                }
            }
        });
    }

    public static class ResponseTimes implements ResponseTimesMBean {
        public long getMaxDuration() {
            return MAX_RESPONSE_TIME.getAndSet(0);
        }

        public long getMinDuration() {
            return MIN_RESPONSE_TIME.getAndSet(Long.MAX_VALUE);
        }

        public long getConcurrentOutboundRequests() {
            return CURRENT_CONCURRENCY - REQUEST_LIMITER.availablePermits();
        }
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
