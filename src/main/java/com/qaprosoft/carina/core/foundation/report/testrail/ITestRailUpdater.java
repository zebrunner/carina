package com.qaprosoft.carina.core.foundation.report.testrail;

import org.testng.ITestResult;


public interface ITestRailUpdater {
	public void updateBeforeSuite(String testClass);
	
	public void updateAfterTest(ITestResult result, String errorMessage);
}
