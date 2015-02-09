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
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

public class DevicePool
{
	private static final Logger LOGGER = Logger.getLogger(DevicePool.class);
	
	private static final ConcurrentHashMap<Long, Device> threadId2Device = new ConcurrentHashMap<Long, Device>();
	private static final ConcurrentHashMap<Long, List<Device>> threadId2IgnoredDevices = new ConcurrentHashMap<Long, List<Device>>();
	private static List<Device> devices = new ArrayList<Device>();

	public static synchronized void registerDevices() {
		String params = Configuration.get(Parameter.MOBILE_DEVICES);
		if (params.isEmpty()) {
			LOGGER.info("Parameter.MOBILE_DEVICES is empty. Skip devices registration.");
			return;
		}
		if (devices.size() > 0) {
			//already registered
			LOGGER.info("devices are already registered. Count is: " + devices.size());
			return;
		}
		//TODO: implement 1) device status verification; 2) adjustments of thread numbers
		String[] devicesArgs = params.split(";");
		for (int i=0; i<devicesArgs.length; i++) {
			if (devicesArgs[i].isEmpty()) {
				continue;
			}
			Device device = new Device(devicesArgs[i]);
			devices.add(device);
			LOGGER.info("Adding new device into the list: " + device.getName());
		}
	}
	
	public static synchronized Device registerDevice2Thread(Long threadId)
	{
		if (Configuration.get(Parameter.MOBILE_DEVICES).isEmpty()) {
			return null;
		}
		
		int count = 0;
		boolean found = false;
		Device freeDevice = null;
		while (++count<100 && !found) {
			for (Device device : devices) {
				LOGGER.info("Check device status for registration: " + device.getName());
				if (!threadId2Device.containsValue(device)) {
					if (!threadId2IgnoredDevices.containsKey(threadId)) {
						//current thread doesn't have ignored devices
						LOGGER.info("identified free non-ingnored device: " + device.getName());
						freeDevice = device;
						found = true;
						break;						
					} else if (!threadId2IgnoredDevices.get(threadId).contains(device)) {
						LOGGER.info("identified free non-ingnored device: " + device.getName());
						freeDevice = device;
						found = true;
						break;
					} else {
						//additional verification onto the count of the ignored devices. If all of them are added into ignored list then choose any again
						if (devices.size() == threadId2IgnoredDevices.get(threadId).size()) {
							LOGGER.info("device from ignored list will be added as all devices are ignored!");
							freeDevice = device;
							found = true;
							break;							
						}
						LOGGER.info("Unable to register device as it is in the ignored pool!");
					}
				}
			}
			if (!found) {
				LOGGER.warn("There is no free device, wating 3 min... attempt: " + count);
				pause(180);
			}
		}
		
		if (freeDevice != null) {
			threadId2Device.put(threadId, freeDevice);
			LOGGER.info("Registering device '" + freeDevice.getName() + "' with thread '" + threadId + "'");
		} else {
			throw new RuntimeException("Unable to find available device after '" + count + "' attempts!");	
		}
		return freeDevice;

	}
	
	public static synchronized void ignoreDevice()
	{
		Long threadId = Thread.currentThread().getId();
		Device device = getDeviceByThread(threadId);
		ignoreDevice(threadId, device);
	}
	
	public static synchronized void ignoreDevice(Long threadId, Device device)
	{
		if (Configuration.get(Parameter.MOBILE_DEVICES).isEmpty()) {
			return;
		}
		if (devices.size() <= 1) {
			LOGGER.info("Unable to irnore single device!");
			return;
		}
		if (device == null) {
			return;
		}
		
		LOGGER.info("Put device '" + device.getName() + "' into ignored list.");
		List<Device> devices = new ArrayList<Device>(); 
		if (threadId2IgnoredDevices.containsKey(threadId)) {
			devices = threadId2IgnoredDevices.get(threadId);
		}
		devices.add(device);
		
		threadId2IgnoredDevices.put(threadId, devices);	
	}
	
	
	public static Device getDeviceByThread(long threadId)
	{
		Device device = null;
		if (threadId2Device.containsKey(threadId)) {
			device = threadId2Device.get(threadId);
			LOGGER.info("Getting device '" + device.getName() + "' by thread '" + threadId + "'");
		}
		return device;
	}
	
	public static synchronized void deregisterDeviceByThread(long threadId)
	{
		if (threadId2Device.containsKey(threadId)) {
			Device device = threadId2Device.get(threadId);
			
			threadId2Device.remove(threadId);
			LOGGER.info("Deregistering device '" + device.getName() + "' with thread '" + threadId + "'");
		}
		
		if (threadId2IgnoredDevices.containsKey(threadId)) {
			LOGGER.info("Deregistering all ignored devices for thread '" + threadId + "'");
			threadId2IgnoredDevices.remove(threadId);
		}
	}

	public static synchronized Device getDevice() {
		Device device = null;
		if (Configuration.get(Parameter.BROWSER).equalsIgnoreCase(SpecialKeywords.MOBILE_POOL)) {
			device = DevicePool.getDeviceByThread(Thread.currentThread().getId());
		} 
		return device;
	}
	
	public static synchronized String getDeviceUdid() {
		String udid = Configuration.get(Parameter.MOBILE_DEVICE_UDID);
		if (Configuration.get(Parameter.BROWSER).equalsIgnoreCase(SpecialKeywords.MOBILE_POOL)) {
			Device device = DevicePool.getDeviceByThread(Thread.currentThread().getId());
			udid = device.getUdid();
		} 
		
		return udid;
	}
	
	/**
	 * Pause for specified timeout.
	 * 
	 * @param timeout
	 *            in seconds.
	 */

	private static void pause(long timeout) {
		try {
			Thread.sleep(timeout * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
