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

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.internal.TestResult;

import com.qaprosoft.carina.core.foundation.jira.Jira;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

public class RetryAnalyzer implements IRetryAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private Integer runCount = 0;
    private Integer maxCount = Configuration.getInt(Parameter.RETRY_COUNT);

    @Override
    public boolean retry(ITestResult result) {
        runCount++;
        LOGGER.debug("RetryAnalyzer: " + result.getMethod().getRetryAnalyzer(result) +
                "method: " + result.getMethod().getConstructorOrMethod().getName() + "; " +
                "paramIndex: " + ((TestResult) result).getParameterIndex() + "; " +
                "runCount: " + runCount);

        LOGGER.debug("RetryAnalyzer: " + result.getMethod().getRetryAnalyzer(result) + "Method: " + result.getMethod().getMethodName()
                + "; Incremented retryCount: " + runCount);
        if (runCount <= maxCount && !Jira.isRetryDisabled(result)) {
            return true;
        }
        return false;
    }
}