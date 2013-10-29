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
 * TestStatistics - bean for aggregation of performance tests execution for further report generation.
 * 
 * @author Alex Khursevich
 */
public class TestStatistics
{
	private String name;
	private long testsCount;
	private long failuresCount;
	private long averageTime;
	private long minTime;
	private long maxTime;
	private Map<String, TestStatistics> subTestStatistics;
	
	private int users;
	private int loop;
	private long rumpup;

	public TestStatistics(String name, long testsCount)
	{
		this.name = name;
		this.testsCount = testsCount;
		this.failuresCount = testsCount;
		this.minTime = Long.MAX_VALUE;
		this.maxTime = Long.MIN_VALUE;
		this.averageTime = 0;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public long getTestsCount()
	{
		return testsCount;
	}

	public void setTestsCount(long testsCount)
	{
		this.testsCount = testsCount;
	}

	public long getFailuresCount()
	{
		return failuresCount;
	}

	public void setFailuresCount(long failuresCount)
	{
		this.failuresCount = failuresCount;
	}

	public int getSuccessRate()
	{
		long successTestsCount = testsCount - failuresCount;
		return testsCount > 0 ? (int) ((((double) successTestsCount) / ((double) testsCount)) * 100) : 0;
	}

	public long getAverageTime()
	{
		long successTestsCount = testsCount - failuresCount;
		return averageTime > 0 && successTestsCount > 0 ? averageTime / successTestsCount : 0;
	}

	public void setAverageTime(long averageTime)
	{
		this.averageTime = averageTime;
	}

	public long getMinTime()
	{
		return minTime;
	}

	public void setMinTime(long minTime)
	{
		this.minTime = minTime;
	}

	public long getMaxTime()
	{
		return maxTime;
	}

	public void setMaxTime(long maxTime)
	{
		this.maxTime = maxTime;
	}
	
	public int getUsers()
	{
		return users;
	}

	public void setUsers(int users)
	{
		this.users = users;
	}

	public int getLoop()
	{
		return loop;
	}

	public void setLoop(int loop)
	{
		this.loop = loop;
	}

	public long getRumpup()
	{
		return rumpup;
	}

	public void setRumpup(long rumpup)
	{
		this.rumpup = rumpup;
	}

	public Map<String, TestStatistics> getSubTestStatistics()
	{
		if (subTestStatistics == null)
		{
			subTestStatistics = new LinkedHashMap<String, TestStatistics>();
		}
		return subTestStatistics;
	}

	public void addStatistics(long timer)
	{
		this.maxTime = Math.max(timer, this.maxTime);
		this.minTime = Math.min(timer, this.minTime);
		this.failuresCount--;
		this.averageTime += timer;
	}
}