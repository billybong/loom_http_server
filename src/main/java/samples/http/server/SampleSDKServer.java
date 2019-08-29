package samples.http.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import util.Execution;
import util.IO;
import util.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SampleSDKServer implements AutoCloseable {

    private final static HttpClient HTTP_CLIENT = HttpClient.newBuilder().executor(Execution.SINGLE_THREADED_FIBER_EXECUTOR).build();
    private final static HttpRequest HTTP_REQUEST = HttpRequest.newBuilder(URI.create("http://127.0.0.1:8080/")).GET().build();
    private static final byte[] RESPONSE = "Hello from samples server".getBytes();
    private HttpServer httpServer;

    public static SampleSDKServer startServer() {
        var sampleHttpServer = new SampleSDKServer();
        try {
            sampleHttpServer.startHttpServer();
            return sampleHttpServer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SampleSDKServer() {
    }

    private void startHttpServer() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("localhost", 8080), -1);
        httpServer.setExecutor(Execution.SINGLE_THREADED_FIBER_EXECUTOR);
        httpServer.createContext("/", this::handleRequest);
        httpServer.start();
        awaitStartup();
    }

    private void awaitStartup() {
        while (true) {
            try {
                var response = HTTP_CLIENT.send(HTTP_REQUEST, HttpResponse.BodyHandlers.discarding());
                if(response.statusCode() != 200) {
                    Logger.log("Http server is not up yet....");
                    continue;
                }
                Logger.log("Successfully connected to http server");
                return;
            } catch (Exception e) {
                Logger.log("Http server is not up yet....");
            }
        }
    }

    private void handleRequest(HttpExchange exchange) {
        Logger.log("Http server received request");
        IO.sleep();
        exchange.getResponseHeaders().set("Content-Type", "application/octet");
        try (var responseBody = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(200, RESPONSE.length);
            responseBody.write(RESPONSE);
        } catch (IOException e) {
            Logger.log("Exception in http server..");
            e.printStackTrace();
        } finally {
            exchange.close();
        }
    }

    @Override
    public void close() {
        httpServer.stop(0);
    }
}
