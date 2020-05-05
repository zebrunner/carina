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
package com.qaprosoft.carina.core.foundation.listeners;

import java.util.Arrays;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.IResultListener2;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.ParameterGenerator;
import com.qaprosoft.carina.core.foundation.utils.StringGenerator;

public class TestNameListener implements IResultListener2 {
    private static final Logger LOGGER = Logger.getLogger(TestNameListener.class);
    
    static final ThreadLocal<String> testName = new ThreadLocal<String>();

    @Override
    public void beforeConfiguration(ITestResult result) {
        LOGGER.info("TestNameListener->beforeConfiguration");
        // added 3 below lines to be able to track log/screenshots for before suite/class/method actions too
        //TestNamingUtil.releaseTestInfoByThread();
        
        setTestName(result);
    }

    @Override
    public void onConfigurationSuccess(ITestResult result) {
        LOGGER.info("TestNameListener->onConfigurationSuccess");
    }

    @Override
    public void onConfigurationSkip(ITestResult result) {
        LOGGER.info("TestNameListener->onConfigurationSkip");
    }

    @Override
    public void onConfigurationFailure(ITestResult result) {
        LOGGER.info("TestNameListener->onConfigurationFailure");
    }

    @Override
    public void onStart(ITestContext context) {
        LOGGER.info("TestNameListener->onStart(ITestContext context)");
        String uuid = StringGenerator.generateNumeric(8);
        ParameterGenerator.setUUID(uuid);

        ReportContext.getBaseDir(); // create directory for logging as soon as possible
    }

    @Override
    public void onTestStart(ITestResult result) {
        LOGGER.info("TestNameListener->onTestStart");

        // obligatory reset any registered canonical name because for ALREADY_PASSED methods we can't do this in
        // onTestSkipped method
        //TestNamingUtil.releaseTestInfoByThread();
        
        setTestName(result);

    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        LOGGER.info("TestNameListener->onTestSuccess");
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        LOGGER.info("TestNameListener->");
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        LOGGER.info("TestNameListener->onTestFailure");
    }

    @Override
    public void onFinish(ITestContext context) {
        LOGGER.info("TestNameListener->onFinish");
    }
    
    public static String getTestName() {
        //TODO: think about returning very simple vaid name if nothing was specified yet! Need ITestResult arg for that!
        if (testName.get() == null) {
            LOGGER.error("testName is not set yet! returning " + SpecialKeywords.UNDEFINED + " value");
            return SpecialKeywords.UNDEFINED;
        }
        return testName.get();
    }
    
    public static String setTestName(String name) {
        LOGGER.warn("Overridden testName: " + name);
        testName.set(name);
        return testName.get();
    }
    
    @SuppressWarnings("unlikely-arg-type")
    private static String setTestName(ITestResult result) {
        String name = "";

        if (result.getTestContext() != null) {
            @SuppressWarnings("unchecked")
            Map<Object[], String> testnameMap = (Map<Object[], String>) result.getTestContext().getAttribute(SpecialKeywords.TEST_NAME_ARGS_MAP);
    
            if (testnameMap != null) {
                String testHash = String.valueOf(Arrays.hashCode(result.getParameters()));
                if (testnameMap.containsKey(testHash)) {
                    name = testnameMap.get(testHash);
                }
            }
    
            if (name.isEmpty()) {
                name = result.getTestContext().getCurrentXmlTest().getName();
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
                    name = methodUID + " - " + name;
                }
            }
        }

        name = name + " - " + getMethodName(result);
        LOGGER.info("testName: " + name);
        
        testName.set(name);
        return testName.get();
    }
    

    private static String getMethodName(ITestResult result) {
        
        //TODO: test testMethodName adjustment based on pattern
        // adjust testName using pattern
        ITestNGMethod m = result.getMethod();
        String name = Configuration.get(Configuration.Parameter.TEST_NAMING_PATTERN);
        name = name.replace(SpecialKeywords.METHOD_NAME, m.getMethodName());
        name = name.replace(SpecialKeywords.METHOD_PRIORITY, String.valueOf(m.getPriority()));
        name = name.replace(SpecialKeywords.METHOD_THREAD_POOL_SIZE, String.valueOf(m.getThreadPoolSize()));

        if (m.getDescription() != null) {
            name = name.replace(SpecialKeywords.METHOD_DESCRIPTION, m.getDescription());
        } else {
            name = name.replace(SpecialKeywords.METHOD_DESCRIPTION, "");
        }

        return name;
    }
    
}
