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
package m3da.server.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link InMemoryStoreService}
 */
public class InMemoryStoreServiceTest {

	private InMemoryStoreService service = new InMemoryStoreService(3);

	@Test
	public void enqueue_3_message_get_3_message() {

		// prepare
		List<Message> msgA = new ArrayList<Message>(1);
		List<Message> msgB = new ArrayList<Message>(1);
		List<Message> msgC = new ArrayList<Message>(1);

		// run
		service.enqueueReceivedData("clientId", 1, msgA);
		service.enqueueReceivedData("clientId", 2, msgB);
		service.enqueueReceivedData("clientId", 3, msgC);

		// verify
		Map<Long, List<Message>> data = service.lastReceivedData("clientId");
		Assert.assertEquals(3, data.size());
		Assert.assertEquals(msgA, data.get(1L));
		Assert.assertEquals(msgB, data.get(2L));
		Assert.assertEquals(msgC, data.get(3L));

	}

	@Test
	public void enqueue_4_message_get_3_message_the_oldest_one_is_discarded() {

		// prepare
		List<Message> msgA = new ArrayList<Message>(1);
		List<Message> msgB = new ArrayList<Message>(1);
		List<Message> msgC = new ArrayList<Message>(1);
		List<Message> msgD = new ArrayList<Message>(1);

		// run
		service.enqueueReceivedData("clientId", 1, msgA);
		service.enqueueReceivedData("clientId", 2, msgB);
		service.enqueueReceivedData("clientId", 3, msgC);
		service.enqueueReceivedData("clientId", 4, msgD);

		// verify
		Map<Long, List<Message>> data = service.lastReceivedData("clientId");
		Assert.assertEquals(3, data.size());
		Assert.assertEquals(msgB, data.get(2L));
		Assert.assertEquals(msgC, data.get(3L));
		Assert.assertEquals(msgD, data.get(4L));

	}

}
