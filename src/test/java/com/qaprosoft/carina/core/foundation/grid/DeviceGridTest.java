package com.qaprosoft.carina.core.foundation.grid;

import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;


public class DeviceGridTest
{
	@Test
	public void testDeviceGrid()
	{
		final long threadId = Thread.currentThread().getId();
		DevicePool.registerDevice2Thread(threadId);
		DevicePool.deregisterDeviceByThread(threadId);
	}
}
