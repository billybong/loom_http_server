package samples.http;

import util.Execution;
import util.Functions;
import util.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static util.Functions.unchecked;

public class App {
    private final static HttpClient HTTP_CLIENT = HttpClient.newBuilder().executor(Execution.FIBER_EXECUTOR).build();
    private final static HttpRequest HTTP_REQUEST = HttpRequest.newBuilder(URI.create("http://127.0.0.1:8080/")).GET().build();

    public static void main(String[] args) throws Exception {
        int nrOfRequests = 1_000;
        warmup();

        var started = 0L;
        var httpServer = SampleHttpServer.start();
        try (var scope = FiberScope.open()) {
            started = System.currentTimeMillis();
            for (int i = 0; i < nrOfRequests; i++) {
                scope.schedule(Execution.SINGLE_THREAD_EXECUTOR, App::sendRequest);
            }
        } finally {
            Logger.log(String.format("Took %d milliseconds", System.currentTimeMillis() - started));
            httpServer.close();
        }

    }

    private static void warmup() {
        try (var httpServer = SampleHttpServer.start(); var scope = FiberScope.open()) {
            for (int i = 0; i < 1000; i++) {
                scope.schedule(Execution.SINGLE_THREAD_EXECUTOR, App::sendRequest);
            }
        }
    }

    private static void sendRequest() {
        try {
            var started = System.currentTimeMillis();
            Logger.log("Sending request");
            var response = HTTP_CLIENT.send(HTTP_REQUEST, HttpResponse.BodyHandlers.ofString());
            Logger.log(String.format("Received response: %s after %d ms", response.body(), System.currentTimeMillis() - started));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
