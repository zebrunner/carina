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
import java.util.regex.Pattern;

import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.JiraClient;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.crypto.CryptoTool;
import com.qaprosoft.carina.core.foundation.report.TestResultItem;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

/*
 * Jira
 * 
 * @author Alex Khursevich
 */
public class Jira
{
	private static final Logger LOG = Logger.getLogger(Jira.class);
	private static IJiraUpdater updater;
	private static JiraClient jira;
	private static boolean isInitialized = false;
	private static CryptoTool cryptoTool;
	private static Pattern CRYPTO_PATTERN = Pattern.compile(SpecialKeywords.CRYPT);

	static
	{
		try
		{
			cryptoTool = new CryptoTool();
			updater = (IJiraUpdater) Class.forName(Configuration.get(Parameter.JIRA_UPDATER)).newInstance();
			BasicCredentials creds = new BasicCredentials(cryptoTool.decryptByPattern(Configuration.get(Parameter.JIRA_USER), CRYPTO_PATTERN), 
					(cryptoTool.decryptByPattern(Configuration.get(Parameter.JIRA_PASSWORD), CRYPTO_PATTERN)));
			jira = new JiraClient(Configuration.get(Parameter.JIRA_URL), creds);
			isInitialized = true;
		}
		catch(Exception e)
		{
			LOG.info("Jira update utility not initialized (specify jira_updater, jira_url, jira_user, jira_password, crypto_key_path): " + e.getMessage());
		}
	}
	
	public synchronized static void updateAfterTest(ITestResult result)
	{
		if(isInitialized)
		{
			try
			{
				updater.updateAfterTest(jira, result);
			}
			catch(Exception e)
			{
				LOG.error("Jira 'updateAfterTest' not performed: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public synchronized static void updateAfterSuite(ITestContext context, List<TestResultItem> results)
	{
		if(isInitialized)
		{
			try
			{
				 updater.updateAfterSuite(jira, context, results);
			}
			catch(Exception e)
			{
				LOG.error("Jira 'updateAfterSuite' not performed: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
