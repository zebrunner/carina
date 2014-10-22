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
package com.qaprosoft.carina.core.foundation.retry;

import java.util.HashMap;
import java.util.Map;

import com.qaprosoft.carina.core.foundation.listeners.UITestListener;

/**
 * Map that stores run count of tests, used in {@link UITestListener}.
 * 
 * @author Alex Khursevich (hursevch@gmail.com)
 */
public class RetryCounter
{
	private static Map<String, Integer> runCountMap;

	static
	{
		runCountMap = new HashMap<String, Integer>();
	}

	public static void initCounter(String test)
	{
		if(runCountMap.containsKey(test))
			return;
		
		runCountMap.put(test, 0);
	}
	
	public static Integer getRunCount(String test)
	{
		return runCountMap.containsKey(test) ? runCountMap.get(test) : 0;
	}
	
	public static void incrementRunCount(String test)
	{
		if(runCountMap.containsKey(test))
		{
			Integer count = runCountMap.get(test) + 1;
			runCountMap.put(test, count);
		}
	}
}
