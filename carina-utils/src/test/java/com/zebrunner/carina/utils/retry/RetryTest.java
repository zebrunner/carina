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

import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.retry.RetryAnalyzer;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

public class RetryTest {

    private static final int maxRetryCount = Configuration.getInt(Configuration.Parameter.RETRY_COUNT);

    @Test
    public void testRetryAnalyzer() {
        RetryAnalyzer retryAnalyzer = new RetryAnalyzer();

        for (int i = 0; i < maxRetryCount; i++) {
            Assert.assertTrue(retryAnalyzer.retry(Reporter.getCurrentTestResult()),
                    "retryAnalyzer retried " + i + " times, but had to " + maxRetryCount);
        }

        Assert.assertFalse(retryAnalyzer.retry(Reporter.getCurrentTestResult()), "Run count is more than " + maxRetryCount);
    }

}
