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
import com.qaprosoft.carina.core.foundation.utils.R;
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

	private static final Device nullDevice = new Device("", "", "", "", "", "");

	protected static synchronized void addDevice()
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

	public static synchronized void removeDevice()
	{
		removeDevice(getDevice());
	}

	public static synchronized void removeDevice(Device device)
	{
		if (device == nullDevice)
		{
			LOGGER.error("Unable to unregister NULL device!");
			return;
		}
		DEVICES.remove(device);
		DEVICE_MODELS.remove(device.getName());
		LOGGER.info("Removed device from the DevicePool: " + device.getName());
	}

	public static synchronized List<Device> getDevices() {
		return DEVICES;
	}
	
	public static synchronized void addDevices()
	{
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
			DEVICE_MODELS.add(device.getName());
			String msg = "Added device into the DevicePool: %s - %s. Devices count: %s";
			LOGGER.info(String.format(msg, device.getName(), device.getUdid(), DEVICES.size()));
		}

		// if pool has single device then redefine device name/version etc parameter instead of abstract DevicesPool and
		// Android 5-6...
		if (DEVICES.size() == 1)
		{
			// -Dmobile_device_name=DevicesPool
			// -Dmobile_platform_name=Android
			// -Dmobile_platform_version=5-6
			Device device = DEVICES.get(0);
			LOGGER.info(String.format("Redefine mobile device settings using single device data: %s %s-%s",
					device.getName(), device.getOs(), device.getOsVersion()));

			R.CONFIG.put("mobile_device_name", device.getName());
			R.CONFIG.put("mobile_platform_name", device.getOs());
			R.CONFIG.put("mobile_platform_version", device.getOsVersion());
		}
	}

	public static Device registerDevice()
	{
		Long threadId = Thread.currentThread().getId();
		if (!Configuration.get(Parameter.DRIVER_TYPE).equalsIgnoreCase(SpecialKeywords.MOBILE_POOL)
				&& !Configuration.get(Parameter.DRIVER_TYPE).equalsIgnoreCase(SpecialKeywords.MOBILE))
		{
			return nullDevice;
		}

		final String testId = UUID.randomUUID().toString();
		Device freeDevice = nullDevice;
		if (GRID_ENABLED)
		{
			String allModels = StringUtils.join(DEVICE_MODELS, "+");
			LOGGER.info(
					"Looking for available device among: " + allModels + " using Zafira Grid. Default timeout 10 min.");
			final String udid = DeviceGrid.connectDevice(testId, DEVICE_MODELS);
			if (!StringUtils.isEmpty(udid))
			{
				for (Device device : DEVICES)
				{
					if (device.getUdid().equalsIgnoreCase(udid))
					{
						if (THREAD_2_DEVICE_MAP.containsValue(device))
						{
							String msg = "STF Grid returned busy device as it exists in THREAD_2_DEVICE_MAP: %s - %s!";
							DeviceGrid.disconnectDevice(device.getTestId(), device.getUdid());
							throw new RuntimeException(String.format(msg, device.getName(), device.getUdid()));
						}
						device.setTestId(testId);
						freeDevice = device;
						break;
					}
				}
			}
		} else
		{
			int count = 0;
			boolean found = false;
			while (++count < 100 && !found)
			{
				for (Device device : DEVICES)
				{
					if (!THREAD_2_DEVICE_MAP.containsValue(device))
					{
						// current thread doesn't have ignored devices
						device.setTestId(testId);
						freeDevice = device;
						found = true;
						break;
					}
				}
				if (!found)
				{
					pause(Configuration.getInt(Parameter.INIT_RETRY_INTERVAL));
				}
			}
		}

		if (freeDevice != nullDevice)
		{
			THREAD_2_DEVICE_MAP.put(threadId, freeDevice);
			String msg = "found device %s-%s for test %s";
			LOGGER.info(String.format(msg, freeDevice.getName(), freeDevice.getUdid(), testId));
		} else
		{
			// TODO: improve loggers about device type, family etc
			String allModels = StringUtils.join(DEVICE_MODELS, "+");
			String msg = "Not found free device among %s devices for 10 minutes!";
			throw new RuntimeException(String.format(msg, allModels));
		}

		return freeDevice;

	}

	public static Device getDevice()
	{
		Device device = nullDevice;

		long threadId = Thread.currentThread().getId();
		if (THREAD_2_DEVICE_MAP.containsKey(threadId))
		{
			device = THREAD_2_DEVICE_MAP.get(threadId);
		}
		return device;
	}

	public static Device getDevice(String udid)
	{
		Device device = nullDevice;
		for (Device dev : DEVICES)
		{
			if (dev.getUdid().equalsIgnoreCase(udid))
			{
				device = dev;
				break;
			}
		}
		if (device == nullDevice)
		{
			String msg = "Not found device by udid among registered pool of %s devices!";
			throw new RuntimeException(String.format(msg, udid, DEVICES.size()));
		}
		return device;
	}

	public static void deregisterDevice()
	{
		Long threadId = Thread.currentThread().getId();
		if (THREAD_2_DEVICE_MAP.containsKey(threadId))
		{
			Device device = THREAD_2_DEVICE_MAP.get(threadId);
			if (GRID_ENABLED)
			{
				DeviceGrid.disconnectDevice(device.getTestId(), device.getUdid());
			}
			THREAD_2_DEVICE_MAP.remove(threadId);
			String msg = "Disconnected device '%s - %s' for test '%s'; thread '%s'";
			LOGGER.info(String.format(msg, device.getName(), device.getUdid(), device.getTestId(), threadId));
		}
	}

	public static String getDeviceUdid()
	{
		return (getDevice() != nullDevice) ? getDevice().getUdid() : Configuration.get(Parameter.MOBILE_DEVICE_UDID);
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
			if (Configuration.get(Parameter.DRIVER_TYPE).equalsIgnoreCase(SpecialKeywords.MOBILE_POOL)
					|| Configuration.get(Parameter.DRIVER_TYPE).equalsIgnoreCase(SpecialKeywords.MOBILE))
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

	/**
	 * Pause for specified timeout.
	 * 
	 * @param timeout
	 *            in seconds.
	 */
	private static void pause(long timeout)
	{
		try
		{
			Thread.sleep(timeout * 1000);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}