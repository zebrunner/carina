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

import java.util.Map;

import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.factory.DeviceType.Type;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;

public class DevicePool
{
	private static final Logger LOGGER = Logger.getLogger(DevicePool.class);

	private static final Device nullDevice = new Device();
	
	private static ThreadLocal<Device> currentDevice = new ThreadLocal<Device>();

	public static Device registerDevice(Device device) {
		if (device == nullDevice) {
			//analyze if it is local mobile run
			//TODO: move "mobile.*" into constants keywords
			if (Configuration.get(Parameter.DRIVER_TYPE).equalsIgnoreCase(SpecialKeywords.MOBILE)) {
				device = new Device(R.CONFIG.get("mobile.deviceName"),
						Configuration.get(Parameter.MOBILE_DEVICE_TYPE),
						R.CONFIG.get("mobile.platformName"),
						R.CONFIG.get("mobile.platformName"), 
						R.CONFIG.get("mobile.udid"), 
						Configuration.get(Parameter.SELENIUM_HOST),
						"");
			}
		}
		
		//register current device to be able to transfer it into Zafira at the end of the test
		setDevice(device);
		Long threadId = Thread.currentThread().getId();
		LOGGER.info("register device fot current thread id: " + threadId + "; device: '" + device.getName() + "'");

		return device;
	}
	
	public static void registerDevice(Map<String, String> propertiesMap) {
        if (propertiesMap.get("core.driver_type").equals(SpecialKeywords.MOBILE)) {
        	//add device from custom capabilities to the devicePool
        	String deviceName = propertiesMap.get("core.mobile_device_name");
        	String deviceType = propertiesMap.get("core.mobile_device_type");
        	String devicePlatform = propertiesMap.get("core.mobile_platform_name");
        	String devicePlatformVersion = propertiesMap.get("core.mobile_platform_version");
        	String deviceUdid = propertiesMap.get("core.mobile_platform_udid");
        	if (deviceUdid == null) {
        		deviceUdid = "";
        	}
        	String seleniumServer = propertiesMap.get("core.selenium_host");
        	
        	setDevice(new Device(deviceName, deviceType, devicePlatform, devicePlatformVersion, deviceUdid, seleniumServer, ""));
        }
	}
	
	
	public static void deregisterDevice()
	{
		currentDevice.remove();
	}

	
	public static Device getDevice()
	{
		long threadId = Thread.currentThread().getId();
		String test = TestNamingUtil.getTestNameByThread();
		Device device = currentDevice.get();
		if (device == null) {
			LOGGER.info("Current device is null for test '" + test + "', thread: " + threadId);
			device = nullDevice;
		} else if (device.getName().isEmpty()) {
			LOGGER.info("Current device name is empty! nullDevice was used for test '" + test + "', thread: " + threadId);
		} else {
			LOGGER.info("Current device name is '" + device.getName() + "' for test '" + test + "', thread: " + threadId);
		}
		return device;
	}


	//TODO: move it to Device class
	public static Type getDeviceType()
	{
		// specify default value based on existing _config.properties parameters
		Type type = Type.DESKTOP;

		if (getDevice() != nullDevice)
		{
			type = getDevice().getType();
		} else
		{
			if (Configuration.get(Parameter.DRIVER_TYPE).equalsIgnoreCase(SpecialKeywords.MOBILE)
					|| Configuration.get(Parameter.DRIVER_TYPE).equalsIgnoreCase(SpecialKeywords.MOBILE_GRID))
			{
				if (Configuration.get(Parameter.MOBILE_PLATFORM_NAME).equalsIgnoreCase(SpecialKeywords.ANDROID))
				{
					type = Type.ANDROID_PHONE;
				}
				if (Configuration.get(Parameter.MOBILE_PLATFORM_NAME).equalsIgnoreCase(SpecialKeywords.IOS))
				{
					if (Configuration.get(Parameter.MOBILE_DEVICE_TYPE).equalsIgnoreCase(SpecialKeywords.TABLET))
					{
						type = Type.IOS_TABLET;
					} else
					{
						type = Type.IOS_PHONE;
					}
				}
			} else
			{
				LOGGER.error("Unable to get device type! 'DESKTOP' type will be returned by default!");
			}
		}
		return type;
	}

	public static Device getNullDevice() {
		return nullDevice;
	}

	private static void setDevice(Device device) {
		String test = TestNamingUtil.getTestNameByThread();
		long threadId = Thread.currentThread().getId();
		LOGGER.info("Set current device to '" + device.getName() + "' test '" + test + "', thread: " + threadId);
		currentDevice.set(device);
	}
	

}