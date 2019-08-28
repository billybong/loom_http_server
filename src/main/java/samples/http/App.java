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
    private final static HttpRequest HTTP_REQUEST = HttpRequest.newBuilder(URI.create("http://localhost:8080/")).GET().build();

    public static void main(String[] args) throws Exception {
        int nrOfRequests = 2000;
        for (int j = 0; j < 100; j++) {
            var started = System.currentTimeMillis();
            try (var httpServer = SampleHttpServer.start(nrOfRequests); var scope = FiberScope.open()) {
                for (int i = 0; i < nrOfRequests; i++) {
                    scope.schedule(Execution.SINGLE_THREAD_EXECUTOR, App::sendRequest);
                }
            }
            Logger.log("Took " + (System.currentTimeMillis() - started) + " milliseconds");
        }

    }

    private static void sendRequest() {
        try {
            //Logger.log("Sending request");
            var response = HTTP_CLIENT.send(HTTP_REQUEST, HttpResponse.BodyHandlers.ofString());
            //Logger.log("Received response: " + response.body());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
