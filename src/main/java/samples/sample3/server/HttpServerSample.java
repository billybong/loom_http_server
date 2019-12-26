package samples.sample3.server;

import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import samples.sample3.server.jetty.FiberBackedJettyThreadPool;

import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SECURITY;
import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;

public class HttpServerSample {

    public static void main(String[] args) throws Exception {
        //Jetty reserves at least 1 thread for IO selector, so 2 is magic number to get 1 worker thread.
        var jettyServer = new Server(new FiberBackedJettyThreadPool(2));
        var httpConnector = new ServerConnector(jettyServer, 0, 1, new HttpConnectionFactory());
        httpConnector.setPort(9080);
        jettyServer.addConnector(httpConnector);

        jettyServer.setHandler(jerseyServletHandler(jettyServer));

        jettyServer.start();
        jettyServer.join();
    }


    private static ServletContextHandler jerseyServletHandler(Server server) {
        var servletContextHandler = new ServletContextHandler(null, "/", NO_SESSIONS | NO_SECURITY);
        var servletHolder = servletContextHandler.addServlet(ServletContainer.class, "/*");
        servletHolder.setInitParameter(ServerProperties.PROVIDER_PACKAGES, HttpServerSample.class.getPackageName());
        return servletContextHandler;
    }
}