package com.qaprosoft.carina.core.foundation.grid;

import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;


public class DeviceGridTest
{
	@Test
	public void testDeviceGrid()
	{
		DevicePool.registerDevices();
		final long threadId = Thread.currentThread().getId();
		DevicePool.registerDevice2Thread(threadId);
		
		try {
			Thread.sleep(15*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DevicePool.deregisterDeviceByThread(threadId);
	}
}
