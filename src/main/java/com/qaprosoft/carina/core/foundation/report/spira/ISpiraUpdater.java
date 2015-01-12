package com.qaprosoft.carina.core.foundation.report.spira;

import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.report.TestResultType;


public interface ISpiraUpdater {
	public void updateAfterSuite(String testClass, TestResultType testResult, String message, String testName, String details, long startDate);
	
	public void updateAfterTest(ITestResult result, Throwable thr);
}
