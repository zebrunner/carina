/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.jira.Jira;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

public class RetryAnalyzer implements IRetryAnalyzer {
    private static final Logger LOGGER = Logger.getLogger(RetryAnalyzer.class);
    private static ThreadLocal<Integer> runCount = new ThreadLocal<Integer>();
    
    @Override
    public boolean retry(ITestResult result) {
        incrementRunCount();
        if (result.getThrowable() != null && result.getThrowable().getMessage() != null
                && result.getThrowable().getMessage().startsWith(SpecialKeywords.ALREADY_PASSED)) {
            LOGGER.debug("AlreadyPassedRetryAnalyzer: " + result.getMethod().getRetryAnalyzer(result) + "Method: " + result.getMethod().getMethodName() + "; Incremented retryCount: " + getRunCount());
            return false;
        }

        LOGGER.debug("RetryAnalyzer: " + result.getMethod().getRetryAnalyzer(result) + "Method: " + result.getMethod().getMethodName() + "; Incremented retryCount: " + getRunCount());
        if (getRunCount() <= getMaxRetryCountForTest() && !Jira.isRetryDisabled(result)) {
            return true;
        }
        return false;
    }
    
    public Integer getRunCount() {
        int count = 0;
        if (runCount.get() != null) {
            // retryCounter already init for current thread
            count = runCount.get();
        }

        return count;
    }
    
    public void incrementRunCount() {
        int count = 0;
        if (runCount.get() != null) {
            // retryCounter already init for current thread
            count = runCount.get();
        }
        runCount.set(++count);
    }
    
    public void resetCounter() {
        // explicitly set runCount to 0 for current thread
        runCount.set(0);
    }
    
    public static int getMaxRetryCountForTest() {
        return Configuration.getInt(Parameter.RETRY_COUNT);
    }

}