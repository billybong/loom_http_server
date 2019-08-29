package samples.http;

import fi.iki.elonen.NanoHTTPD;
import util.Execution;
import util.IO;
import util.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SampleNanoServer extends NanoHTTPD implements AutoCloseable {

    public SampleNanoServer() throws IOException {
        super(8080);
        setAsyncRunner(new AsyncRunner() {
            private final List<ClientHandler> running = Collections.synchronizedList(new ArrayList<ClientHandler>());

            @Override
            public void closeAll() {
                running.forEach(ClientHandler::close);
                running.clear();
            }

            @Override
            public void closed(ClientHandler clientHandler) {
                running.remove(clientHandler);
            }

            @Override
            public void exec(ClientHandler code) {
                Execution.SINGLE_THREADED_FIBER_EXECUTOR.execute(code);
            }
        });
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
    }

    public static SampleNanoServer startServer() {
        try {
            return new SampleNanoServer();
        } catch (IOException ioe) {
            throw new RuntimeException("Couldn't start server:\n" + ioe);
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        Logger.log("Received message");
        IO.sleep();
        return newFixedLengthResponse("");
    }

    @Override
    public void close() {
        super.closeAllConnections();
    }
}