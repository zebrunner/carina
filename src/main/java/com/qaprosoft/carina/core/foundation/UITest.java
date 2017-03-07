/*
 * Copyright 2013-2015 QAPROSOFT (http://qaprosoft.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qaprosoft.carina.core.foundation;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.xml.XmlTest;

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.DriverMode;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.android.recorder.utils.AdbExecutor;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;

public class UITest extends AbstractTest
{
    private static final Logger LOGGER = Logger.getLogger(UITest.class);
    
    private static AdbExecutor executor = new AdbExecutor(Configuration.get(Parameter.ADB_HOST), Configuration.get(Parameter.ADB_PORT));
    private int adb_pid = 0;
    
	@Override
	protected boolean isUITest()
	{
		return true;
	}
	
    @BeforeSuite(alwaysRun = true)
    public void executeBeforeTestSuite(ITestContext context) throws Throwable
    {
    	super.executeBeforeTestSuite(context); //do not remove super otherwise functionality from AbstractTest is not launched at all.
    	
    	String customCapabilities = Configuration.get(Parameter.CUSTOM_CAPABILITIES);
        if (!customCapabilities.isEmpty()) {
        	//redefine core properties using custom capabilities file
            Configuration.loadCoreProperties(customCapabilities);
        }
        
    	DevicePool.addDevices();
    	DriverMode driverMode = Configuration.getDriverMode();
/*    	
	    if (driverMode == DriverMode.SUITE_MODE  && getDriver() == null) //there is no need to verify on null as it is start point for all our tests 
	    {
	    	LOGGER.debug("Initialize driver in UITest->BeforeSuite.");
	    	if (!initDriver()) {
	    		throw init_throwable;
	    	}
	    	executor.screenOn();
	    }
*/	    
	    if (driverMode == DriverMode.SUITE_MODE) {
	    	executor.screenOn();
	    }

    }
    
    @BeforeClass(alwaysRun = true)
    public void executeBeforeTestClass(ITestContext context) throws Throwable {
    	super.executeBeforeTestClass(context);
    	
    	DriverMode driverMode = Configuration.getDriverMode();
		if (driverMode == DriverMode.CLASS_MODE)
	    {
	    	executor.screenOn();
	    }

/*		
	    if (driverMode == DriverMode.SUITE_MODE)
	    {
	    	//get from DriverPool.single_driver because everything it is deleted somehow!!
	    	DriverPool.replaceDriver(DriverPool.getSingleDriver());
	    }
		if (driverMode == DriverMode.CLASS_MODE && getDriver() == null)
	    {
	    	LOGGER.debug("Initialize driver in UITest->BeforeClass.");
	    	if (!initDriver()) {
	    		throw init_throwable;
	    	}
	    	executor.screenOn();
	    }
*/		
		
    }
    
    @BeforeMethod(alwaysRun = true)
    public void executeBeforeTestMethod(XmlTest xmlTest, Method testMethod, ITestContext context) throws Throwable
    {
		super.executeBeforeTestMethod(xmlTest, testMethod, context);
		// TODO: analyze below code line necessity
		// quitExtraDriver(); //quit from extra Driver to be able to proceed with single method_mode for mobile automation
		
		DriverMode driverMode = Configuration.getDriverMode();
	   	if (driverMode == DriverMode.METHOD_MODE)
    	{
	    	executor.screenOn();
    	}
		
/*	    if (driverMode == DriverMode.SUITE_MODE)
	    {
	    	//get from DriverPool.single_driver because everything it is deleted somehow!!
	    	DriverPool.replaceDriver(DriverPool.getSingleDriver());
	    }
	    

	    
		// String test = TestNamingUtil.getCanonicalTestNameBeforeTest(xmlTest, testMethod);
    	if (driverMode == DriverMode.METHOD_MODE && getDriver() == null)
    	{
    		LOGGER.debug("Initialize driver in UItest->BeforeMethod.");
	    	if (!initDriver()) {
	    		throw init_throwable;
	    	}
	    	executor.screenOn();
    	}
*/    	
    	if (driverMode == DriverMode.METHOD_MODE || driverMode == DriverMode.CLASS_MODE) {
			startRecording();
    	}		    	

/*		if (browserVersion.isEmpty() && getDriver() != null
				&& !Configuration.get(Parameter.CUSTOM_CAPABILITIES).isEmpty()) {
			browserVersion = DriverFactory.getBrowserVersion(getDriver());
		}*/
	}    

    @AfterMethod(alwaysRun = true)
    public void executeAfterTestMethod(ITestResult result)
    {
    	try
    	{	    
    		DriverMode driverMode = Configuration.getDriverMode();
			
	    	if (driverMode == DriverMode.METHOD_MODE || driverMode == DriverMode.CLASS_MODE) {
	    		stopRecording(TestNamingUtil.getCanonicalTestName(result));
	    	}
	    	
	    	if (driverMode == DriverMode.METHOD_MODE) {
	    		//TODO: analyze necessity to turn off device display after each method
	    		//executor.screenOff();
	    		LOGGER.debug("Deinitialize driver(s) in @AfterMethod.");
				quitDrivers();
			}	    	
    	}
		catch (Exception e)
		{
		    LOGGER.error("Exception in executeAfterTestMethod");
		    e.printStackTrace();
		}
    	
		super.executeAfterTestMethod(result);    	
    }
    
    @AfterClass(alwaysRun = true)
    public void executeAfterTestClass(ITestContext context) throws Throwable {
    	
	    if (Configuration.getDriverMode() == DriverMode.CLASS_MODE && getDriver() != null)
	    {
	    	executor.screenOff();
	    	LOGGER.debug("Deinitialize driver(s) in UITest->AfterClass.");
			quitDrivers();
	    }
	    
		super.executeAfterTestClass(context);    	
    }
   
    @AfterSuite(alwaysRun = true)
    public void executeAfterTestSuite(ITestContext context)
    {
	    if (Configuration.getDriverMode() == DriverMode.SUITE_MODE && getDriver() != null)
	    {
	    	executor.screenOff();
	    	LOGGER.debug("Deinitialize driver(s) in UITest->AfterSuite.");
			quitDrivers();
			stopRecording(null);
	    }
	    
		super.executeAfterTestSuite(context);	    
	}
    
	
	// --------------------------------------------------------------------------
	// Web Drivers
	// --------------------------------------------------------------------------
	protected WebDriver getDriver() {
		return getDriver(DriverPool.DEFAULT);
	}

	protected WebDriver getDriver(String name) {
		WebDriver drv = DriverPool.getDriver();
		if (drv == null) {
			Assert.fail("Unable to find/start driver!");
		}
		return drv;
	}
	 
	protected static void quitDrivers() {
		DriverPool.quitDrivers();
    }
	
	protected void startRecording() {
		if (Configuration.getBoolean(Parameter.VIDEO_RECORDING)) {
			executor.dropFile(SpecialKeywords.VIDEO_FILE_NAME);
			adb_pid = executor.startRecording(SpecialKeywords.VIDEO_FILE_NAME);
		}
	}
	
	protected void stopRecording(String test) {
		if (Configuration.getBoolean(Parameter.VIDEO_RECORDING) && adb_pid != 0) {
			executor.stopRecording(adb_pid); //stop recording
			pause(3); //very often video from device is black. trying to wait before pulling the file
			
			String videoDir = ReportContext.getBaseDir().getAbsolutePath();			
			if (test != null) {
				videoDir = ReportContext.getTestDir(test).getAbsolutePath();
			} 
			
			executor.pullFile(SpecialKeywords.VIDEO_FILE_NAME, videoDir + "/video.mp4");
		}	
	}

}
