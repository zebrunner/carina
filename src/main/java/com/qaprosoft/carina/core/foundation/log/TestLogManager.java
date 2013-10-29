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
package com.qaprosoft.carina.core.foundation.log;

import java.io.File;
import java.io.PrintStream;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.log4j.Logger;
import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;

/*
 * TestLogManager - simple utility that switches to new test log file for every single test.
 * 
 * @author Alex Khursevich
 */
public class TestLogManager
{
	private static final Logger LOGGER = Logger.getLogger(TestLogManager.class);
	
	private static final String TEST_LOG_FILE = "test.log";

	public static PrintStream initTestLogStream(ITestResult test)
	{
		File root = ReportContext.getTestDir(TestNamingUtil.getCanonicalTestName(test));
		PrintStream logStream = null;
		try
		{
			File log = new File(root.getAbsoluteFile() + "/" + TEST_LOG_FILE);
			if (log.exists())
			{
				log.delete();
			}
			logStream = new PrintStream(log);
			PrintStream psOut = new PrintStream(new TeeOutputStream(System.out, logStream));
			PrintStream psErr = new PrintStream(new TeeOutputStream(System.err, logStream));
			System.setOut(psOut);
			System.setErr(psErr);
		}
		catch (Exception e)
		{
			LOGGER.error(e.getMessage());
		}

		return logStream;
	}

	public static void closeTestLogStream(PrintStream logStream)
	{
		if (logStream != null)
		{
			logStream.close();
		}
	}
}
