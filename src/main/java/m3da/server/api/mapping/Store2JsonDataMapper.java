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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import m3da.server.api.json.JSystemData;
import m3da.server.store.Message;

/**
 * Mapping between m3da.server.store beans and JSON representations.
 */
public class Store2JsonDataMapper {

	/**
	 * Maps a hashmap of received data (organized by nanoseconds, with a list of m3da.store.bean.Messages) into a map of JSystemData, organized by
	 * data id.
	 * 
	 * Time stamps are converted from nanoseconds (m3da message timestamp) to milliseconds (JSON timestamps.)
	 * 
	 * @param lastReceived
	 */
	public Map<String, List<JSystemData>> mapReceivedData(Map<Long, List<Message>> data) {

		Map<String, List<JSystemData>> res = new HashMap<String, List<JSystemData>>();

		if (data == null) {
			return res;
		}

		for (Map.Entry<Long, List<Message>> e : data.entrySet()) {

			Long nanoseconds = e.getKey();
			String timestampInSeconds = String.valueOf(nanoseconds / 1000);

			for (Message message : e.getValue()) {

				String path = message.getPath();
				Map<String, List<?>> pathData = message.getData();

				for (Map.Entry<String, List<?>> received : pathData.entrySet()) {

					String key = received.getKey();

					String dataId = path + "." + key;
					List<JSystemData> resData = null;
					if (res.containsKey(dataId)) {
						resData = res.get(dataId);
					} else {
						resData = new ArrayList<JSystemData>();
						res.put(dataId, resData);
					}

					JSystemData jSystemData = new JSystemData();
					jSystemData.setTimestamp(timestampInSeconds);

					jSystemData.setValue(this.transformStrings(received.getValue()));

					resData.add(jSystemData);

				}

			}

		}

		for (Map.Entry<String, List<JSystemData>> resEntry : res.entrySet()) {
			this.sortJSystemDataList(resEntry.getValue());
		}

		return res;

	}

	/**
	 * Sort a list of JSystemData, by decreasing time stamps.
	 * 
	 * @param jSystemDataList
	 */
	private void sortJSystemDataList(List<JSystemData> jSystemDataList) {
		Comparator<JSystemData> comp = new Comparator<JSystemData>() {
			@Override
			public int compare(JSystemData data1, JSystemData data2) {
				// data2 first to get decreasing timestamps
				return (data2.getTimestamp().compareTo(data1.getTimestamp()));
			}
		};
		Collections.sort(jSystemDataList, comp);
	}

	/**
	 * "Strings" from m3da are actually ByteBuffers ; we will assume all of them are utf-8 string.
	 * 
	 * @param values
	 * @return the list of values, with ByteBuffers converted to utf-8 strings
	 */
	private List<Object> transformStrings(List<?> values) {

		List<Object> res = new ArrayList<Object>();

		for (Object o : values) {
			if (o instanceof ByteBuffer) {
				String str = new String(((ByteBuffer) o).array(), Charset.forName("utf-8"));
				res.add(str);
			} else {
				res.add(o);
			}
		}

		return res;
	}

}
