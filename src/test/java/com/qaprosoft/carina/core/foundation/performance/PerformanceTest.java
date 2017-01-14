package com.qaprosoft.carina.core.foundation.performance;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.performance.Operation.OPERATIONS;

public class PerformanceTest {

	@Test
	public void testAlreadyStartedMetric() {
		Timer.start(OPERATIONS.TEST);
		try {
			Timer.start(OPERATIONS.TEST);
			Assert.fail("Operation already started exception is not raised!");
		} catch (Exception e) {
			Assert.assertEquals("Operation already started: " + OPERATIONS.TEST.getKey(), e.getMessage());
		}
		Timer.stop(OPERATIONS.TEST);
	}

	@Test
	public void testNotStartedMetric() {
		try {
			Timer.stop(OPERATIONS.TEST2);
			Assert.fail("Operation not started is not raised!");
		} catch (Exception e) {
			Assert.assertEquals("Operation not started: " + OPERATIONS.TEST2.getKey(), e.getMessage());
		}
	}

	@Test
	public void testClearMetric() {
		Timer.start(OPERATIONS.TEST3);
		pause(0.1);
		Timer.stop(OPERATIONS.TEST3);

		Timer.clear();
		Map<String, Long> testMetrics = Timer.readAndClear();
		Assert.assertEquals(testMetrics.size(), 0);
	}

	@Test
	public void testTrackMetric() {
		Timer.start(OPERATIONS.TEST4);
		pause(0.1);
		Timer.stop(OPERATIONS.TEST4);

		Map<String, Long> testMetrics = Timer.readAndClear();
		Assert.assertEquals(testMetrics.size(), 1);
		Assert.assertTrue(testMetrics.containsKey(OPERATIONS.TEST4.getKey()));

		// map should be empty for 2nd read attempt
		testMetrics = Timer.readAndClear();
		Assert.assertEquals(testMetrics.size(), 0);

	}

	private void pause(Double timeout) {
		try {
			timeout = timeout * 1000;
			long miliSec = timeout.longValue();
			Thread.sleep(miliSec);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
