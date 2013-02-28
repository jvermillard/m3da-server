/*******************************************************************************
 * Copyright (c) 2012 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package m3da.server.api.mapping;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import m3da.server.api.json.JSystemData;
import m3da.server.store.Message;

import org.junit.Before;
import org.junit.Test;

public class Store2JsonDataMapperTest {

	Store2JsonDataMapper mapper;

	@Before
	public void setUp() {
		this.mapper = new Store2JsonDataMapper();

	}

	@Test
	public void maps_single_received_data() {

		Map<Long, List<Message>> lastReceived = new HashMap<Long, List<Message>>();
		Map<String, List<?>> data = new HashMap<String, List<?>>();
		data.put("bar", Arrays.asList(42));

		long now = System.currentTimeMillis();
		Long nanoseconds = now * 1000;
		lastReceived.put(nanoseconds, Arrays.asList(new Message("@sys.foo", data)));

		Map<String, List<JSystemData>> mapped = mapper.mapReceivedData(lastReceived);
		JSystemData bar = mapped.get("@sys.foo.bar").get(0);
		assertEquals(1, bar.getValue().size());
		assertEquals(42, bar.getValue().get(0));
		assertEquals(String.valueOf(now), bar.getTimestamp());

	}

	@Test
	public void converts_byte_buffers_to_utf8_string() {

		Map<Long, List<Message>> lastReceived = new HashMap<Long, List<Message>>();
		Map<String, List<?>> data = new HashMap<String, List<?>>();
		data.put("bar", Arrays.asList(ByteBuffer.wrap("toto".getBytes())));

		long now = System.currentTimeMillis();
		Long nanoseconds = now * 1000;
		lastReceived.put(nanoseconds, Arrays.asList(new Message("@sys.foo", data)));

		Map<String, List<JSystemData>> mapped = mapper.mapReceivedData(lastReceived);
		JSystemData bar = mapped.get("@sys.foo.bar").get(0);
		assertEquals(1, bar.getValue().size());
		assertEquals("toto", bar.getValue().get(0));
		assertEquals(String.valueOf(now), bar.getTimestamp());

	}

	@Test
	public void maps_several_received_data() {
		Map<Long, List<Message>> lastReceived = new HashMap<Long, List<Message>>();
		Map<String, List<?>> data = new HashMap<String, List<?>>();
		data.put("bar", Arrays.asList(42));
		data.put("baz", Arrays.asList("Hello"));

		long now = System.currentTimeMillis();
		Long nanoseconds = now * 1000;
		lastReceived.put(nanoseconds, Arrays.asList(new Message("@sys.foo", data)));

		Map<String, List<JSystemData>> mapped = mapper.mapReceivedData(lastReceived);
		JSystemData bar = mapped.get("@sys.foo.bar").get(0);
		assertEquals(1, bar.getValue().size());
		assertEquals(42, bar.getValue().get(0));
		assertEquals(String.valueOf(now), bar.getTimestamp());

		JSystemData baz = mapped.get("@sys.foo.baz").get(0);
		assertEquals(1, baz.getValue().size());
		assertEquals("Hello", baz.getValue().get(0));
		assertEquals(String.valueOf(now), baz.getTimestamp());

	}

	@Test
	public void collects_successive_communications() {
		Map<Long, List<Message>> lastReceived = new HashMap<Long, List<Message>>();

		long now = System.currentTimeMillis();
		Long comm1 = now * 1000;
		Map<String, List<?>> data1 = new HashMap<String, List<?>>();
		data1.put("bar", Arrays.asList(42));
		lastReceived.put(comm1, Arrays.asList(new Message("@sys.foo", data1)));

		Long comm2 = (now + 5000) * 1000;
		Map<String, List<?>> data2 = new HashMap<String, List<?>>();
		data2.put("bar", Arrays.asList(43));
		lastReceived.put(comm2, Arrays.asList(new Message("@sys.foo", data2)));

		// Results should be sorted by decreasing timestamps
		Map<String, List<JSystemData>> mapped = mapper.mapReceivedData(lastReceived);
		JSystemData bar = mapped.get("@sys.foo.bar").get(0);
		assertEquals(1, bar.getValue().size());
		assertEquals(43, bar.getValue().get(0));
		assertEquals(String.valueOf(now + 5000), bar.getTimestamp());

		bar = mapped.get("@sys.foo.bar").get(1);
		assertEquals(1, bar.getValue().size());
		assertEquals(42, bar.getValue().get(0));
		assertEquals(String.valueOf(now), bar.getTimestamp());

	}

	@Test
	public void handles_null_results() {
		Map<String, List<JSystemData>> mapped = mapper.mapReceivedData(null);
		assertEquals(0, mapped.keySet().size());
	}

}
