package samples.sample3.client;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Semaphore;

public class LoadGenerator {

    private static Statistics statistics = new Statistics();

    public static void main(String[] args) throws Exception {
        var mbeanServer = ManagementFactory.getPlatformMBeanServer();
        mbeanServer.registerMBean(statistics, new ObjectName("loadGenerator:type=Stats"));

        var httpClient = HttpClient.newBuilder().executor(runnable -> FiberScope.background().schedule(runnable)).build();
        var httpRequest = HttpRequest.newBuilder(URI.create("http://127.0.0.1:9080/hello")).build();
        var requestLimiter = new Semaphore(10_000);
        while (true) {
            requestLimiter.acquire();
            FiberScope.background().schedule(() -> {
                try {
                    var started = System.currentTimeMillis();
                    httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());
                    var responseTime = System.currentTimeMillis() - started;
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
}