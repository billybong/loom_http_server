package samples.http.jersey;

import util.Execution;
import util.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class HttpDriver {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().build();
    private static final HttpRequest HTTP_REQUEST = HttpRequest.newBuilder(URI.create("http://127.0.0.1:8080/hello")).GET().build();
    private static final HttpResponse.BodyHandler<Void> DISCARDING_BODY_HANDLER = HttpResponse.BodyHandlers.discarding();
    private static final int MAX_REQUESTS = 1_000;
    private static final int RAMP_UP = 100;

    public static void main(String[] args) {
        var requests = RAMP_UP;
        while (true) {
            var started = System.currentTimeMillis();
            var fastest = new AtomicLong(Long.MAX_VALUE);
            var slowest = new AtomicLong(0L);
            try (var scope = FiberScope.open()) {
                for (int i = 0; i < requests; i++) {
                    scope.schedule(Execution.FIBER_EXECUTOR, ()-> {
                        var responseTime = HttpDriver.sendRequest();
                        slowest.getAndUpdate(current -> Math.max(responseTime, current));
                        fastest.getAndUpdate(current -> Math.min(responseTime, current));
                    });
                }
            }
            Logger.log(String.format("%d requests in %d ms. Fastest: %d Slowest: %d", requests, System.currentTimeMillis() - started, fastest.get(), slowest.get()));
            requests = Math.min(MAX_REQUESTS, requests + RAMP_UP);
        }
    }

    private static long sendRequest() {
        try {
            var started = System.currentTimeMillis();
            HTTP_CLIENT.send(HTTP_REQUEST, DISCARDING_BODY_HANDLER);
            return System.currentTimeMillis() - started;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
