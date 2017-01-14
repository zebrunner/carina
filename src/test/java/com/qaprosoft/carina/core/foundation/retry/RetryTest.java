package com.qaprosoft.carina.core.foundation.retry;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;

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

	@AfterMethod
	public void testRetryAnalyzer(ITestResult result) {
		R.CONFIG.put("retry_count", "1");
		RetryCounter.initCounter(TestNamingUtil.getCanonicalTestName(result));

		RetryAnalyzer retryAnalyzer = new RetryAnalyzer();
		Assert.assertEquals(retryAnalyzer.retry(result), true);
		Assert.assertEquals(retryAnalyzer.retry(result), false);
	}

}
