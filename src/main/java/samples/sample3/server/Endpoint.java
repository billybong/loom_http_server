package samples.sample3.server;

import util.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/hello")
public class Endpoint {

    @GET
    public String hello() throws InterruptedException {
        Logger.log("in endpoint");
        Thread.sleep(1000);
        return "hello";
    }
}
