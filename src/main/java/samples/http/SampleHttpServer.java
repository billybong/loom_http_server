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
    private final CountDownLatch latch;
    private HttpServer httpServer;

    public static SampleHttpServer start(int requestsToAwait) throws IOException {
        var sampleHttpServer = new SampleHttpServer(requestsToAwait);
        sampleHttpServer.startHttpServer();
        return sampleHttpServer;
    }

    private SampleHttpServer(int requestsToAwait) {
        latch = new CountDownLatch(requestsToAwait);
    }

    private void startHttpServer() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("localhost", 8080), -1);
        httpServer.setExecutor(Execution.FIBER_EXECUTOR);
        httpServer.createContext("/", this::handleRequest);
        httpServer.start();
    }

    private void handleRequest(HttpExchange exchange) {
        //Logger.log("Http server received request");
        //performExpensiveIO();
        sleep(100);
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

    private void performExpensiveIO() {
        latch.countDown();
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
