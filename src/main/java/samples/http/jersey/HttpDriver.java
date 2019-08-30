package samples.http.jersey;

import util.Execution;
import util.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpDriver {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().build();
    private static final HttpRequest HTTP_REQUEST = HttpRequest.newBuilder(URI.create("http://127.0.0.1:8080/hello")).GET().build();
    private static final HttpResponse.BodyHandler<Void> DISCARDING_BODY_HANDLER = HttpResponse.BodyHandlers.discarding();

    public static void main(String[] args) {
        while (true) {
            try (var scope = FiberScope.open()) {
                for (int i = 0; i < 1_000; i++) {
                    scope.schedule(Execution.FIBER_EXECUTOR, HttpDriver::sendRequest);
                }
            }
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
}
