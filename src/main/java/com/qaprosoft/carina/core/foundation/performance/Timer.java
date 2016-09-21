/*
 * Copyright 2013-2016 QAPROSOFT (http://qaprosoft.com/).
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

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class Timer
{
	private static final Logger LOGGER = Logger.getLogger(Timer.class);

	private static ThreadLocal<ConcurrentHashMap<String, Long>> metrics = new ThreadLocal<ConcurrentHashMap<String, Long>>();
	
	public static synchronized void start(IPerformanceOperation operation)
	{
		Map<String, Long> testMertrics = getTestMetrics();
		if(testMertrics.containsKey(operation.getKey()))
		{
			throw new RuntimeException("Operation already started: " + operation.getKey());
		}
		testMertrics.put(operation.getKey(), Calendar.getInstance().getTimeInMillis());
	}
	
	public static synchronized void stop(IPerformanceOperation operation)
	{
		Map<String, Long> testMertrics = getTestMetrics();
		if(!testMertrics.containsKey(operation.getKey()))
		{
			throw new RuntimeException("Operation not started: " + operation.getKey());
		}
		testMertrics.put(operation.getKey(), Calendar.getInstance().getTimeInMillis() - testMertrics.get(operation.getKey()));
	}
	
	public static synchronized Map<String, Long> readAllRecords()
	{
		Map<String, Long> testMertrics = getTestMetrics();
		for(String key : testMertrics.keySet())
		{
			// timer not stopped
			if(TimeUnit.MILLISECONDS.toMinutes(testMertrics.get(key)) > 60)
			{
				testMertrics.remove(key);
				LOGGER.error("Timer not stopped for operation: " + key);
			}
		}
		return testMertrics;
	}
	
	public static synchronized void clear()
	{
		getTestMetrics().clear();
	}
	
	private static Map<String, Long> getTestMetrics()
	{
		ConcurrentHashMap<String, Long> testMetrics = metrics.get();
		if(testMetrics == null)
		{
			testMetrics = new ConcurrentHashMap<>();
			metrics.set(testMetrics);
		}
		return testMetrics;
	}
	
	public interface IPerformanceOperation
	{
		String getKey();
	}
}



