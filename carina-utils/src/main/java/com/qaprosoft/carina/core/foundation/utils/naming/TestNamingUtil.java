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
package com.qaprosoft.carina.core.foundation.utils.naming;

import java.util.Arrays;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.TestResult;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;

/**
 * Common naming utility for unique test method identification.
 * 
 * @author Aliaksei_Khursevich (hursevich@gmail.com)
 */
public class TestNamingUtil {
    private static final Logger LOGGER = Logger.getLogger(TestNamingUtil.class);

    private static final ConcurrentHashMap<Long, Stack<String>> threadId2TestName = new ConcurrentHashMap<Long, Stack<String>>();
    private static final ConcurrentHashMap<String, String> testName2Bug = new ConcurrentHashMap<String, String>();
    
    private static final ConcurrentHashMap<String, Integer> testNameInvCounter = new ConcurrentHashMap<String, Integer>();
    
    public static synchronized String associateTestInfo2Thread(String test, Long threadId, ITestResult result) {
        // introduce invocation count calculation here as in multi threading mode TestNG doesn't provide valid
        // getInvocationCount() value
        
        
        int index = ((TestResult) result).getParameterIndex();
        if (index > 0) {
            // that's a dataprovider line index
            index++; //to make correlation between line and index number
            LOGGER.debug("test: " + test  + "; index: " + index);
            test = test + String.format(SpecialKeywords.DAPAPROVIDER_INDEX, String.format("%04d", index));
        }
        
        int invCount = result.getTestContext().getAllTestMethods()[0].getInvocationCount();
        if (invCount > 1) {
            LOGGER.debug("Detected method '" + result.getMethod().getMethodName() + "' with non zero invocationCount: " + invCount);
            int countIndex = getCurrentInvocationCount(test);
            LOGGER.debug("test: " + test  + "; InvCount index: " + countIndex);
            test = test + String.format(SpecialKeywords.INVOCATION_COUNTER, String.format("%04d", countIndex));
        }        
        
        // TODO: analyze how to use stack for retries
        Stack<String> stack = new Stack<String>();

        if (threadId2TestName.containsKey(threadId)) {
            // not the first time
            stack = threadId2TestName.get(threadId);
        }
        stack.push(test);
        threadId2TestName.put(threadId, stack);
        return test;
    }

    public static synchronized void releaseTestInfoByThread() {
        long threadId = Thread.currentThread().getId();
        if (!isTestNameRegistered()) {
            // LOGGER.warn("There is no TestInfo for release in threadId: " + threadId);
            return;
        }

        Stack<String> stack = threadId2TestName.get(threadId);
        String test = stack.pop();
        LOGGER.debug("Releasing information about test: " + test);

        if (stack.isEmpty()) {
            threadId2TestName.remove(threadId);
        }
    }

    public static boolean isTestNameRegistered() {
        long threadId = Thread.currentThread().getId();
        if (threadId2TestName.get(threadId) != null) {
            Stack<String> stack = threadId2TestName.get(threadId);
            if (stack.size() > 0) {
                String test = stack.get(stack.size() - 1);
                return test != null;
            }
        }
        return false;
    }

    public static String getTestNameByThread() {
        long threadId = Thread.currentThread().getId();

        Stack<String> stack = threadId2TestName.get(threadId);
        if (stack == null) {
            LOGGER.debug("Unable to find registered test name for threadId: " + threadId + ". stack is null!");
            return null;
        }

        if (stack.size() == 0) {
            LOGGER.debug("Unable to find registered test name for threadId from empty stack: " + threadId);
            return null;
        }

        return stack.get(stack.size() - 1);
    }

    public static synchronized void associateBug(String testName, String id) {
        testName2Bug.put(testName, id);
    }

    public static synchronized String getBug(String testName) {
        if (testName == null) {
            return null;
        }
        return testName2Bug.get(testName);
    }

