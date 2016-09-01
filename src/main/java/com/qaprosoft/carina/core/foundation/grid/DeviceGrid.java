package com.qaprosoft.carina.core.foundation.grid;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.support.ui.FluentWait;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.qaprosoft.carina.core.foundation.grid.GridRequest.Operation;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

public class DeviceGrid {
	
	private static final Logger LOGGER = Logger.getLogger(DeviceGrid.class);
	String[] udid = {""};
	final StringBuilder udid2 = new StringBuilder("");
	
	public synchronized String findDevice(final String testId, List<String> deviceModels) {
		
		
		final ObjectMapper mapper = new ObjectMapper();
		
		Pubnub punub = new Pubnub(Configuration.get(Parameter.ZAFIRA_GRID_PKEY), Configuration.get(Parameter.ZAFIRA_GRID_SKEY));
		try {
			punub.subscribe(Configuration.get(Parameter.ZAFIRA_GRID_CHANNEL), new Callback() {
				/*@Override
				public void failedCallback(String channel, Object message) {
				}*/
				@Override
				public void successCallback(String channel, Object message) {
					System.out.println(message.toString());
					/*if ( message instanceof JSONArray) {
						System.out.println("wtf");
					}
					if(!(message instanceof JSONObject))
						return;*/
					String json = ((JSONObject) message).toString();
					if (json.contains(testId) && json.contains("connected")) {
						try {
							System.out.println("1");
							GridResponse rs = mapper.readValue(json, GridResponse.class);
							System.out.println("2");
							if(rs.isConnected())
							{
								System.out.println("3");
								System.out.println(rs.getSerial());
								udid[0] = rs.getSerial();
								udid2.setLength(0);
								udid2.append(rs.getSerial());
								System.out.println("4");
							}
						} catch (Exception e) {
							System.out.println("5");
							LOGGER.error(e.getMessage(), e);
						}
					}
				};
			});

			GridRequest rq = new GridRequest();
			rq.setTestId(testId);
			rq.setModels(deviceModels);
			rq.setOperation(Operation.CONNECT);
			punub.publish(Configuration.get(Parameter.ZAFIRA_GRID_CHANNEL), new JSONObject(mapper.writeValueAsString(rq)), new Callback() { });

/*			Thread.sleep(1000 * 15);
			
			int i = 0;
			boolean found = false;
			while (i++ < 100 && !found) {
				found = !StringUtils.isEmpty(udid[0]);
				found = !StringUtils.isEmpty(udid2);
				Thread.sleep(1000 * 10);
			}*/
			//Thread.sleep(1000 * 15);
			System.out.println(udid[0]);
			System.out.println(udid2.toString());
			new FluentWait<String>(udid2.toString()).withTimeout(1, TimeUnit.MINUTES).pollingEvery(10, TimeUnit.SECONDS)
			.until(new Function<String, Boolean>() {
				@Override
				public Boolean apply(String udid) {
					return !StringUtils.isEmpty(udid);
				}
			});
			
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			punub.unsubscribeAll();
		}

		if(!StringUtils.isEmpty(udid2.toString())) {
			LOGGER.info("Free deice udid from grid: " + udid2.toString());
		}
		
		if(!StringUtils.isEmpty(udid[0])) {
			LOGGER.info("Free deice udid from grid: " + udid[0]);
		}
		return udid[0];
	}
}
