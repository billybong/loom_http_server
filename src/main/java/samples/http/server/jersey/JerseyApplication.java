package samples.http.server.jersey;

import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Execution;

import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;

import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;

public class JerseyApplication {

    private static final Logger logger = LoggerFactory.getLogger(JerseyApplication.class);

    public static void main(String[] args) {
        Server server = new Server(createThreadPool());

        ServerConnector http = new ServerConnector(server, new HttpConnectionFactory());
        http.setPort(8080);
        server.addConnector(http);
        ServletContextHandler servletContextHandler = new ServletContextHandler(NO_SESSIONS);
        servletContextHandler.setContextPath("/");
        server.setHandler(servletContextHandler);

        ServletHolder servletHolder = servletContextHandler.addServlet(ServletContainer.class, "/*");
        servletHolder.setInitOrder(0);
        servletHolder.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES, Endpoint.class.getCanonicalName());

        try {
            server.start();
            new CountDownLatch(1).await();
        } catch (Exception ex) {
            logger.error("Error occurred while starting Jetty", ex);
            System.exit(1);
        } finally {
            server.destroy();
        }
    }

    private static ThreadPool createThreadPool() {
        ThreadPoolExecutor threadPoolExecutor = new FiberWrappingThreadPoolExecutor(2, 4, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        return new ExecutorThreadPool(threadPoolExecutor);
    }

    private static class FiberWrappingThreadPoolExecutor extends ThreadPoolExecutor {

        public FiberWrappingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        @Override
        public void execute(Runnable command) {
            FiberScope.background().schedule(super::execute, command);
            //FiberScope.background().schedule(Execution.SINGLE_THREAD_EXECUTOR, command);
        }
    }
}