/*
 * Copyright 2013-2015 QAPROSOFT (http://qaprosoft.com/).
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
package com.qaprosoft.carina.core.foundation.log;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Collects test logs and screenshot comments. Comments are associated with
 * webdriver session ID, screenshot comments - using screenshot file name.
 * 
 * @author Alex Khursevich
 */
public class TestLogCollector
{
	private static Map<String, StringBuilder> collector = Collections.synchronizedMap(new HashMap<String, StringBuilder>());
	private static Map<String, String> screenSteps = Collections.synchronizedMap(new HashMap<String, String>());

	/**
	 * Clears messages in driver session context.
	 * 
	 * @param sessionId String
     */
	public static void clearSessionLogs(String sessionId)
	{
		if (collector.containsKey(sessionId))
		{
			collector.remove(sessionId);
		} 
	}
	
	/**
	 * Adds message to test logs.
	 *
     * @param sessionId Session id
	 * @param msg message
	 *            
	 */
	public static synchronized void logToSession(String sessionId, String msg)
	{
		msg += "\n\r";
		if (collector.containsKey(sessionId))
		{
			collector.get(sessionId).append(msg);
		} 
		else
		{
			collector.put(sessionId, new StringBuilder(msg));
		}
	}

	/**
	 * Returns test log by webdriver session ID.
	 * 
	 * @param sessionId String
	 * 
	 * @return test logs
	 */
	public static synchronized String getSessionLogs(String sessionId)
	{
		return collector.containsKey(sessionId) ? collector.get(sessionId).toString() : "";
	}

	/**
	 * Stores comment for screenshot.
	 *
     * @param screenId screenId id
     * @param msg message
	 *            
	 */
	public static void addScreenshotComment(String screenId, String msg)
	{
		if (!StringUtils.isEmpty(screenId))
		{
			screenSteps.put(screenId, msg);
		}
	}

	/**
	 * Return comment for screenshot.
	 * 
	 * @param screenId Screen Id
	 *            
	 * @return screenshot comment
	 */
	public static String getScreenshotComment(String screenId)
	{
		String comment = "";
		if (screenSteps.containsKey(screenId))
			comment = screenSteps.get(screenId);
		return comment;
	}
}
