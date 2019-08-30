package samples.http.server.jersey;

import util.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Path("/hello")
public class Endpoint {

    static AtomicInteger ongoingWaits = new AtomicInteger();

    @GET
    public String hello() throws BrokenBarrierException, InterruptedException {
        Logger.log("in jersey, " + ongoingWaits.incrementAndGet() + " awaiting");
        Thread.sleep(10_000);
        ongoingWaits.decrementAndGet();
        Logger.log("continuing--");
        return "hello";
    }
}
