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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.factory.DeviceType.Type;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;

public class DevicePool
{
	private static final Logger LOGGER = Logger.getLogger(DevicePool.class);

	private static final Pattern HOST_PATTERN = Pattern.compile(".*http:\\/\\/(.*):.*");

	//private static final Map<Long, Device> THREAD_2_DEVICE_MAP = Collections.synchronizedMap(new HashMap<Long, Device>());

	private static final List<Device> DEVICES = Collections.synchronizedList(new ArrayList<Device>());

	private static final Device nullDevice = new Device("", "", "", "", "", "", "");
	
	private static ThreadLocal<Device> curDevice = new ThreadLocal<Device>();

	protected static synchronized void addDevice()
	{
		Device device = new Device(Configuration.get(Parameter.MOBILE_DEVICE_NAME),
				Configuration.get(Parameter.MOBILE_DEVICE_TYPE),
				Configuration.get(Parameter.MOBILE_PLATFORM_NAME),
				Configuration.get(Parameter.MOBILE_PLATFORM_VERSION),
				Configuration.get(Parameter.MOBILE_DEVICE_UDID),
				Configuration.get(Parameter.SELENIUM_HOST),
				"");
		DEVICES.add(device);
		String msg = "Registered single device into the DevicePool: %s - %s";
		LOGGER.info(String.format(msg, device.getName(), device.getUdid()));
	}

	public static synchronized List<Device> getDevices() {
		return DEVICES;
	}
	
	public static synchronized void addDevice(Device device) {
		DEVICES.add(device);
		String msg = "Registered single device into the DevicePool: %s - %s";
		LOGGER.info(String.format(msg, device.getName(), device.getUdid()));
	}
	
	public static synchronized void addDevice(Map<String, String> propertiesMap) {
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
        	
        	addDevice(new Device(deviceName, deviceType, devicePlatform, devicePlatformVersion, deviceUdid, seleniumServer, ""));
        }

	}
	
	public static synchronized void addDevices()
	{
		if (!Configuration.get(Parameter.DRIVER_TYPE).equalsIgnoreCase(SpecialKeywords.MOBILE)
				&& !Configuration.get(Parameter.DRIVER_TYPE).equalsIgnoreCase(SpecialKeywords.MOBILE_GRID)) {
			return;
		}
			
		if (Configuration.get(Parameter.DRIVER_TYPE).equalsIgnoreCase(SpecialKeywords.MOBILE))
		{
			addDevice();
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
		// TODO: implement for Enabled Grid
		// 1) device status verification
		// 2) adjustments of thread numbers
		for (String args : params.split(";"))
		{
			if (args.isEmpty())
			{
				continue;
			}
			Device device = new Device(args);
			DEVICES.add(device);
			String msg = "Added device into the DevicePool: %s - %s. Devices count: %s";
			LOGGER.info(String.format(msg, device.getName(), device.getUdid(), DEVICES.size()));
		}

		// if pool has single device then redefine device name/version etc parameter instead of abstract DevicesPool and Android 5-6...
		if (DEVICES.size() == 1)
		{
			Device device = DEVICES.get(0);
			LOGGER.info(String.format("Redefine mobile device settings using single device data: %s %s-%s",
					device.getName(), device.getOs(), device.getOsVersion()));

			// QAA-1574 Redefine particular phone details for single threaded mobile tests
			// redefine test execution logic onto the single thread and single device
			R.CONFIG.put(Parameter.DATA_PROVIDER_THREAD_COUNT.getKey(), "1");
			R.CONFIG.put(Parameter.THREAD_COUNT.getKey(), "1");

			R.CONFIG.put(Parameter.DRIVER_TYPE.getKey(), "mobile");

			R.CONFIG.put(Parameter.MOBILE_DEVICE_NAME.getKey(), device.getName());
			R.CONFIG.put(Parameter.MOBILE_PLATFORM_NAME.getKey(), device.getOs());
			R.CONFIG.put(Parameter.MOBILE_PLATFORM_VERSION.getKey(), device.getOsVersion());
			R.CONFIG.put(Parameter.MOBILE_DEVICE_UDID.getKey(), device.getUdid());
			R.CONFIG.put(Parameter.MOBILE_DEVICE_TYPE.getKey(), device.getType().toString());
		}
	}
	
	public static Device findDevice(String udid) {
		LOGGER.info("Looking for device with udid: " + udid);
		if (StringUtils.isEmpty(udid)) {
			throw new RuntimeException(String.format("Unable to find device from pool using empty udid!"));
		}

		Device freeDevice = nullDevice;
		for (Device device : DEVICES)
		{
			if (device.getUdid().equalsIgnoreCase(udid))
			{
				freeDevice = device;
				break;
			}
		}
		if (freeDevice == nullDevice) {
			// TODO: improve loggers about device type, family etc
			throw new RuntimeException("Not found free device!");
		}
		return freeDevice;
	}
	
	public static void registerDevice(Device freeDevice) {
		if (freeDevice != nullDevice)
		{
			//register current device to be able to transfer it into Zafira at the end of the test
			setDevice(freeDevice);
			Long threadId = Thread.currentThread().getId();
			LOGGER.info("register device fot current thread id: " + threadId + "; device: '" + freeDevice.getName() + "'");
		} else
		{
			throw new RuntimeException("Unable to register null Device for the test!");
		}
		
	}
	
	public static Device getDevice()
	{
		long threadId = Thread.currentThread().getId();
		String test = TestNamingUtil.getTestNameByThread();
		Device device = curDevice.get();
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

	public static void deregisterDevice()
	{
		removeDevice();
	}

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

	/**
	 * Check if system is distributed (devices are connected to different servers)
	 * @return boolean
	 */
	public static boolean isSystemDistributed()
	{
		boolean result = false;
		for (int i = 0; i < DEVICES.size() - 1; i++)
		{
			if (!getServer(DEVICES.get(i).getSeleniumServer())
					.equals(getServer(DEVICES.get(i + 1).getSeleniumServer())))
			{
				result = true;
			}
		}
		return result;
	}

	public static String getServer(final String server)
	{
		Matcher matcher = HOST_PATTERN.matcher(server);
		String serverValue;
		if (matcher.find())
		{
			serverValue = matcher.group(1);
		} else
		{
			serverValue = server.split(":")[0];
		}
		return serverValue;
	}

	public static String getServer()
	{
		return getServer(getDevice().getSeleniumServer());
	}

	public static Device getNullDevice() {
		return nullDevice;
	}

	private static void setDevice(Device device) {
		String test = TestNamingUtil.getTestNameByThread();
		long threadId = Thread.currentThread().getId();
		LOGGER.info("Set current device to '" + device.getName() + "' test '" + test + "', thread: " + threadId);
		curDevice.set(device);
	}
	
	private static void removeDevice() {
		curDevice.remove();
	}

}