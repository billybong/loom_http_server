package samples.http.server;

import fi.iki.elonen.NanoHTTPD;
import util.Execution;
import util.IO;
import util.Logger;

import java.io.IOException;

public class SampleNanoServer extends NanoHTTPD implements AutoCloseable {

    public SampleNanoServer() throws IOException {
        super(8080);
        setAsyncRunner(new FiberAsyncRunner());
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

    private class FiberAsyncRunner extends DefaultAsyncRunner implements AsyncRunner {
        @Override
        public void exec(ClientHandler code) {
            Execution.FIBER_EXECUTOR.execute(code);
        }
    }
}