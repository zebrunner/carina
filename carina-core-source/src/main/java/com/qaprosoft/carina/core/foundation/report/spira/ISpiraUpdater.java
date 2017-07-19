package com.qaprosoft.carina.core.foundation.report.spira;

import java.util.List;

import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.report.TestResultType;


public interface ISpiraUpdater {
	void updateAfterSuite(String testClass, TestResultType testResult, String message, String testName, long startDate);
	
	void updateAfterTest(ITestResult result, String errorMessage, List<String> jiraTickets);
}
