package samples.http.server;

import util.Execution;
import util.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class App {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().executor(Execution.FIBER_EXECUTOR).build();
    private static final HttpRequest HTTP_REQUEST = HttpRequest.newBuilder(URI.create("http://127.0.0.1:8080/")).GET().build();
    private static final HttpResponse.BodyHandler<Void> DISCARDING_BODY_HANDLER = HttpResponse.BodyHandlers.discarding();

    public static void main(String[] args) {
        int nrOfRequests = 1_000;
        try (var httpServer = SampleNanoServer.startServer()) {
            var started = 0L;
            warmup(30);
            started = System.currentTimeMillis();
            try (var scope = FiberScope.open()) {
                for (int i = 0; i < nrOfRequests; i++) {
                    scope.schedule(Execution.SINGLE_THREAD_EXECUTOR, App::sendRequest);
                }
            }
            Logger.log(String.format("Took %d milliseconds", System.currentTimeMillis() - started));
        }
    }

    private static void sendRequest() {
        try {
            Logger.log("Sending request");
            var started = System.currentTimeMillis();
            var response = HTTP_CLIENT.send(HTTP_REQUEST, DISCARDING_BODY_HANDLER);
            Logger.log(String.format("Received response: %s after %d ms", response.statusCode(), System.currentTimeMillis() - started));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void warmup(int warmupDurationInSeconds) {
        Logger.log("Warming up JIT...");
        Logger.disable();
        var started = System.currentTimeMillis();
        while (System.currentTimeMillis() - started < warmupDurationInSeconds * 1000) {
            try (var scope = FiberScope.open()) {
                for (int i = 0; i < 100; i++) {
                    scope.schedule(Execution.SINGLE_THREAD_EXECUTOR, () -> HTTP_CLIENT.send(HTTP_REQUEST, DISCARDING_BODY_HANDLER));
                }
            }
        }
        Logger.enable();
        Logger.log("Warmed up!");
    }
}
