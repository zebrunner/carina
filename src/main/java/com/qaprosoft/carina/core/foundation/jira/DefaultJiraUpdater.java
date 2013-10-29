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
package com.qaprosoft.carina.core.foundation.jira;

import java.util.List;

import net.rcarz.jiraclient.JiraClient;

import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.report.TestResultItem;

/*
 * DefaultJiraUpdater
 * 
 * @author Alex Khursevich
 */
public class DefaultJiraUpdater implements IJiraUpdater
{
	@Override
	public void updateAfterTest(JiraClient jira, ITestResult result)
	{
		// DO nothing by default
	}

	@Override
	public void updateAfterSuite(JiraClient jira, List<TestResultItem> results)
	{
		// DO nothing by default
	}
}
