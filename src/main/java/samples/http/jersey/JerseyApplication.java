package samples.http.jersey;

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
        var server = new Server(createThreadPool());
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

    private static ThreadPool createThreadPool() {
        ExecutorService executorService = Executors.newWorkStealingPool(5);

        return new ThreadPool() {
            @Override
            public void execute(Runnable command) {
                FiberScope.background().schedule(executorService, command); //--works with 1 thread - but Jetty is configured with 5
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
        };
    }

    private static void doTheJettyCeremonialDance(Server server) {
        ServerConnector http = new ServerConnector(server, new HttpConnectionFactory());
        http.setPort(8080);
        server.addConnector(http);
        ServletContextHandler servletContextHandler = new ServletContextHandler(NO_SESSIONS);
        servletContextHandler.setContextPath("/");
        server.setHandler(servletContextHandler);

        ServletHolder servletHolder = servletContextHandler.addServlet(ServletContainer.class, "/*");
        servletHolder.setInitOrder(0);
        servletHolder.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES, Endpoint.class.getCanonicalName());
    }
}