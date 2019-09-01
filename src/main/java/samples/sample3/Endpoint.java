package samples.sample3;

import util.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.concurrent.atomic.AtomicInteger;

@Path("/hello")
public class Endpoint {

    private static AtomicInteger ongoingWaits = new AtomicInteger();
    private static AtomicInteger maxSeenConcurrency = new AtomicInteger();

    @GET
    public String hello() throws InterruptedException {
        var currentlyWaiting = ongoingWaits.incrementAndGet();
        maxSeenConcurrency.getAndUpdate(currentHighMark -> {
            if(currentlyWaiting <= currentHighMark){
                return currentHighMark;
            }
            Logger.log("At " + currentlyWaiting + " concurrency");
            return currentlyWaiting;
        });
        Thread.sleep(1000);
        ongoingWaits.decrementAndGet();
        return "hello";
    }
}
