package m3da.server.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import m3da.server.store.Message;
import m3da.server.store.StoreService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class GetDataServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(GetDataServlet.class);

    private StoreService store;

    public GetDataServlet(StoreService store) {
        this.store = store;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String system = req.getPathInfo();
        if (system == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "no system id in the path");
            return;
        }

        system = system.substring(1);
        LOG.info("system " + system);
        List<Message> data;
        while ((data = store.popData(system)) != null) {
            resp.getWriter().write("data: ");
            for (Message d : data) {
                LOG.info("data: " + d);
                resp.getWriter().write(d + "\n");
            }
        }
    }
}