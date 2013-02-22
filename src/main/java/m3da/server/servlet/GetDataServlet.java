/*******************************************************************************
 * Copyright (c) 2013 Sierra Wireless.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 ******************************************************************************/
package m3da.server.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
		Map<Long, List<Message>> data = store.lastReceivedData(system);
		for (Map.Entry<Long, List<Message>> e : data.entrySet()) {
			resp.getWriter().write("received at : " + e.getKey());
			for (Message d : e.getValue()) {
				LOG.info("data: " + d);
				resp.getWriter().write(d + "\n");
			}

		}
	}
}
