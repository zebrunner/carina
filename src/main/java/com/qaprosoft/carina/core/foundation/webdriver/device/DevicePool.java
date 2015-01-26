/*
 * Copyright 2013 QAPROSOFT (http://qaprosoft.com/).
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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

public class DevicePool
{
	private static final Logger LOGGER = Logger.getLogger(DevicePool.class);
	
	private static final ConcurrentHashMap<Long, Device> threadId2Device = new ConcurrentHashMap<Long, Device>();
	private static List<Device> devices = new ArrayList<Device> ();

	public static synchronized void registerDevices() {
		String params = Configuration.get(Parameter.MOBILE_DEVICES);
		if (params.isEmpty()) {
			LOGGER.debug("Parameter.MOBILE_DEVICES is empty. Skip devices registration.");
			return;
		}
		if (devices.size() > 0) {
			//already registered
			LOGGER.debug("devices are already registered. Count is: " + devices.size());
			return;
		}
		//TODO: implement 1) device status verification; 2) adjustments of thread numbers
		String[] devicesArgs = params.split(";");
		for (int i=0; i<devicesArgs.length; i++) {
			Device device = new Device(devicesArgs[i]);
			devices.add(device);
			LOGGER.debug("Adding new device into the list: " + device.getName());
		}
	}
	
	public static synchronized Device registerDevice2Thread(Long threadId)
	{
		if (Configuration.get(Parameter.MOBILE_DEVICES).isEmpty()) {
			return null;
		}
		
		for (Device device : devices) {
			if (!threadId2Device.containsValue(device)) {
				threadId2Device.put(threadId, device);
				LOGGER.debug("Registering device '" + device.getName() + "' with thread '" + threadId + "'");
				return device;
			}
		}
		throw new RuntimeException("There are not available devices!"); 
	}
	
	public static Device getDeviceByThread(long threadId)
	{
		Device device = null;
		if (threadId2Device.containsKey(threadId)) {
			device = threadId2Device.get(threadId);
			LOGGER.debug("Getting device '" + device.getName() + "' by thread '" + threadId + "'");
		}
		return device;
	}
	
	public static synchronized void deregisterDeviceByThread(long threadId)
	{
		if (threadId2Device.containsKey(threadId)) {
			Device device = threadId2Device.get(threadId);
			
			threadId2Device.remove(threadId);
			LOGGER.debug("Deregistering device '" + device.getName() + "' with thread '" + threadId + "'");
		}
	}
}
