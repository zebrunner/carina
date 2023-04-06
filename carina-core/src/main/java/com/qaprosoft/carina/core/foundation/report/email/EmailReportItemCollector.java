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
package com.qaprosoft.carina.core.foundation.report.email;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;

import com.zebrunner.carina.utils.report.TestResultItem;

/**
 * EmailReportGenerator generates emailable report using data from test suite log.
 * 
 * @author Alex Khursevich
 */
public class EmailReportItemCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static synchronized void push(TestResultItem emailItem) {
        TestResultCollector.push(emailItem);
    }

    public static synchronized void push(String itemToDelete) {
        TestResultCollector.push(itemToDelete);
    }

    public static synchronized TestResultItem pull(ITestResult result) {
        return TestResultCollector.pull(result);
    }

    public static List<TestResultItem> getTestResults() {
        return TestResultCollector.getTestResults();
    }

    public static List<String> getCreatedItems() {
        return TestResultCollector.getCreatedItems();
    }
}
