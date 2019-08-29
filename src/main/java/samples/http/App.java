package samples.http;

import util.Execution;
import util.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class App {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().executor(Execution.SINGLE_THREADED_FIBER_EXECUTOR).build();
    private static final HttpRequest HTTP_REQUEST = HttpRequest.newBuilder(URI.create("http://127.0.0.1:8080/")).GET().build();
    private static final HttpResponse.BodyHandler<Void> RESPONSE_BODY_HANDLER = HttpResponse.BodyHandlers.discarding();
    //private static final SampleNanoServer HTTP_SERVER = SampleNanoServer.startServer();
    private static final SampleSDKServer HTTP_SERVER = SampleSDKServer.start();

    public static void main(String[] args) {
        int nrOfRequests = 1_000;
        var started = 0L;
        warmup();
        started = System.currentTimeMillis();
        try (var scope = FiberScope.open()) {
            for (int i = 0; i < nrOfRequests; i++) {
                scope.schedule(Execution.SINGLE_THREAD_EXECUTOR, App::sendRequest);
            }
        } finally {
            Logger.log(String.format("Took %d milliseconds", System.currentTimeMillis() - started));
            HTTP_SERVER.close();
        }
    }

    private static void sendRequest() {
        try {
            var started = System.currentTimeMillis();
            Logger.log("Sending request");
            var response = HTTP_CLIENT.send(HTTP_REQUEST, RESPONSE_BODY_HANDLER);
            Logger.log(String.format("Received response: %s after %d ms", response.statusCode(), System.currentTimeMillis() - started));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void warmup() {
        Logger.log("Warming up JIT...");
        var warmupDurationInSeconds = 30;
        Logger.disable();
        var started = System.currentTimeMillis();
        while (System.currentTimeMillis() - started < warmupDurationInSeconds * 1000) {
            try (var scope = FiberScope.open()) {
                for (int i = 0; i < 100; i++) {
                    scope.schedule(Execution.SINGLE_THREAD_EXECUTOR, () -> HTTP_CLIENT.send(HTTP_REQUEST, RESPONSE_BODY_HANDLER));
                }
            }
        }
        Logger.enable();
        Logger.log("Warmed up!");
    }
}
