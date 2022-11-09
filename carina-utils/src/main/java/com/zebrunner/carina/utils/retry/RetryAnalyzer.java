/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
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
package com.zebrunner.carina.utils.retry;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.internal.TestResult;

import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.Configuration.Parameter;

public class RetryAnalyzer implements IRetryAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Integer maxCount = Configuration.getInt(Parameter.RETRY_COUNT);

    private Integer runCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        runCount++;
        LOGGER.debug("RetryAnalyzer: " + result.getMethod().getRetryAnalyzer(result) +
                "method: " + result.getMethod().getConstructorOrMethod().getName() + "; " +
                "paramIndex: " + ((TestResult) result).getParameterIndex() + "; " +
                "runCount: " + runCount);

        LOGGER.debug("RetryAnalyzer: " + result.getMethod().getRetryAnalyzer(result) + "Method: " + result.getMethod().getMethodName()
                + "; Incremented retryCount: " + runCount);
        return runCount <= maxCount;
    }
}
