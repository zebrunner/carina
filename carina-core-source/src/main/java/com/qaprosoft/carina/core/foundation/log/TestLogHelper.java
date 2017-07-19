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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

//import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;

/**
 * Helps to collect session logs.
 * 
 * @author Alex Khursevich
 */
public class TestLogHelper
{
	private String prefix = "";
	private String PREFIX_FORMAT = "%s - ";
	private String sessionID;

	public TestLogHelper(String sessionID)
	{
		this.sessionID = sessionID;
	}
	
	public TestLogHelper(WebDriver driver)
	{
		this.sessionID = ((RemoteWebDriver) driver).getSessionId().toString();
	}

	public TestLogHelper(WebDriver driver, String prefix)
	{
		this.sessionID = ((RemoteWebDriver) driver).getSessionId().toString();
		this.prefix = String.format(PREFIX_FORMAT, prefix);
		TestLogCollector.clearSessionLogs(sessionID);
	}

	public void log(String msg)
	{
		TestLogCollector.logToSession(sessionID, prefix + msg);
	}
	
	public void log(String msg, Throwable thr)
	{
        StackTraceElement[] elems = thr.getStackTrace();
        for (StackTraceElement elem : elems) {
        	msg = msg + "\n" + elem.toString();
        }
		TestLogCollector.logToSession(sessionID, prefix + msg);
	}
	
	public void log(Throwable thr)
	{
		String msg = thr.getMessage();
		
        StackTraceElement[] elems = thr.getStackTrace();
        for (StackTraceElement elem : elems) {
        	msg = msg + "\n" + elem.toString();
        }
        
        TestLogCollector.logToSession(sessionID, prefix + msg);
	}
	
	public String getSessionLogs()
	{
		return TestLogCollector.getSessionLogs(sessionID);
	}

/*	public static String getSessionLogs(String test)
	{
		return TestLogCollector.getSessionLogs(DriverPool.getSessionIdByTestName(test));
	}*/
	
	public static String getSessionLogs(WebDriver driver)
	{
		return TestLogCollector.getSessionLogs(((RemoteWebDriver) driver).getSessionId().toString());
	}	

	public void setPrefix(String prefix)
	{
		this.prefix = String.format(PREFIX_FORMAT, prefix);
	}
}
