package com.qaprosoft.carina.core.foundation.report.testrail;

import org.testng.ITestContext;
import org.testng.ITestResult;


public interface ITestRailUpdater {
	public void updateBeforeSuite(ITestContext context, String testClass, String title);
	
	public void updateAfterTest(ITestResult result, String errorMessage);
}
