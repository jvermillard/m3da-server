package m3da.server;

import m3da.server.servlet.GetDataServlet;
import m3da.server.store.InMemoryStoreService;
import m3da.server.store.StoreService;
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

        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }

        Server server = new Server(Integer.valueOf(webPort));
        WebAppContext root = new WebAppContext();

        root.setContextPath("/");
        root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
        root.setResourceBase(webappDirLocation);
        root.setParentLoaderPriority(true);

        StoreService service = new InMemoryStoreService(10);

        ServletHolder servletHolder = new ServletHolder(new GetDataServlet(service));
        root.addServlet(servletHolder, "/data/*");

        server.setHandler(root);

        server.start();

        service.start();

        M3daTcpServer tcpServer = new M3daTcpServer(2, 30, 44900, 4, 8, service);
        tcpServer.start();
        server.join();

        tcpServer.stop();
        service.stop();

    }

}
