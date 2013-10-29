/*
 * Copyright 2013 QAPROSOFT (http://qaprosoft.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qaprosoft.carina.core.foundation.performance;

import java.util.LinkedHashMap;
import java.util.Map;

/*
 * Performance test result bean for collecting metrics and details.
 * 
 * @author Alex Khursevich
 */
public class PerformanceTestResult
{
	public enum Status {PASS, FAIL, UNKNOWN}
	
	private String testName;
	private Status status;
	private String failureDetails;
	private long timer;
	private Map<String, PerformanceTestResult> subTestResuts;

	public PerformanceTestResult(String testName)
	{
		this.testName = testName;
		this.status = Status.UNKNOWN;
	}
	
	public void startTest()
	{
		this.startTimer();
	}
	
	public void startSubTest(String testName)
	{
		PerformanceTestResult tr = new PerformanceTestResult(testName);
		tr.startTimer();
		getSubTestResuts().put(testName, tr);
	}
	
	public void finishSubTest(String subTestName, Status status, String failureDetails)
	{
		PerformanceTestResult tr = getSubTestResuts().get(subTestName);
		tr.status = status;
		tr.failureDetails = failureDetails;
		tr.stopTimer();
	}
	
	public void finishTest(Status status, String failureDetails)
	{
		this.status = status;
		this.failureDetails = failureDetails;
		this.stopTimer();
		for(String subTestName : getSubTestResuts().keySet())
		{
			PerformanceTestResult tr = getSubTestResuts().get(subTestName);
			if(tr != null && Status.UNKNOWN.equals(tr.getStatus()))
			{
				tr.setStatus(Status.FAIL);
				tr.setFailureDetails(failureDetails);
				tr.stopTimer();
			}
		}
	}
	
	public String getTestName()
	{
		return testName;
	}

	public void setTestName(String testName)
	{
		this.testName = testName;
	}
	
	public Status getStatus()
	{
		return status;
	}

	public void setStatus(Status status)
	{
		this.status = status;
	}

	public long getTimer()
	{
		return timer;
	}

	public void	startTimer()
	{
		this.timer = System.currentTimeMillis();
	}
	
	public long	stopTimer()
	{
		long delta = System.currentTimeMillis() - timer;
		timer = delta > 0 ? delta : 0;
		return timer;
	}

	public String getFailureDetails()
	{
		return failureDetails;
	}

	public void setFailureDetails(String failureDetails)
	{
		this.failureDetails = failureDetails;
	}

	public Map<String, PerformanceTestResult> getSubTestResuts()
	{
		if(subTestResuts == null)
		{
			subTestResuts = new LinkedHashMap<String, PerformanceTestResult>();
		}
		return subTestResuts;
	}

	public void setSubTestResuts(Map<String, PerformanceTestResult> subTestResuts)
	{
		this.subTestResuts = subTestResuts;
	}

	@Override
	public String toString()
	{
		return String.format("Test result: %s (%d ms)", getStatus(), getTimer());
	}
}
