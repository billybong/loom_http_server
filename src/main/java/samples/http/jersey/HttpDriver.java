package samples.http.jersey;

import util.Execution;
import util.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HttpDriver {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().build();
    private static final HttpRequest HTTP_REQUEST = HttpRequest.newBuilder(URI.create("http://127.0.0.1:8080/hello")).GET().build();
    private static final HttpResponse.BodyHandler<Void> DISCARDING_BODY_HANDLER = HttpResponse.BodyHandlers.discarding();
    private static final int MAX_REQUESTS = 1_000;
    private static final int RAMP_UP = 100;

    public static void main(String[] args) {
        var requests = RAMP_UP;
        while (true) {
            try (var scope = FiberScope.open()) {
                var started = System.currentTimeMillis();
                var responseFibers = IntStream.range(0, requests)
                        .mapToObj(i -> scope.schedule(Execution.FIBER_EXECUTOR, HttpDriver::sendRequest))
                        .collect(Collectors.toList());

                var responseTimes = responseFibers.stream()
                        .map(fiber -> fiber.toFuture().join())
                        .sorted()
                        .collect(Collectors.toList());

                var duration = System.currentTimeMillis() - started;
                var fastest = responseTimes.get(0);
                var slowest = responseTimes.get(responseTimes.size() - 1);

                Logger.log(String.format("%d requests in %d ms. Fastest: %d Slowest: %d", requests, duration, fastest, slowest));
            }
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
