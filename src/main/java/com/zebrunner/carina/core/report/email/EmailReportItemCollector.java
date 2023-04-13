/*******************************************************************************
 * Copyright 2020-2023 Zebrunner Inc (https://www.zebrunner.com).
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
package com.zebrunner.carina.core.report.email;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
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
    private static LinkedHashMap<String, TestResultItem> emailResultsMap = new LinkedHashMap<String, TestResultItem>();
    private static Map<String, TestResultItem> testResultsMap = Collections.synchronizedMap(new HashMap<String, TestResultItem>());
    private static List<String> createdItems = new ArrayList<String>();

    public static synchronized void push(TestResultItem emailItem) {
        emailResultsMap.put(emailItem.hash(), emailItem);
        testResultsMap.put(emailItem.getTest(), emailItem);
    }

    public static synchronized void push(String itemToDelete) {
        createdItems.add(itemToDelete);
    }

    public static synchronized TestResultItem pull(ITestResult result) {
        TestResultItem testResultItem = null;
        try {
            Class<?> artifactClass = ClassUtils.getClass("com.zebrunner.agent.testng.core.testname.TestNameResolverRegistry");
            Object object = MethodUtils.invokeStaticMethod(artifactClass, "get");
            Object name = MethodUtils.invokeMethod(object, "resolve", result);
            testResultItem = testResultsMap.get(name);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            LOGGER.debug("Cannot get info from TestNameResolverRegistry class because Zebrunner agent does not loaded in classloader");
        }
        return testResultItem;
    }

    public static List<TestResultItem> getTestResults() {
        return new ArrayList<TestResultItem>(emailResultsMap.values());
    }

    public static List<String> getCreatedItems() {
        return createdItems;
    }
}
