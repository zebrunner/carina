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

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.internal.IResultListener2;

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.ParameterGenerator;
import com.qaprosoft.carina.core.foundation.utils.StringGenerator;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;

public class TestNameListener implements IResultListener2 {
    private static final Logger LOGGER = Logger.getLogger(TestNameListener.class);

    @Override
    public void beforeConfiguration(ITestResult result) {
        LOGGER.info("TestNameListener->beforeConfiguration");
        // added 3 below lines to be able to track log/screenshots for before suite/class/method actions too
        TestNamingUtil.releaseTestInfoByThread();

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
        LOGGER.info("TestNameListener->onStart");
        String uuid = StringGenerator.generateNumeric(8);
        ParameterGenerator.setUUID(uuid);

        ReportContext.getBaseDir(); // create directory for logging as soon as possible
    }

    @Override
    public void onTestStart(ITestResult result) {
        LOGGER.info("TestNameListener->onTestStart");

        // obligatory reset any registered canonical name because for ALREADY_PASSED methods we can't do this in
        // onTestSkipped method
        TestNamingUtil.releaseTestInfoByThread();

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

}
