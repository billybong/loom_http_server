package samples.sample3.server;

import util.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.concurrent.atomic.AtomicInteger;

@Path("/hello")
public class Endpoint {

    private static AtomicInteger concurrency = new AtomicInteger();

    @GET
    public String hello() throws InterruptedException {
        //Logger.log("in endpoint");
        concurrency.incrementAndGet();
        //Thread.sleep(1000);
        concurrency.decrementAndGet();
        return "hello";
    }

    public static int getConcurrency(){
        return concurrency.get();
    }
}
