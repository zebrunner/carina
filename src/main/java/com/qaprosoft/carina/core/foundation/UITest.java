/*
 * Copyright 2013 QAPROSOFT (http://qaprosoft.com/).
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
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.xml.XmlTest;

import com.qaprosoft.carina.core.foundation.listeners.UITestListener;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.DriverMode;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.android.recorder.utils.AdbExecutor;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;
import com.qaprosoft.carina.core.foundation.webdriver.DriverFactory;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;

@Listeners({ UITestListener.class })
public class UITest extends AbstractTest
{
    protected static final Logger LOGGER = Logger.getLogger(UITest.class);
    
	protected WebDriver extraDriver;
	protected static ThreadLocal<WebDriver> webDrivers = new ThreadLocal<WebDriver>();

    private static AdbExecutor executor = new AdbExecutor(Configuration.get(Parameter.ADB_HOST), Configuration.get(Parameter.ADB_PORT));
    private int adb_pid = 0;
    
    Throwable init_throwable;

	
	@Override
	protected boolean isUITest()
	{
		return true;
	}
	
    @BeforeSuite(alwaysRun = true)
    public void executeBeforeTestSuite(ITestContext context) throws Throwable
    {
    	super.executeBeforeTestSuite(context);

	    if (Configuration.getDriverMode(Parameter.DRIVER_MODE) == DriverMode.SUITE_MODE  && getDriver(true) == null)
	    {
	    	LOGGER.debug("Initialize driver in UITest->BeforeSuite.");
	    	if (!initDriver(context.getSuite().getName())) {
	    		context.setAttribute(SpecialKeywords.INITIALIZATION_FAILURE, init_throwable);
	    		throw init_throwable;
	    	}
    		setDriver(driver);
    		
    		//String sessionId = 
    		DriverPool.registerDriverSession(driver);
    		
    		DriverPool.associateTestNameWithDriver("BeforeSuite " + context.getSuite().getName(), driver);
    		
			initSummary(driver);
    		
	    }
    }
    
    @BeforeClass(alwaysRun = true)
    public void executeBeforeTestClass(ITestContext context) throws Throwable {
    	super.executeBeforeTestClass(context);    	

	    if (Configuration.getDriverMode(Parameter.DRIVER_MODE) == DriverMode.SUITE_MODE)
	    {
	    	//get from DriverPool.single_driver because everything it is deleted somehow!!
			driver = DriverPool.getSingleDriver();
			if (driver != null) {
     	    	setDriver(driver);
	    		String sessionId = DriverPool.registerDriverSession(driver);
	    		context.getCurrentXmlTest().addParameter(SpecialKeywords.SESSION_ID, sessionId);
			}
	    }
		if (Configuration.getDriverMode(Parameter.DRIVER_MODE) == DriverMode.CLASS_MODE && getDriver(true) == null)
	    {
	    	LOGGER.debug("Initialize driver in UITest->BeforeClass.");
	    	if (!initDriver(this.getClass().getName())) {
	    		context.setAttribute(SpecialKeywords.INITIALIZATION_FAILURE, init_throwable);
	    		throw init_throwable;
	    	}
    		setDriver(driver);
    		
    		String sessionId = DriverPool.registerDriverSession(driver);
    		context.getCurrentXmlTest().addParameter(SpecialKeywords.SESSION_ID, sessionId);
    		
    		DriverPool.associateTestNameWithDriver("BeforeClass " + this.getClass().getName(), driver);
    		
			initSummary(driver);
	    }    		
    }
    
    @BeforeMethod(alwaysRun = true)
    public void executeBeforeTestMethod(XmlTest xmlTest, Method testMethod, ITestContext context) throws Throwable
    {
		super.executeBeforeTestMethod(xmlTest, testMethod, context);    	
	    if (Configuration.getDriverMode(Parameter.DRIVER_MODE) == DriverMode.SUITE_MODE)
	    {
	    	//get from DriverPool.single_driver because everything it is deleted somehow!!
			driver = DriverPool.getSingleDriver();

			if (driver != null) {
     	    	setDriver(driver);
	    		String sessionId = DriverPool.registerDriverSession(driver);
	    		context.getCurrentXmlTest().addParameter(SpecialKeywords.SESSION_ID, sessionId);
			}
	    }
	    
		String test = TestNamingUtil.getCanonicalTestNameBeforeTest(xmlTest, testMethod);
    	if (Configuration.getDriverMode(Parameter.DRIVER_MODE) == DriverMode.METHOD_MODE && getDriver(true) == null)
    	{
    		LOGGER.debug("Initialize driver in UItest->BeforeMethod.");
	    	if (!initDriver(test)) {
	    		context.setAttribute(SpecialKeywords.INITIALIZATION_FAILURE, init_throwable);
	    		throw init_throwable;
	    	}	    		
    		setDriver(driver);		    		
    		
	    	String sessionId = DriverPool.registerDriverSession(driver);
	    	xmlTest.addParameter(SpecialKeywords.SESSION_ID, sessionId);
			initSummary(driver);
			
    	}
    	if (Configuration.getDriverMode(Parameter.DRIVER_MODE) == DriverMode.METHOD_MODE ||
    			Configuration.getDriverMode(Parameter.DRIVER_MODE) == DriverMode.CLASS_MODE) {
			startRecording();
    	}		    	

    	if (browserVersion.isEmpty() && getDriver() != null)
    		browserVersion = DriverFactory.getBrowserVersion(getDriver());
	}    

    @AfterMethod(alwaysRun = true)
    public void executeAfterTestMethod(ITestResult result)
    {
    	try
    	{	    
    		quitExtraDriver(); //all extraDrivers should be exited after test method!
//    		String sessionId = result.getTestContext().getCurrentXmlTest().getParameter(SpecialKeywords.SESSION_ID);
//		    WebDriver drv = getDriver(sessionId);
	
//			String testName = TestNamingUtil.getCanonicalTestName(result);
	
//		    File testLogFile = new File(ReportContext.getTestDir(testName) + "/test.log");
//		    if (!testLogFile.exists()) testLogFile.createNewFile();
//		    FileWriter fw = new FileWriter(testLogFile);
//		    
//		    if (drv != null)
//		    {
//				fw.append("\r\n**************************** UI logs ****************************\r\n\r\n");
//				
//				try
//				{
//					//fw.append(TestLogHelper.getSessionLogs(testName));
//					fw.append(TestLogHelper.getSessionLogs(drv));
//				}
//				catch (Exception e)
//				{
//				    LOGGER.error("AfterTest - unable to get test logs. " + e.getMessage());
//				}
//	
//		    }
//		    try
//		    {
//				fw.close();
//		    }
//		    catch (Exception e)
//		    {
//		    	LOGGER.error("Error during FileWriter close. " + e.getMessage());
//		    	e.printStackTrace();
//		    }	
			
	    	if (Configuration.getDriverMode(Parameter.DRIVER_MODE) == DriverMode.METHOD_MODE ||
	    			Configuration.getDriverMode(Parameter.DRIVER_MODE) == DriverMode.CLASS_MODE) {
				stopRecording(TestNamingUtil.getCanonicalTestName(result));
	    	}
	    	
	    	if (Configuration.getDriverMode(Parameter.DRIVER_MODE) == DriverMode.METHOD_MODE) {
	    		LOGGER.debug("Deinitialize driver in @AfterMethod.");
				quitDriver();
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
    	try {
    	    if (Configuration.getDriverMode(Parameter.DRIVER_MODE) == DriverMode.CLASS_MODE && getDriver() != null)
    	    {
    	    	LOGGER.debug("Deinitialize driver in UITest->AfterClass.");
    			quitDriver();
    	    }
    	}
    	catch (Throwable thr)
    	{
			context.setAttribute(SpecialKeywords.INITIALIZATION_FAILURE, thr);
			throw thr;
    	}
    	
		super.executeAfterTestClass(context);    	
    }
   
    @AfterSuite(alwaysRun = true)
    public void executeAfterTestSuite(ITestContext context)
    {
	    if (Configuration.getDriverMode(Parameter.DRIVER_MODE) == DriverMode.SUITE_MODE && getDriver() != null)
	    {
	    	LOGGER.debug("Deinitialize driver in UITest->AfterSuite.");
			quitDriver();
			stopRecording(null);
	    }
	    
		super.executeAfterTestSuite(context);	    
	}
    
	
	// --------------------------------------------------------------------------
	// Web Drivers
	// --------------------------------------------------------------------------
	protected WebDriver createExtraDriver(String driverName, DesiredCapabilities capabilities, String selenium_host) {
		if (extraDriver != null) {
			LOGGER.warn("Extra Driver is already initialized! Exsting extraDriver will be closed!");
			extraDriver.quit();
		}
		if (capabilities == null && selenium_host == null) {
			extraDriver = DriverFactory.create(driverName);	
		}
		else {
			extraDriver = DriverFactory.create(driverName, capabilities, selenium_host);
		}
    	
    	if (extraDriver == null ) {
    		Assert.fail("Unable to initialize extra driver: " + driverName + "!");
    	}
		return extraDriver;		
	}
	
	protected WebDriver createExtraDriver(final String driverName) {
		return createExtraDriver(driverName, null, null);
	}

	protected WebDriver getExtraDriver() {
		return extraDriver;
	}

	protected void quitExtraDriver() {
		if (extraDriver != null) {
			extraDriver.quit();
			extraDriver = null;
		}
	}
	
	protected WebDriver getDriver(boolean threadOnly) {
		if (webDrivers.get() != null && !webDrivers.get().toString().contains("(null")) {
			return webDrivers.get();
		} else {
			LOGGER.debug("Unable to find valid driver!");
			return null;
		}		
	}
	protected WebDriver getDriver() {
		//webDrivers.get() or simple driver
		//return webDrivers.get() != null ? webDrivers.get() : driver;
		
		if (webDrivers.get() != null && !webDrivers.get().toString().contains("(null")) {
			LOGGER.debug("Returning thread safety valid driver");
			return webDrivers.get();
		} else if (driver != null && !driver.toString().contains("null")) {
			LOGGER.debug("Returning default driver as thread safety driver absent!");
			return driver;
		} else {
			LOGGER.debug("Unable to find valid driver!");
			return null;
		}
	}
	protected WebDriver getDriver(String sessionId) {
		//sometime driver can be replaced by recovery system. 
		//in this case we should analyze if current driver closed and try to find driver in DriverPool by new sessionId
		if (webDrivers.get() == null || webDrivers.get().toString().contains("(null")) {
			driver = DriverPool.getDriverBySessionId(sessionId);
			if (driver != null) {
				setDriver(driver);
				return driver;
			}
		}
		return webDrivers.get();
	}	
 
	 
	protected static void setDriver(WebDriver driver) {
		webDrivers.set(driver);
	}

	protected static void closeDriver() {
    	webDrivers.get().close();
    }
    
	protected static void quitDriver() {
    	try {
	    	LOGGER.debug("Driver exiting..." + webDrivers.get());
    		//webDrivers.get().close();
	    	webDrivers.get().quit();
	    	LOGGER.debug("Driver exited..." + webDrivers.get());
    	}
    	catch (Exception e) {
    		LOGGER.warn("Error discovered during driver quit: " + e.getMessage());
    		LOGGER.debug("======================================================================================================================================");
    	} finally {
    		//TODO analyze how to forcibly kill session on device
	    	webDrivers.remove();
	    	//LOGGER.debug("Driver exited finally: " + webDrivers.get());
    	}
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
	
	protected boolean initDriver(String name) {
		int maxCount = Configuration.getInt(Parameter.RETRY_COUNT) + 1; //1 - is default run without retry
    	boolean init = false;
    	int count = 0;
    	while (!init & count++ < maxCount) {
    		try {
    			driver = DriverFactory.create(name);
    			init = true;
    		}
    		catch (Throwable thr) {
    			LOGGER.error(String.format("Driver initialization '%s' FAILED! Retry %d of %d time - %s", name, count, maxCount, thr.getMessage()));
    			init_throwable = thr;
    		}
    	}

    	return init;
	}
}
