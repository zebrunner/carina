/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.xml.XmlTest;

import com.amazonaws.services.s3.model.S3Object;
import com.qaprosoft.amazon.AmazonS3Manager;
import com.qaprosoft.carina.core.foundation.api.APIMethodBuilder;
import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.dataprovider.core.DataProviderFactory;
import com.qaprosoft.carina.core.foundation.jira.Jira;
import com.qaprosoft.carina.core.foundation.listeners.AbstractTestListener;
import com.qaprosoft.carina.core.foundation.report.Artifacts;
import com.qaprosoft.carina.core.foundation.report.testrail.TestRail;
import com.qaprosoft.carina.core.foundation.skip.ExpectedSkipManager;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.DriverMode;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.common.CommonUtils;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;

/*
 * AbstractTest - base test for UI and API tests.
 * 
 * @author Alex Khursevich
 */
@Listeners({ AbstractTestListener.class })
public abstract class AbstractTest // extends DriverHelper
{
    protected static final Logger LOGGER = Logger.getLogger(AbstractTest.class);

    protected APIMethodBuilder apiMethodBuilder;

    protected static final long EXPLICIT_TIMEOUT = Configuration.getLong(Parameter.EXPLICIT_TIMEOUT);

    @BeforeSuite(alwaysRun = true)
    public void executeBeforeTestSuite(ITestContext context) {


    }

    @BeforeClass(alwaysRun = true)
    public void executeBeforeTestClass(ITestContext context) throws Throwable {
        // do nothing for now
    }

    @AfterClass(alwaysRun = true)
    public void executeAfterTestClass(ITestContext context) throws Throwable {
        if (Configuration.getDriverMode() == DriverMode.CLASS_MODE) {
            LOGGER.debug("Deinitialize driver(s) in UITest->AfterClass.");
            quitDrivers();
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void executeBeforeTestMethod(XmlTest xmlTest, Method testMethod, ITestContext context) throws Throwable {

        // handle expected skip
        if (ExpectedSkipManager.getInstance().isSkip(testMethod, context)) {
            skipExecution("Based on rule listed above");
        }

        // do nothing for now
        apiMethodBuilder = new APIMethodBuilder();
    }

    @AfterMethod(alwaysRun = true)
    public void executeAfterTestMethod(ITestResult result) {
        try {
            if (apiMethodBuilder != null) {
                apiMethodBuilder.close();
            }

            DriverMode driverMode = Configuration.getDriverMode();

            if (driverMode == DriverMode.METHOD_MODE) {
                LOGGER.debug("Deinitialize driver(s) in @AfterMethod.");
                quitDrivers();
            }

            // TODO: improve later removing duplicates with AbstractTestListener
            // handle Zafira already passed exception for re-run and do nothing. maybe return should be enough
            if (result.getThrowable() != null && result.getThrowable().getMessage() != null
                    && result.getThrowable().getMessage().startsWith(SpecialKeywords.ALREADY_PASSED)) {
                // [VD] it is prohibited to release TestInfoByThread in this place.!
                return;
            }

            // handle AbstractTest->SkipExecution
            if (result.getThrowable() != null && result.getThrowable().getMessage() != null
                    && result.getThrowable().getMessage().startsWith(SpecialKeywords.SKIP_EXECUTION)) {
                // [VD] it is prohibited to release TestInfoByThread in this place.!
                return;
            }

            List<String> tickets = Jira.getTickets(result);
            result.setAttribute(SpecialKeywords.JIRA_TICKET, tickets);
            Jira.updateAfterTest(result);

            // we shouldn't deregister info here as all retries will not work
            // TestNamingUtil.releaseZafiraTest();

            // clear jira tickets to be sure that next test is not affected.
            Jira.clearTickets();

            Artifacts.clearArtifacts();

        } catch (Exception e) {
            LOGGER.error("Exception in AbstractTest->executeAfterTestMethod: " + e.getMessage());
            e.printStackTrace();
        }

    }

    @AfterSuite(alwaysRun = true)
    public void executeAfterTestSuite(ITestContext context) {


    }


    /**
     * Redefine TestRails cases from test.
     *
     * @param cases to set
     */
    protected void setTestRailCase(String... cases) {
        TestRail.setCasesID(cases);
    }

    @DataProvider(name = "DataProvider", parallel = true)
    public Object[][] createData(final ITestNGMethod testMethod, ITestContext context) {
        Annotation[] annotations = testMethod.getConstructorOrMethod().getMethod().getDeclaredAnnotations();
        Object[][] objects = DataProviderFactory.getNeedRerunDataProvider(annotations, context, testMethod);
        return objects;
    }

    @DataProvider(name = "SingleDataProvider")
    public Object[][] createDataSingleThread(final ITestNGMethod testMethod,
            ITestContext context) {
        Annotation[] annotations = testMethod.getConstructorOrMethod().getMethod().getDeclaredAnnotations();
        Object[][] objects = DataProviderFactory.getNeedRerunDataProvider(annotations, context, testMethod);
        return objects;
    }

    /**
     * Pause for specified timeout.
     *
     * @param timeout in seconds.
     */

    public void pause(long timeout) {
        CommonUtils.pause(timeout);
    }

    public void pause(Double timeout) {
        CommonUtils.pause(timeout);
    }

    protected void putS3Artifact(String key, String path) {
        AmazonS3Manager.getInstance().put(Configuration.get(Parameter.S3_BUCKET_NAME), key, path);
    }

    protected S3Object getS3Artifact(String bucket, String key) {
        return AmazonS3Manager.getInstance().get(Configuration.get(Parameter.S3_BUCKET_NAME), key);
    }

    protected S3Object getS3Artifact(String key) {
        return getS3Artifact(Configuration.get(Parameter.S3_BUCKET_NAME), key);
    }


    protected void setBug(String id) {
        String test = TestNamingUtil.getTestNameByThread();
        TestNamingUtil.associateBug(test, id);
    }

    protected void skipExecution(String message) {
        throw new SkipException(SpecialKeywords.SKIP_EXECUTION + ": " + message);
    }

    // --------------------------------------------------------------------------
    // Web Drivers
    // --------------------------------------------------------------------------
    protected WebDriver getDriver() {
        return getDriver(DriverPool.DEFAULT);
    }

    protected WebDriver getDriver(String name) {
        WebDriver drv = DriverPool.getDriver(name);
        if (drv == null) {
            Assert.fail("Unable to find driver by name: " + name);
        }
        
        return drv;
    }

    protected WebDriver getDriver(String name, DesiredCapabilities capabilities, String seleniumHost) {
        WebDriver drv = DriverPool.getDriver(name, capabilities, seleniumHost);
        if (drv == null) {
            Assert.fail("Unable to find driver by name: " + name);
        }
        return drv;
    }

    protected static void quitDrivers() {
        DriverPool.quitDrivers();
    }


}