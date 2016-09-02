package com.qaprosoft.carina.core.foundation.grid;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
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
	
	public static String findDevice(final String testId, List<String> deviceModels) 
	{
		Pubnub punub = new Pubnub(Configuration.get(Parameter.ZAFIRA_GRID_PKEY), Configuration.get(Parameter.ZAFIRA_GRID_SKEY));
		GridCallback gridCallback = new GridCallback(testId);
		try {
			punub.subscribe(Configuration.get(Parameter.ZAFIRA_GRID_CHANNEL), gridCallback);

			GridRequest rq = new GridRequest(testId, deviceModels, Operation.CONNECT);
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
