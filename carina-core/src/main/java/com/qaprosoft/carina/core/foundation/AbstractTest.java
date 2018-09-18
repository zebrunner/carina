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

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.xml.XmlTest;

import com.amazonaws.services.s3.model.S3Object;
import com.qaprosoft.amazon.AmazonS3Manager;
import com.qaprosoft.carina.core.foundation.dataprovider.core.DataProviderFactory;
import com.qaprosoft.carina.core.foundation.listeners.CarinaListener;
import com.qaprosoft.carina.core.foundation.report.testrail.TestRail;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.common.CommonUtils;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;

/*
 * AbstractTest - base test for UI and API tests.
 * 
 * @author Alex Khursevich
 */
@Listeners({ CarinaListener.class })
public abstract class AbstractTest // extends DriverHelper
{
    protected static final Logger LOGGER = Logger.getLogger(AbstractTest.class);

    protected static final long EXPLICIT_TIMEOUT = Configuration.getLong(Parameter.EXPLICIT_TIMEOUT);


    @BeforeMethod(alwaysRun = true)
    public void executeBeforeTestMethod(XmlTest xmlTest, Method testMethod, ITestContext context) throws Throwable {

    }

    @AfterMethod(alwaysRun = true)
    public void executeAfterTestMethod(ITestResult result) {


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