/*
 * Copyright 2013-2016 QAPROSOFT (http://qaprosoft.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qaprosoft.carina.core.foundation.webdriver.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.grid.DeviceGrid;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.factory.DeviceType.Type;

public class DevicePool
{
	private static final Logger LOGGER = Logger.getLogger(DevicePool.class);
	
	private static final Pattern HOST_PATTERN = Pattern.compile(".*http:\\/\\/(.*):.*");
	
	private static final Map<Long, Device> THREAD_2_DEVICE_MAP = Collections.synchronizedMap(new HashMap<Long, Device>());
	
	private static final List<Device> DEVICES = Collections.synchronizedList(new ArrayList<Device>());
	private static final List<String> DEVICE_MODELS = Collections.synchronizedList(new ArrayList<String>());
	
	private static final boolean GRID_ENABLED = Configuration.getBoolean(Parameter.ZAFIRA_GRID_ENABLED);
	
	public static synchronized void registerDevice() 
	{
		Device device = new Device(Configuration.get(Parameter.MOBILE_DEVICE_NAME), 
				                   Configuration.get(Parameter.MOBILE_DEVICE_TYPE), 
				                   Configuration.get(Parameter.MOBILE_PLATFORM_NAME), 
				                   Configuration.get(Parameter.MOBILE_PLATFORM_VERSION), 
				                   Configuration.get(Parameter.MOBILE_DEVICE_UDID), 
				                   Configuration.get(Parameter.SELENIUM_HOST));
		DEVICES.add(device);
		DEVICE_MODELS.add(device.getName());
		String msg = "Registered single device into the DevicePool: %s - %s";
		LOGGER.info(String.format(msg, device.getName(), device.getUdid()));		
	}
	
	public static synchronized void unregisterDevice(Device device) 
	{
		if (device == null) 
		{
			LOGGER.error("Unable to unregister NULL device!");
			return;
		}
		DEVICES.remove(device);
		DEVICE_MODELS.remove(device.getName());
		LOGGER.info("Removed device from the DevicePool: " + device.getName());		
	}
	

	public static synchronized void registerDevices() 
	{
		if (Configuration.get(Parameter.DRIVER_TYPE).equalsIgnoreCase(SpecialKeywords.MOBILE)) 
		{
			registerDevice();
			return;
		}
		
		if (!CollectionUtils.isEmpty(DEVICES)) 
		{
			LOGGER.info("Devices are already registered. Count is: " + DEVICES.size());
			return;
		}
		
		String params = Configuration.get(Parameter.MOBILE_DEVICES);
		if (params.isEmpty()) 
		{
			LOGGER.debug("Parameter.MOBILE_DEVICES is empty. Skip devices registration.");
			return;
		}
		//TODO: implement 1) device status verification; 2) adjustments of thread numbers
		for(String args : params.split(";")) {
			if(args.isEmpty()) 
			{
				continue;
			}
			Device device = new Device(args);
			DEVICES.add(device);
			DEVICE_MODELS.add(device.getName());
			String msg = "Added device into the DevicePool: %s - %s. Devices count: %s";
			LOGGER.info(String.format(msg, device.getName(), device.getUdid(), DEVICES.size()));
		}
	}
	
	public static Device registerDevice2Thread()
	{
		Long threadId = Thread.currentThread().getId();
		if (!Configuration.get(Parameter.DRIVER_TYPE).equalsIgnoreCase(SpecialKeywords.MOBILE_POOL) &&
			!Configuration.get(Parameter.DRIVER_TYPE).equalsIgnoreCase(SpecialKeywords.MOBILE)) 
		{
			return null;
		}

		final String testId = UUID.randomUUID().toString();
		Device freeDevice = null;
		if (GRID_ENABLED) 
		{
			//TODO: handle the case when STF returned device which is already used and exists in THREAD_2_DEVICE_MAP 
			final String udid = DeviceGrid.connectDevice(testId, DEVICE_MODELS);
			if (!StringUtils.isEmpty(udid)) 
			{
				for (Device device : DEVICES) 
				{
					if (device.getUdid().equalsIgnoreCase(udid)) 
					{
						if (THREAD_2_DEVICE_MAP.containsValue(device)) {
							String msg = "STF Grid returned busy device as it exists in THREAD_2_DEVICE_MAP: %s - %s!";
							throw new RuntimeException(String.format(msg,  device.getName(), device.getUdid()));
						}
						device.setTestId(testId);
						freeDevice = device;
						break;
					}
				}
			}
		}
		else
		{
			int count = 0;
			boolean found = false;
			while (++count<100 && !found) {
				for (Device device : DEVICES) {
					if (!THREAD_2_DEVICE_MAP.containsValue(device)) {
							//current thread doesn't have ignored devices
							device.setTestId(testId);
							freeDevice = device;
							found = true;
							break;						
					}
				}
				if (!found) {
					int sec = Configuration.getInt(Parameter.INIT_RETRY_INTERVAL);
					//System.out.println("There is no free device, wating " + sec + " sec... attempt: " + count);
					pause(sec);
				}
			}
		}

		if (freeDevice != null) {
			THREAD_2_DEVICE_MAP.put(threadId, freeDevice);
			String msg = "found device %s-%s for test %s";
			LOGGER.info(String.format(msg, freeDevice.getName(), freeDevice.getUdid(), testId));
		} else {
			//TODO: improve loggers about device type, family etc
			String msg = "Not found free device for thread: %s among registered pool of %s devices!";
			throw new RuntimeException(String.format(msg, threadId, DEVICES.size()));	
		}
		
		return freeDevice;

	}	
	
	public static Device getDevice() 
	{
		Device device = null;
		if (!Configuration.get(Parameter.DRIVER_TYPE).equalsIgnoreCase(SpecialKeywords.MOBILE_POOL) &&
		    !Configuration.get(Parameter.DRIVER_TYPE).equalsIgnoreCase(SpecialKeywords.MOBILE)) 
		{
			return null;
		}
		long threadId = Thread.currentThread().getId();
		if (THREAD_2_DEVICE_MAP.containsKey(threadId)) 
		{
			device = THREAD_2_DEVICE_MAP.get(threadId);
		}
		return device;
	}
	
	public static Device getDevice(String udid) {
		Device device = null;
		for (Device dev : DEVICES) 
		{
			if (dev.getUdid().equalsIgnoreCase(udid)) 
			{
				device = dev;
				break;
			}
		}
		if (device == null) 
		{
			String msg = "Not found device by udid among registered pool of %s devices!";
			throw new RuntimeException(String.format(msg, udid, DEVICES.size()));
		}
		return device;
	}
	
	
	public static void deregisterDeviceFromThread()
	{
		Long threadId = Thread.currentThread().getId();
		if (THREAD_2_DEVICE_MAP.containsKey(threadId)) 
		{
			Device device = THREAD_2_DEVICE_MAP.get(threadId);
			if(GRID_ENABLED)
			{
				DeviceGrid.disconnectDevice(device.getTestId(), device.getUdid());
			}
			THREAD_2_DEVICE_MAP.remove(threadId);
			String msg = "Disconnected device '%s - %s' for test '%s'; thread '%s'"; 
			LOGGER.info(String.format(msg,  device.getName(), device.getUdid(), device.getTestId(), threadId));
		}
	}


	public static String getDeviceUdid() 
	{
		String udid = Configuration.get(Parameter.MOBILE_DEVICE_UDID);
		if (Configuration.get(Parameter.DRIVER_TYPE).equalsIgnoreCase(SpecialKeywords.MOBILE_POOL) ||
				Configuration.get(Parameter.DRIVER_TYPE).equalsIgnoreCase(SpecialKeywords.MOBILE)) 
		{
			Device device = DevicePool.getDevice();
			if (device == null) {
				throw new RuntimeException("Unable to find device by thread!");
			}
			udid = device.getUdid();
		} 
		return udid;
	}
	


	public static Type getDeviceType() 
	{
		// specify default value based on existing _config.properties parameters
		Type type = Type.DESKTOP;
		Device device = getDevice();
		if (device != null) {
			type = device.getType();
		} 
		else 
		{
			if (Configuration.get(Parameter.DRIVER_TYPE).equalsIgnoreCase(SpecialKeywords.MOBILE_POOL)
					|| Configuration.get(Parameter.DRIVER_TYPE).equalsIgnoreCase(SpecialKeywords.MOBILE)) 
			{
				if (Configuration.get(Parameter.MOBILE_PLATFORM_NAME).equalsIgnoreCase(SpecialKeywords.ANDROID)) 
				{
					type = Type.ANDROID_PHONE;
				}
				if (Configuration.get(Parameter.MOBILE_PLATFORM_NAME).equalsIgnoreCase(SpecialKeywords.IOS)) 
				{
					type = Type.IOS_PHONE;
				}
			} else 
			{
				LOGGER.error("Unable to get device type! 'DESKTOP' type will be returned by default!");
			}
		}
		return type;
	}
	
	/**
	 * Check if system is distributed (devices are connected to different servers)
	 * @return
	 */
	public static boolean isSystemDistributed() 
	{
		boolean result = false;
		for (int i = 0; i < DEVICES.size() - 1; i++) 
		{
			if (!getServer(DEVICES.get(i).getSeleniumServer()).equals(getServer(DEVICES.get(i + 1).getSeleniumServer()))) 
			{
				result = true;
			}
		}
		return result;
	}
	
	public static String getServer(final String server) {
		Matcher matcher = HOST_PATTERN.matcher(server);
		String serverValue;
		if (matcher.find()) {
			serverValue = matcher.group(1);
		} else {
			serverValue = server.split(":")[0];
		}
		return serverValue;
	}
	
	public static String getServer() {
		return getServer(getDevice().getSeleniumServer());
	}
	
	/**
	 * Pause for specified timeout.
	 * 
	 * @param timeout
	 *            in seconds.
	 */
	private static void pause(long timeout) 
	{
		try {
			Thread.sleep(timeout * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}