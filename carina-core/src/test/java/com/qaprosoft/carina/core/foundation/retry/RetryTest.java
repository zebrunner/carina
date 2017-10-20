package com.qaprosoft.carina.core.foundation.retry;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.R;


public class RetryTest {
	
	@Test
	public void testInitRetryCounter() {
		RetryCounter.initCounter();
		int count = RetryCounter.getRunCount();
		Assert.assertEquals(count, 0);
	}
	
	@Test
	public void testDoubleInitRetryCounter() {
		RetryCounter.initCounter();
		RetryCounter.initCounter();
		int count = RetryCounter.getRunCount();
		Assert.assertEquals(count, 0);
	}
	
	@Test
	public void testRetryCounter() {
		RetryCounter.initCounter();
		RetryCounter.incrementRunCount();
		int count = RetryCounter.getRunCount();
		Assert.assertEquals(count, 1);
	}

	@Test
	public void testGetMaxRetryCountForTest() {
		R.CONFIG.put("retry_count", "1");
		Assert.assertEquals(RetryAnalyzer.getMaxRetryCountForTest(), 1);
	}
	
	@AfterTest
	public void resetCounter() {
		RetryCounter.resetCounter();
	}

}
