package samples.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import util.Execution;
import util.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

public class SampleHttpServer implements AutoCloseable{

    public static final byte[] RESPONSE = "Hello from samples server".getBytes();
    private HttpServer httpServer;

    public static SampleHttpServer start() {
        var sampleHttpServer = new SampleHttpServer();
        try {
            sampleHttpServer.startHttpServer();
            return sampleHttpServer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SampleHttpServer() {

    }

    private void startHttpServer() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("localhost", 8080), -1);
        httpServer.setExecutor(Execution.FIBER_EXECUTOR);
        httpServer.createContext("/", this::handleRequest);
        httpServer.start();
    }

    private void handleRequest(HttpExchange exchange) {
        Logger.log("Http server received request");
        sleep(1000);
        try (var responseBody = exchange.getResponseBody()) {
            exchange.getResponseHeaders().set("Content-Type", "application/octet");
            exchange.sendResponseHeaders(200, RESPONSE.length);
            responseBody.write(RESPONSE);
        } catch (IOException e) {
            Logger.log("Exception in http server..");
            e.printStackTrace();
        } finally {
            exchange.close();
        }
    }

    private void sleep(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        httpServer.stop(0);
    }
}
