/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.core.foundation.retry;

import org.apache.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.jira.Jira;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

public class RetryAnalyzer implements IRetryAnalyzer {
    public static final Logger LOGGER = Logger.getLogger(RetryAnalyzer.class);

    public boolean retry(ITestResult result) {
        if (RetryCounter.getRunCount() < getMaxRetryCountForTest() && !Jira.isRetryDisabled(result)) {
            RetryCounter.incrementRunCount();
            return true;
        }
        return false;
    }

    public static int getMaxRetryCountForTest() {
        return Configuration.getInt(Parameter.RETRY_COUNT);
    }
}