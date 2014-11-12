package com.qaprosoft.carina.core.foundation.zafira;

import java.util.Date;

import com.qaprosoft.zafira.client.ZafiraClient;
import com.qaprosoft.zafira.client.ZafiraClient.Response;
import com.qaprosoft.zafira.client.model.JobType;
import com.qaprosoft.zafira.client.model.TestCaseType;
import com.qaprosoft.zafira.client.model.TestRunType;
import com.qaprosoft.zafira.client.model.TestRunType.Initiator;
import com.qaprosoft.zafira.client.model.TestSuiteType;
import com.qaprosoft.zafira.client.model.TestType;

public class ZafiraClientExample
{
	private static final ZafiraClient zc = new ZafiraClient("http://stg.caronfly.com:8080/zafira");

	public static void main(String[] args)
	{
		exampleCreateJob();
		exampleCreateTestSuite();
		exampleCreateTestRun();
		exampleFinishTestRun();
		exampleCreateTest();
		exampleCreateTestCases();
	}
	
	public static JobType exampleCreateJob()
	{
		JobType job = new JobType("http://stg.caronfly.com:8081/view/zafira/job/zafira-ws", "akhursevich");
		Response<JobType> response = zc.createJob(job);
		return response.getObject();
	}
	
	public static TestSuiteType exampleCreateTestSuite()
	{
		TestSuiteType testSuite = new  TestSuiteType("sanity", "akhursevich");
		Response<TestSuiteType> response = zc.createTestSuite(testSuite);
		return response.getObject();
	}
	
	public static TestRunType exampleCreateTestRun()
	{
		TestRunType testRun = new TestRunType(1L, "akhursevich", "http://localhost:8081", "master", "sdfs232fs3rwf34f5", "<config></config>", 1L, 1, Initiator.HUMAN);
		Response<TestRunType> response = zc.createTestRun(testRun);
		return response.getObject();
	}
	
	public static TestRunType exampleFinishTestRun()
	{
		Response<TestRunType> response = zc.finishTestRun(4);
		return response.getObject();
	}

	public static TestType exampleCreateTest()
	{
		TestType test = new TestType("Zafira login test", com.qaprosoft.zafira.client.model.TestType.Status.PASSED, "<config></config>", 4L, 1L, "Hello!", new Date().getTime(), new Date().getTime(), "http://localhost:8081/demo", "http://localhost:8081/log");
		Response<TestType> response = zc.createTest(test);
		return response.getObject();
	}
	
	public static TestCaseType [] exampleCreateTestCases()
	{
		TestCaseType[] testCases = new TestCaseType[] {new TestCaseType("com.qaprosoft.zofira.Test", "testLogin", "Hello!", 1L, "akhursevich"), new TestCaseType("com.qaprosoft.zofira.Test", "testLogout", "Hello!", 1L, "vdelendik")};
		Response<TestCaseType[]> response = zc.createTestCases(testCases);
		return response.getObject();
	}
}