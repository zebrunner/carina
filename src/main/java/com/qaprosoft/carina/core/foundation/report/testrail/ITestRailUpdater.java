package com.qaprosoft.carina.core.foundation.report.testrail;

import com.qaprosoft.carina.core.foundation.report.TestResultType;
import org.testng.ITestResult;


public interface ITestRailUpdater {
	public void updateBeforeSuite(String testClass);
	
	public void updateAfterTest(ITestResult result, Throwable thr);
}
