package com.qaprosoft.carina.core.foundation.retry;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.R;


public class RetryTest {
	
	@Test(priority = 1)
	public void testInitRetryCounter() {
		RetryCounter.initCounter();
		int count = RetryCounter.getRunCount();
		Assert.assertEquals(count, 0);
	}
	
	@Test(priority = 2)
	public void testDoubleInitRetryCounter() {
		RetryCounter.initCounter();
		RetryCounter.initCounter();
		int count = RetryCounter.getRunCount();
		Assert.assertEquals(count, 0);
	}
	
	@Test(priority = 3)
	public void testRetryCounter() {
		RetryCounter.initCounter();
		RetryCounter.incrementRunCount();
		int count = RetryCounter.getRunCount();
		Assert.assertEquals(count, 1);
	}

	@Test(priority = 4)
	public void testGetMaxRetryCountForTest() {
		R.CONFIG.put("retry_count", "1");
		Assert.assertEquals(RetryAnalyzer.getMaxRetryCountForTest(), 1);
	}
	
	@AfterMethod
	public void resetCounter() {
		RetryCounter.resetCounter();
	}

}
