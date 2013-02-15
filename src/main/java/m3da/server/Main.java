package m3da.server;

import m3da.server.servlet.GetDataServlet;
import m3da.server.store.MapDbStoreService;
import m3da.server.tcp.M3daTcpServer;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * 
 * This class launches the web application in an embedded Jetty container. This is the entry point to your application.
 * The Java command that is used for launching should fire this main method.
 * 
 */
public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String webappDirLocation = "src/main/webapp/";

        // The port that we should run on can be set into an environment variable
        // Look for that variable and default to 8080 if it isn't there.
        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }

        Server server = new Server(Integer.valueOf(webPort));
        WebAppContext root = new WebAppContext();

        root.setContextPath("/");
        root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
        root.setResourceBase(webappDirLocation);

        // Parent loader priority is a class loader setting that Jetty accepts.
        // By default Jetty will behave like most web containers in that it will
        // allow your application to replace non-server libraries that are part of the
        // container. Setting parent loader priority to true changes this behavior.
        // Read more here: http://wiki.eclipse.org/Jetty/Reference/Jetty_Classloading
        root.setParentLoaderPriority(true);

        MapDbStoreService service = new MapDbStoreService();

        ServletHolder servletHolder = new ServletHolder(new GetDataServlet(service));
        root.addServlet(servletHolder, "/data/*");

        server.setHandler(root);

        server.start();

        service.start();
        /*
         * service.enqueueData("sys", System.currentTimeMillis(), new ArrayList<Data>());
         * service.enqueueData("systemId", System.nanoTime(), Collections.singletonList(new Data("A", null)));
         * service.enqueueData("systemId", System.nanoTime(), Collections.singletonList(new Data("B", null)));
         * service.enqueueData("systemId", System.nanoTime(), Collections.singletonList(new Data("C", null)));
         * service.enqueueData("systemId2", System.nanoTime(), new ArrayList<Data>()); service.enqueueData("systemId2",
         * System.nanoTime(), new ArrayList<Data>()); System.err.println("pop1 " + service.popData("systemId"));
         * System.err.println("pop2 " + service.popData("systemId")); System.err.println("pop3 " +
         * service.popData("systemId")); System.err.println("pop4 " + service.popData("systemId"));
         */

        M3daTcpServer tcpServer = new M3daTcpServer(2, 30, 44900, 4, 8, service);
        tcpServer.start();
        server.join();

        tcpServer.stop();
        service.stop();

    }

}
