package com.qaprosoft.carina.grid.integration;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

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
	private static Logger LOGGER = Logger.getLogger(STF.class.getName());

	private static final Long STF_TIMEOUT = 3600L;
	
	private STFClient client;
	
	private static boolean running = false;
	
	public final static STF INSTANCE = new STF();
	
	private STF()
	{
		String serviceURL = System.getProperty("STF_URL");
		String authToken = System.getProperty("STF_TOKEN");
		LOGGER.info("*********************************");
		LOGGER.info("Credentials for STF: " + serviceURL + " / " + authToken);
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
				LOGGER.info("STF connection error");
			}
		}
		else
		{
			LOGGER.info("Set STF_URL and STF_TOKEN to use STF integration");
		}
		LOGGER.info("*********************************");
    }
	
	public static boolean isRunning()
	{
		return running;
	}
	
	public static boolean isDeviceAvailable(String udid)
	{
		boolean available = false;
		if (isRunning()) {
			try {
				Response<Devices> rs = INSTANCE.client.getAllDevices();
				if (rs.getStatus() == 200) {
					for (STFDevice device : rs.getObject().getDevices()) {
						if (udid.equals(device.getSerial())) {
							available = device.getPresent() && device.getReady() && !device.getUsing()
									&& device.getOwner() == null;
							break;
						}
					}
				} else {
					LOGGER.info("Unable to get devices status HTTP status: " + rs.getStatus());
				}
			} catch (Exception e) {
				LOGGER.info("Unable to get devices status HTTP status via udid: " + udid);
			}
		}
		return available;
	}
	
	//TODO: why do we have boolean as return value here and below
	public static boolean reserveDevice(String udid)
	{
		return INSTANCE.client.reserveDevice(udid,  TimeUnit.SECONDS.toMillis(STF_TIMEOUT));		
	}
	
	public static boolean returnDevice(String udid)
	{
		//TODO: investigate why we had INSTANCE.client.remoteDisconnectDevice(udid)
		//return INSTANCE.client.remoteDisconnectDevice(udid) && INSTANCE.client.returnDevice(udid);
		return INSTANCE.client.returnDevice(udid);
	}
}