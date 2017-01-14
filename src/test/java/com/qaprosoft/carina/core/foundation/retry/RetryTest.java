package com.qaprosoft.carina.core.foundation.retry;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.R;

public class RetryTest {
	@Test
	public void testRetryCounter() {
		String test = "testRetryCounter";
		RetryCounter.initCounter(test);

		Assert.assertEquals(0, 0);
		int count = RetryCounter.getRunCount(test);
		Assert.assertEquals(count, 0);
		RetryCounter.incrementRunCount(test);
		count = RetryCounter.getRunCount(test);
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
