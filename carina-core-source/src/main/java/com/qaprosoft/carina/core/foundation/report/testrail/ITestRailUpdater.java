package com.qaprosoft.carina.core.foundation.report.testrail;

import org.testng.ITestContext;
import org.testng.ITestResult;


public interface ITestRailUpdater {
	void updateBeforeSuite(ITestContext context, String testClass, String title);
	
	void updateAfterTest(ITestResult result, String errorMessage);
}
