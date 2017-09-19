package com.qaprosoft.carina.grid.integration;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.zafira.client.STFClient;
import com.qaprosoft.zafira.models.stf.Devices;
import com.qaprosoft.zafira.models.stf.Response;
import com.qaprosoft.zafira.models.stf.STFDevice;

/**
 * Singleton for STF client.
 * 
 * @author akhursevich
 */
public class STF
{
	private static Logger LOGGER = LoggerFactory.getLogger(STF.class);

	private static final Long STF_TIMEOUT = 3600L;
	
	private STFClient client;
	
	private static boolean running = false;
	
	public final static STF INSTANCE = new STF();
	
	private STF()
	{
		String serviceURL = System.getProperty("STF_URL");
		String authToken = System.getProperty("STF_TOKEN");
		if (!StringUtils.isEmpty(serviceURL) && !StringUtils.isEmpty(authToken))
		{
			this.client = new STFClient(serviceURL, authToken);
			if(this.client.getAllDevices().getStatus() == 200)
			{
				running = true;
				LOGGER.info("STF connection established");
			}
			else
			{
				LOGGER.error("STF connection error");
			}
		}
		else
		{
			LOGGER.error("Set STF_URL and STF_TOKEN to use STF integration");
		}
    }
	
	public static boolean isRunning()
	{
		return running;
	}
	
	public static boolean isDeviceAvailable(String udid)
	{
		boolean available = false;
		if(isRunning())
		{
			Response<Devices> rs = INSTANCE.client.getAllDevices();
			if(rs.getStatus() == 200)
			{
				for(STFDevice device : rs.getObject().getDevices())
				{
					if(udid.equals(device.getSerial()))
					{
						available = device.getPresent() && device.getReady() && !device.getUsing() && device.getOwner() == null;
						break;
					}
				}
			}
			else
			{
				LOGGER.error("Unable to get devices status HTTP status: " + rs.getStatus());
			}
		}
		return available;
	}
	
	public synchronized static boolean reserveDevice(String udid)
	{
		return INSTANCE.client.reserveDevice(udid,  TimeUnit.SECONDS.toMillis(STF_TIMEOUT));		
	}
	
	public synchronized static boolean returnDevice(String udid)
	{
		return INSTANCE.client.remoteDisconnectDevice(udid) && INSTANCE.client.returnDevice(udid);
	}
}