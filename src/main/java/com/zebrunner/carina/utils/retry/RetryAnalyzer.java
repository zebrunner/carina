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

import com.zebrunner.carina.core.config.TestConfiguration;
import com.zebrunner.carina.utils.config.Configuration;

public class RetryAnalyzer implements IRetryAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Integer MAX_COUNT = Configuration.getRequired(TestConfiguration.Parameter.RETRY_COUNT, Integer.class);

    private Integer runCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        runCount++;
        LOGGER.debug("RetryAnalyzer: {}; method: {}; " +
                "paramIndex: {}; runCount: {}",
                result.getMethod().getRetryAnalyzer(result),
                result.getMethod().getConstructorOrMethod().getName(),
                ((TestResult) result).getParameterIndex(),
                runCount);

        LOGGER.debug(
                "RetryAnalyzer: {}; Method: {}; Incremented retryCount: {}",
                result.getMethod().getRetryAnalyzer(result),
                result.getMethod().getMethodName(),
                runCount);
        return runCount <= MAX_COUNT;
    }
}
