package com.qaprosoft.carina.core.foundation.grid;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.FluentWait;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class DeviceGrid
{

	private static final Logger LOGGER = Logger.getLogger(DeviceGrid.class);

	private static final String GRID_SESSION_ID = Configuration.get(Parameter.CI_RUN_ID) != null
			? Configuration.get(Parameter.CI_RUN_ID) : UUID.randomUUID().toString();

	private static final Integer GRID_HEARTBEAT_TIMEOUT = 120;
	private static final Integer GRID_HEARTBEAT_INTERVAL = 59;
	private static final Integer GRID_DEVICE_TIMEOUT = Configuration.getInt(Parameter.ZAFIRA_GRID_TIMEOUT);

	private static final String PKEY = Configuration.get(Parameter.ZAFIRA_GRID_PKEY);
	private static final String SKEY = Configuration.get(Parameter.ZAFIRA_GRID_SKEY);
	private static final String CHANNEL = Configuration.get(Parameter.ZAFIRA_GRID_CHANNEL);

	private static Pubnub heartbeat;

	/**
	 * Connect to remote mobile device.
	 * 
	 * @param testId
	 *            - unique test id generated bu UUID
	 * @param deviceModels
	 *            - list of possible devices to get from STF
	 * @return device udid
	 */
	public static String connectDevice(String testId, List<String> deviceModels)
	{
		Pubnub punub = new Pubnub(PKEY, SKEY);
		GridCallback gridCallback = new GridCallback(testId);
		GridRequest rq = new GridRequest(GRID_SESSION_ID, testId, deviceModels, Operation.CONNECT);
		try
		{
			startHeartBeat();
			punub.subscribe(CHANNEL, gridCallback);

			punub.publish(CHANNEL, toJsonObject(rq), new Callback()
			{
			});
// 			TODO: implement event logging
//			ZafiraIntegrator.logEvent(
//					new EventType(Type.REQUEST_DEVICE_CONNECT, GRID_SESSION_ID, testId, new Gson().toJson(rq)));

			new FluentWait<GridCallback>(gridCallback).withTimeout(GRID_DEVICE_TIMEOUT, TimeUnit.SECONDS)
					.pollingEvery(10, TimeUnit.SECONDS).until(callback -> !StringUtils.isEmpty(callback.getUdid()));
		} catch (TimeoutException e)
		{
// 			TODO: implement event logging
//			ZafiraIntegrator
//					.logEvent(new EventType(Type.DEVICE_WAIT_TIMEOUT, GRID_SESSION_ID, testId, new Gson().toJson(rq)));
			LOGGER.error(e.getMessage(), e);
		} catch (Exception e)
		{
			LOGGER.error(e.getMessage(), e);
		} finally
		{
			punub.unsubscribeAll();
		}

		return gridCallback.getUdid();
	}

	/**
	 * Disconnects from remote device.
	 * 
	 * @param testId
	 *            - unique test id generated bu UUID
	 * @param udid
	 *            - device udid to disconnect from
	 */
	public static void disconnectDevice(final String testId, String udid)
	{
		try
		{
			GridRequest rq = new GridRequest(GRID_SESSION_ID, testId, udid, Operation.DISCONNECT);
			Pubnub punub = new Pubnub(PKEY, SKEY);
			punub.publish(CHANNEL, toJsonObject(rq), new Callback()
			{
			});
// 			TODO: implement event logging
//			ZafiraIntegrator.logEvent(
//					new EventType(Type.REQUEST_DEVICE_DISCONNECT, GRID_SESSION_ID, testId, new Gson().toJson(rq)));
		} catch (Exception e)
		{
			LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * Starts heartbeat from the very first call, if test run aborted grid will automatically drop all connections
	 * requested from session.
	 * @throws PubnubException com.pubnub.api.PubnubException
	 */
	public static void startHeartBeat() throws PubnubException
	{
		if (heartbeat == null)
		{
			heartbeat = new Pubnub(PKEY, SKEY);
			heartbeat.setHeartbeat(GRID_HEARTBEAT_TIMEOUT);
			heartbeat.setHeartbeatInterval(GRID_HEARTBEAT_INTERVAL);
			heartbeat.setUUID(GRID_SESSION_ID);
			heartbeat.subscribe(CHANNEL, new Callback()
			{
			});
		}
	}

	public static void stopHeartBeat()
	{
		if (heartbeat != null)
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
// 						TODO: implement event logging
//						ZafiraIntegrator.markEventReceived(new EventType(Type.CONNECT_DEVICE, GRID_SESSION_ID, testId));
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

	private static JSONObject toJsonObject(Object object)
	{
		JSONObject json = null;
		try
		{
			json = new JSONObject(new ObjectMapper().writeValueAsString(object));
		} catch (Exception e)
		{
			LOGGER.error(e.getMessage());
		}
		return json;
	}
}
