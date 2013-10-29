package com.qaprosoft.carina.core.foundation.log;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.log.GlobalTestLog.Type;

public class GlobalTestLogTest
{
	@Test
	public void testGlobalTestLog()
	{
		GlobalTestLog logger = new GlobalTestLog();
		logger.log(Type.SOAP, "1");
		logger.log(Type.REST, "2");
		logger.log(Type.UI, "3");
		Assert.assertEquals(logger.readLog(Type.SOAP), "SOAP LOGGER: 1\r\n");
		Assert.assertEquals(logger.readLog(Type.REST), "REST LOGGER: 2\r\n");
		Assert.assertEquals(logger.readLog(Type.UI), "UI LOGGER: 3\r\n");
	}
}
