package samples.sample3;

import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import util.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;

public class JerseyApplication {

    public static void main(String[] args) {
        var fiberThreadPool = new FiberBackedThreadPool();
        var server = new Server(fiberThreadPool);
        doTheJettyCeremonialDance(server);

        try {
            server.start();
            server.join();
        } catch (Exception ex) {
            Logger.log("Error occurred while starting Jetty: "+ ex.getMessage());
            System.exit(1);
        } finally {
            server.destroy();
        }
    }


    private static void doTheJettyCeremonialDance(Server server) {
        ServerConnector http = new ServerConnector(server, 1, 1, new HttpConnectionFactory());
        http.setPort(8080);
        server.addConnector(http);
        ServletContextHandler servletContextHandler = new ServletContextHandler(NO_SESSIONS);
        servletContextHandler.setContextPath("/");
        server.setHandler(servletContextHandler);

        ServletHolder servletHolder = servletContextHandler.addServlet(ServletContainer.class, "/*");
        servletHolder.setInitOrder(0);
        servletHolder.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES, Endpoint.class.getCanonicalName());
    }

    public static class FiberBackedThreadPool implements ThreadPool {
        //Jetty reserves at least 1 thread for IO connector, so 2 is magic number to get 1 worker thread.
        ExecutorService executorService = Executors.newWorkStealingPool(2);

        @Override
        public void execute(Runnable command) {
            FiberScope.background().schedule(executorService, command);
        }

        @Override
        public void join() throws InterruptedException {
            new CountDownLatch(1).await();
        }

        @Override
        public int getThreads() {
            return 0;
        }

        @Override
        public int getIdleThreads() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isLowOnThreads() {
            return false;
        }
    }
}