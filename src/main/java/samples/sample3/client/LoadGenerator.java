package samples.sample3.client;

import util.Logger;

import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

public class LoadGenerator {
    private static final int CONCURRENT_REQUESTS = 10_000;

    private final Executor fiberExecutor = runnable -> FiberScope.background().schedule(runnable);
    private final HttpClient httpClient = HttpClient.newBuilder().executor(fiberExecutor).build();
    private final HttpRequest httpRequest = HttpRequest.newBuilder(URI.create("http://127.0.0.1:9080/hello")).build();
    private final HttpResponse.BodyHandler<Void> discardingBodyHandler = HttpResponse.BodyHandlers.discarding();
    private final Statistics statistics;

    private LoadGenerator(Statistics statistics) {
        this.statistics = statistics;
    }

    public static void main(String[] args) throws Exception {
        var statistics = new Statistics();
        ManagementFactory.getPlatformMBeanServer().registerMBean(statistics, new ObjectName("loadGenerator:type=Stats"));
        new LoadGenerator(statistics).start();
    }

    private void start() throws Exception {
        var backgroundScope = FiberScope.background();
        var requestLimiter = new Semaphore(CONCURRENT_REQUESTS);
        Logger.log("Sending requests...");
        while (true) {
            requestLimiter.acquire();
            backgroundScope.schedule(() -> {
                try {
                    var responseTime = sendHttpRequest();
                    statistics.recordSuccess(responseTime);
                } catch (Exception e) {
                    e.printStackTrace();
                    statistics.recordFailure();
                } finally {
                    requestLimiter.release();
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
}
