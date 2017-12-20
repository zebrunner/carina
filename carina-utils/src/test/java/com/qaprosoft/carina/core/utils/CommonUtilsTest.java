package com.qaprosoft.carina.core.utils;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.common.CommonUtils;

public class CommonUtilsTest {
	
	private static final Logger LOGGER = Logger.getLogger(CommonUtilsTest.class);

	@Test
	public void testPause(){
		long pause = 2L;
		long startTime = System.currentTimeMillis();		
		CommonUtils.pause(pause);
		long endTime = System.currentTimeMillis();
		long actualPause = (endTime - startTime)/1000;
		Assert.assertEquals(actualPause, pause);
	}	
}