    public static String getCanonicalTestName(ITestResult result) {
        // verify if testName is already registered with thread then return it back
        if (isTestNameRegistered()) {
            return getTestNameByThread();
        }

        String testName = "";

        if (result.getTestContext() != null) {
            @SuppressWarnings("unchecked")
            Map<Object[], String> testnameMap = (Map<Object[], String>) result.getTestContext().getAttribute(SpecialKeywords.TEST_NAME_ARGS_MAP);
    
            if (testnameMap != null) {
                String testHash = String.valueOf(Arrays.hashCode(result.getParameters()));
                if (testnameMap.containsKey(testHash)) {
                    testName = testnameMap.get(testHash);
                }
            }
    
            if (testName.isEmpty()) {
                testName = result.getTestContext().getCurrentXmlTest().getName();
            }
    
            // TODO: find the bext way to calculate TUID/hash
            if (result.getTestContext().getCurrentXmlTest().getAllParameters().containsKey(SpecialKeywords.EXCEL_DS_CUSTOM_PROVIDER) ||
                    result.getTestContext().getCurrentXmlTest().getAllParameters().containsKey(SpecialKeywords.DS_CUSTOM_PROVIDER)) {
                // AUTO-274 "Pass"ing status set on emailable report when a test step fails
                String methodUID = "";
                for (int i = 0; i < result.getParameters().length; i++) {
                    if (result.getParameters()[i] != null) {
                        if (result.getParameters()[i].toString().contains(SpecialKeywords.TUID + ":")) {
                            methodUID = result.getParameters()[i].toString().replace(SpecialKeywords.TUID + ":", "");
                            break; // first TUID: parameter is used
                        }
                    }
                }
                if (!methodUID.isEmpty()) {
                    testName = methodUID + " - " + testName;
                }
            }
        }

        return StringEscapeUtils.escapeHtml4(appendTestMethodName(testName, result.getMethod()));
    }

    public static String getCanonicalTestMethodName(ITestResult result) {
        String testMethodName = result.getMethod().getMethodName();

        // TODO: remove test method name map as soon as possible
        @SuppressWarnings("unchecked")
        Map<Object[], String> testMethodNameMap = (Map<Object[], String>) result.getTestContext()
                .getAttribute(SpecialKeywords.TEST_METHOD_NAME_ARGS_MAP);

        if (testMethodNameMap != null) {
            String testHash = String.valueOf(Arrays.hashCode(result.getParameters()));
            if (testMethodNameMap.containsKey(testHash)) {
                LOGGER.error("Error message to check how often this feature is used.");
                testMethodName = testMethodNameMap.get(testHash);
            }
        }

        return StringEscapeUtils.escapeHtml4(testMethodName);
    }

    public static String getPackageName(ITestResult result) {
        return StringEscapeUtils.escapeHtml4(result.getMethod().getRealClass().getPackage().getName());
    }

    public static String appendTestMethodName(String testName, ITestNGMethod m) {
        testName = testName + " - " + adjustTestName(m);
        return StringEscapeUtils.escapeHtml4(testName);
    }

    private static String adjustTestName(ITestNGMethod m) {
        String testName = Configuration.get(Configuration.Parameter.TEST_NAMING_PATTERN);
        testName = testName.replace(SpecialKeywords.METHOD_NAME, m.getMethodName());
        testName = testName.replace(SpecialKeywords.METHOD_PRIORITY, String.valueOf(m.getPriority()));
        testName = testName.replace(SpecialKeywords.METHOD_THREAD_POOL_SIZE, String.valueOf(m.getThreadPoolSize()));

        if (m.getDescription() != null) {
        	testName = testName.replace(SpecialKeywords.METHOD_DESCRIPTION, m.getDescription());
        } else {
        	testName = testName.replace(SpecialKeywords.METHOD_DESCRIPTION, "");
        }

        return testName;
    }

    private static int getCurrentInvocationCount(String test) {
        /*TODO: reopen https://github.com/cbeust/testng/issues/1758 bug 
         * Explain that appropriate TestNG functionality doesn't work in multi-threading env 
         */
        
        int invCount = 1;
        if (!testNameInvCounter.containsKey(test)) {
            testNameInvCounter.put(test, invCount);
        } else {
            invCount = testNameInvCounter.get(test) + 1;
            testNameInvCounter.put(test, invCount);
        }
        
        return invCount;
    }
}
