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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In memory thread safe implementation of {@link StoreService}. Will discard the oldest messages if the maximum of message per client is reached.
 */
public class InMemoryStoreService implements StoreService {

	private static final Logger LOG = LoggerFactory.getLogger(InMemoryStoreService.class);

	/** maximum messages in the queue for a client */
	private int maxMessage;

	/**
	 * Create a store service with a limit of number of stored message per client.
	 * 
	 * @param maxMessage
	 *            the maximum message stored for a unique client
	 */
	public InMemoryStoreService(int maxMessage) {
		super();
		this.maxMessage = maxMessage;
	}

	private Map<String /* client id */, Map<Long /* reception nanos date */, List<Message>>> receivedData = new ConcurrentHashMap<String, Map<Long, List<Message>>>();

	private Map<String /* client id */, AtomicInteger /* number of waiting data per client */> receivedDataCounter = new ConcurrentHashMap<String, AtomicInteger>();

	@Override
	public void enqueueReceivedData(String clientId, long receptionInNanoSec, List<Message> newData) {
		LOG.debug("enqueueReceivedData( clientId = {}, receptionInnanoSec = {}, newData = {} )", clientId, receptionInNanoSec, newData);

		Map<Long, List<Message>> msgQueue;
		synchronized (this) {
			msgQueue = receivedData.get(clientId);
			if (msgQueue == null) {
				/** we use the SkipListMap because we need to remove the element in natural key order */
				msgQueue = new ConcurrentSkipListMap<Long, List<Message>>();
				receivedData.put(clientId, msgQueue);
				// initialize data counter
				receivedDataCounter.put(clientId, new AtomicInteger(0));
			}
		}

		msgQueue.put(receptionInNanoSec, newData);

		// check if we have too much received data
		AtomicInteger counter = receivedDataCounter.get(clientId);
		int delta = counter.incrementAndGet() - maxMessage;
		if (delta > 0) {
			// we should purge some message
			Iterator<Entry<Long, List<Message>>> iterator = msgQueue.entrySet().iterator();
			int size;
			do {
				iterator.next();
				iterator.remove();
				size = counter.decrementAndGet();
			} while (size > maxMessage);
		}
	}

	@Override
	public Map<Long, List<Message>> lastReceivedData(String clientId) {
		LOG.debug("lastReceivedData( clientid = {} )", clientId);
		return receivedData.get(clientId);
	}

	@Override
	public void start() {
		LOG.debug("start");
		// nothing to do here
	}

	@Override
	public void stop() {
		LOG.debug("stop");
		// nothing to do here
	}
}
