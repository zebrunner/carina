/*
 * Copyright 2013-2015 QAPROSOFT (http://qaprosoft.com/).
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
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.android.recorder.utils.AdbExecutor;
import com.qaprosoft.carina.core.foundation.utils.factory.DeviceType.Type;

public class DevicePool
{
	private static final Logger LOGGER = Logger.getLogger(DevicePool.class);
	
	private static final ConcurrentHashMap<Long, Device> threadId2Device = new ConcurrentHashMap<Long, Device>();
	private static List<Device> devices = new ArrayList<Device>();

	public static synchronized void registerDevice() {
		
		String name = Configuration.get(Parameter.MOBILE_DEVICE_NAME);
		String type = Configuration.get(Parameter.MOBILE_DEVICE_TYPE);
		String os = Configuration.get(Parameter.MOBILE_PLATFORM_NAME);
		String osVersion = Configuration.get(Parameter.MOBILE_PLATFORM_VERSION);
		String udid = Configuration.get(Parameter.MOBILE_DEVICE_UDID);
		String seleniumServer = Configuration.get(Parameter.SELENIUM_HOST);
		
		Device device = new Device(name, type, os, osVersion, udid, seleniumServer);
		devices.add(device);
		LOGGER.info("Adding single device into the DevicePool: " + device.getName());		
	}
	
	public static synchronized void registerDevices() {
		if (Configuration.get(Parameter.BROWSER).equalsIgnoreCase(SpecialKeywords.MOBILE)) {
			registerDevice();
			return;
		}
		String params = Configuration.get(Parameter.MOBILE_DEVICES);
		if (params.isEmpty()) {
			LOGGER.debug("Parameter.MOBILE_DEVICES is empty. Skip devices registration.");
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
			LOGGER.info("Adding new device into the DevicePool: " + device.getName());
		}
	}
	
	public static synchronized Device registerDevice2Thread(Long threadId)
	{
		if (!Configuration.get(Parameter.BROWSER).equalsIgnoreCase(SpecialKeywords.MOBILE_POOL) &&
				!Configuration.get(Parameter.BROWSER).equalsIgnoreCase(SpecialKeywords.MOBILE)) {
			//return null for non mobile/mobile_pool tests 
			return null;
		}
		
		int count = 0;
		boolean found = false;
		Device freeDevice = null;
		while (++count<100 && !found) {
			for (Device device : devices) {
				LOGGER.info("Check device status for registration: " + device.getName());
				if (!threadId2Device.containsValue(device)) {
						//current thread doesn't have ignored devices
						LOGGER.info("identified free non-ingnored device: " + device.getName());
						freeDevice = device;
						found = true;
						break;						
				}
			}
			if (!found) {
				int sec = Configuration.getInt(Parameter.INIT_RETRY_INTERVAL);
				LOGGER.warn("There is no free device, wating " + sec + " sec... attempt: " + count);
				pause(sec);
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
	
	public static synchronized Device getDevice() {
		Device device = null;
		if (!Configuration.get(Parameter.BROWSER).equalsIgnoreCase(SpecialKeywords.MOBILE_POOL) &&
				!Configuration.get(Parameter.BROWSER).equalsIgnoreCase(SpecialKeywords.MOBILE)) {
			return null;
		}
		long threadId = Thread.currentThread().getId();
		if (threadId2Device.containsKey(threadId)) {
			device = threadId2Device.get(threadId);
			LOGGER.debug("Getting device '" + device.getName() + "' by thread '" + threadId + "'");
		}
		return device;
	}
	
	public static synchronized Device getDevice(String udid) {
		Device device = null;
		for (Device dev : devices) {
			if (dev.getUdid().equalsIgnoreCase(udid)) {
				device = dev;
				break;
			}
		}
		if (device == null) {
			throw new RuntimeException("Unable to find device by udid: " + udid + "!");
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
	}


	public static synchronized String getDeviceUdid() {
		String udid = Configuration.get(Parameter.MOBILE_DEVICE_UDID);
		if (Configuration.get(Parameter.BROWSER).equalsIgnoreCase(SpecialKeywords.MOBILE_POOL) ||
				Configuration.get(Parameter.BROWSER).equalsIgnoreCase(SpecialKeywords.MOBILE)) {
			Device device = DevicePool.getDevice();
			if (device == null) {
				throw new RuntimeException("Unable to find device by thread!");
			}
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

	public static Type getDeviceType() {
		//specify default value based on existing _config.properties parameters
		Type type = Type.DESKTOP;		
		
		if (Configuration.get(Parameter.BROWSER).equalsIgnoreCase(SpecialKeywords.MOBILE_POOL) ||
				Configuration.get(Parameter.BROWSER).equalsIgnoreCase(SpecialKeywords.MOBILE)) {
			if (Configuration.get(Parameter.MOBILE_PLATFORM_NAME).equalsIgnoreCase(SpecialKeywords.ANDROID)) {
				type = Type.ANDROID_PHONE;
			}
			if (Configuration.get(Parameter.MOBILE_PLATFORM_NAME).equalsIgnoreCase(SpecialKeywords.IOS)) {
				type = Type.IOS_PHONE;
			}
		}
		
		Device device = getDevice();
		if (device != null) {
			type = device.getType();
		} else {
			LOGGER.error("Unable to get device type! 'DESKTOP' type will be returned by default!");
		}
		return type;
	}
	
	public static void screensOn(AdbExecutor executor) {
		for (Device device : devices) {
			executor.screenOn(device.getUdid());
		}
	}
	
	public static void screensOff(AdbExecutor executor) {
		for (Device device : devices) {
			executor.screenOff(device.getUdid());
		}
	}
}
