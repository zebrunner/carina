package com.qaprosoft.carina.core.foundation.retry;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.R;

public class RetryTest {
	
	@Test
	public void testIncrementNotExistedTestRetryCounter() {
		String test = "test";
		RetryCounter.incrementRunCount(test);
		// count should be 0 anyway
		int count = RetryCounter.getRunCount(test);
		Assert.assertEquals(count, 0);
	}
	
	@Test
	public void testInitRetryCounter() {
		String test = "testInitRetryCounter";
		RetryCounter.initCounter(test);
		int count = RetryCounter.getRunCount(test);
		Assert.assertEquals(count, 0);

	}
	
	@Test
	public void testDoubleInitRetryCounter() {
		String test = "testDoubleInitRetryCounter";
		RetryCounter.initCounter(test);
		RetryCounter.initCounter(test);
		int count = RetryCounter.getRunCount(test);
		Assert.assertEquals(count, 0);
	}
	
	@Test
	public void testRetryCounter() {
		String test = "testRetryCounter";
		RetryCounter.initCounter(test);
		RetryCounter.incrementRunCount(test);
		int count = RetryCounter.getRunCount(test);
		Assert.assertEquals(count, 1);
	}

	@Test
	public void testGetMaxRetryCountForTest() {
		R.CONFIG.put("retry_count", "1");
		Assert.assertEquals(RetryAnalyzer.getMaxRetryCountForTest(), 1);
	}

/*	@AfterMethod
	public void testRetryAnalyzer(ITestResult result) {
		if (result.getMethod().getMethodName().equals("testRetryCounter")) {
	
			String test = TestNamingUtil.getCanonicalTestName(result);
			R.CONFIG.put("retry_count", "1");
			R.CONFIG.put("track_known_issues", "true");
			
			RetryCounter.initCounter(test);
			int count = RetryCounter.getRunCount(test);
			Assert.assertEquals(count, 0);

			Assert.assertEquals(retryAnalyzer.retry(result), true);
			count = RetryCounter.getRunCount(test);
			Assert.assertEquals(count, 1);
			Assert.assertEquals(retryAnalyzer.retry(result), false);
		}
	}
*/
}
