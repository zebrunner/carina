package com.qaprosoft.carina.core.foundation.grid;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.openqa.selenium.support.ui.FluentWait;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;
import com.qaprosoft.carina.core.foundation.grid.GridRequest.Operation;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

/**
 * DeviceGrid communicates over PubNub with grid queue and provides connect/diconnect device functionality.
 * 
 * @author Alex Khursevich
 */
public class DeviceGrid {
	
	private static final Logger LOGGER = Logger.getLogger(DeviceGrid.class);
	
	private static final String GRID_SESSION_ID = UUID.randomUUID().toString();
	
	private static Pubnub heartbeat;
	
	/**
	 * Connect to remote mobile device.
	 * @param testId - unique test id generated bu UUID
	 * @param deviceModels - list of possible devices to get from STF
	 * @return device udid
	 */
	public static String connectDevice(String testId, List<String> deviceModels) 
	{
		Pubnub punub = new Pubnub(Configuration.get(Parameter.ZAFIRA_GRID_PKEY), Configuration.get(Parameter.ZAFIRA_GRID_SKEY));
		GridCallback gridCallback = new GridCallback(testId);
		try {
//			startHeartBeat();
			punub.subscribe(Configuration.get(Parameter.ZAFIRA_GRID_CHANNEL), gridCallback);
			
			GridRequest rq = new GridRequest(GRID_SESSION_ID, testId, deviceModels, Operation.CONNECT);
			punub.publish(Configuration.get(Parameter.ZAFIRA_GRID_CHANNEL), new JSONObject(new ObjectMapper().writeValueAsString(rq)), new Callback() {});
			
			new FluentWait<GridCallback>(gridCallback)
				.withTimeout(10, TimeUnit.MINUTES)
				.pollingEvery(10, TimeUnit.SECONDS)
				.until(new Function<GridCallback, Boolean>() 
				{
					@Override
					public Boolean apply(GridCallback callback) 
					{
						return !StringUtils.isEmpty(callback.getUdid());
					}
				});
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			punub.unsubscribeAll();
		}
		
		return gridCallback.getUdid();
	}
	
	/**
	 * Disconnects from remote device.
	 * @param testId - unique test id generated bu UUID
	 * @param udid - device udid to disconnect from
	 */
	public static void disconnectDevice(final String testId, String udid) 
	{
		try
		{
			GridRequest rq = new GridRequest(GRID_SESSION_ID, testId, udid, Operation.DISCONNECT);
			Pubnub punub = new Pubnub(Configuration.get(Parameter.ZAFIRA_GRID_PKEY), Configuration.get(Parameter.ZAFIRA_GRID_SKEY));
			punub.publish(Configuration.get(Parameter.ZAFIRA_GRID_CHANNEL), new JSONObject(new ObjectMapper().writeValueAsString(rq)), new Callback() {});
		} catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	public static void startHeartBeat() throws PubnubException
	{
		if(heartbeat == null)
		{
			heartbeat = new Pubnub(Configuration.get(Parameter.ZAFIRA_GRID_PKEY), Configuration.get(Parameter.ZAFIRA_GRID_SKEY));
			heartbeat.setHeartbeat(120);
			heartbeat.setHeartbeatInterval(60);
			heartbeat.setUUID(GRID_SESSION_ID);
			heartbeat.subscribe(Configuration.get(Parameter.ZAFIRA_GRID_CHANNEL), new Callback(){});
		}
	}
	
	public static void stopHeartBeat()
	{
		if(heartbeat != null)
		{
			heartbeat.unsubscribeAllChannels();
		}
	}
	
	public static class GridCallback extends Callback
	{
		private String udid;
		private String testId;
		
		public GridCallback(String testId)
		{
			this.testId = testId;
		}

		@Override
		public void successCallback(String channel, Object message)
		{

			String json = ((JSONObject) message).toString();
			if (json.contains(testId) && json.contains("connected"))
			{
				try
				{
					GridResponse rs = new ObjectMapper().readValue(json, GridResponse.class);
					if (rs.isConnected())
					{
						udid = rs.getSerial();
						LOGGER.info("Device found in grid by UDID: " + udid);
					}
				} catch (Exception e)
				{
					LOGGER.error(e.getMessage(), e);
				}
			}
		}

		public String getUdid()
		{
			return udid;
		}
	}
}
