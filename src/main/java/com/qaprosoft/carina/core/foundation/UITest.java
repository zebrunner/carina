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
import org.apache.log4j.NDC;
import org.openqa.selenium.WebDriver;
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
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;

@Listeners({ UITestListener.class })
public class UITest extends AbstractTest
{
    private static final Logger LOGGER = Logger.getLogger(UITest.class);
    private static int driverInitCount = Configuration.getInt(Parameter.INIT_RETRY_COUNT) + 1; //1 - is default run without retry
    
    protected WebDriver driver;
    
	protected static ThreadLocal<WebDriver> webDrivers = new ThreadLocal<WebDriver>();

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
        
    	DevicePool.registerDevices();
    	DriverMode driverMode = Configuration.getDriverMode();
    	
	    if (driverMode == DriverMode.SUITE_MODE/*  && getDriver() == null*/) //there is no need to verify on null as it is start point for all our tests 
	    {
	    	LOGGER.debug("Initialize driver in UITest->BeforeSuite.");
	    	if (!initDriver(context.getSuite().getName(), driverInitCount)) {
	    		throw init_throwable;
	    	}
	    	executor.screenOn();
	    }

    }
    
    @BeforeClass(alwaysRun = true)
    public void executeBeforeTestClass(ITestContext context) throws Throwable {
    	super.executeBeforeTestClass(context);
    	
    	DriverMode driverMode = Configuration.getDriverMode();

	    if (driverMode == DriverMode.SUITE_MODE)
	    {
	    	//get from DriverPool.single_driver because everything it is deleted somehow!!
			driver = DriverPool.getSingleDriver();
			if (driver != null) {
     	    	setDriver(driver);
			}
	    }
		if (driverMode == DriverMode.CLASS_MODE && getDriver() == null)
	    {
	    	LOGGER.debug("Initialize driver in UITest->BeforeClass.");
	    	if (!initDriver(this.getClass().getName(), driverInitCount)) {
	    		throw init_throwable;
	    	}
	    	executor.screenOn();
	    }
		
		
    }
    
    @BeforeMethod(alwaysRun = true)
    public void executeBeforeTestMethod(XmlTest xmlTest, Method testMethod, ITestContext context) throws Throwable
    {
		super.executeBeforeTestMethod(xmlTest, testMethod, context);
		quitExtraDriver(); //quit from extra Driver to be able to proceed with single method_mode for mobile automation
		
		DriverMode driverMode = Configuration.getDriverMode();
		
	    if (driverMode == DriverMode.SUITE_MODE)
	    {
	    	//get from DriverPool.single_driver because everything it is deleted somehow!!
			driver = DriverPool.getSingleDriver();

			if (driver != null) {
     	    	setDriver(driver);
			}
	    }
	    

	    
		String test = TestNamingUtil.getCanonicalTestNameBeforeTest(xmlTest, testMethod);
    	if (driverMode == DriverMode.METHOD_MODE && getDriver() == null)
    	{
    		LOGGER.debug("Initialize driver in UItest->BeforeMethod.");
	    	if (!initDriver(test, driverInitCount)) {
	    		throw init_throwable;
	    	}
	    	executor.screenOn();
    	}
    	if (driverMode == DriverMode.METHOD_MODE || driverMode == DriverMode.CLASS_MODE) {
			startRecording();
    	}		    	

		if (browserVersion.isEmpty() && getDriver() != null
				&& !Configuration.get(Parameter.CUSTOM_CAPABILITIES).isEmpty()) {
			browserVersion = DriverFactory.getBrowserVersion(getDriver());
		}
	}    

    @AfterMethod(alwaysRun = true)
    public void executeAfterTestMethod(ITestResult result)
    {
    	try
    	{	    
    		quitExtraDriver(); 
    		DriverMode driverMode = Configuration.getDriverMode();
			
	    	if (driverMode == DriverMode.METHOD_MODE || driverMode == DriverMode.CLASS_MODE) {
	    		stopRecording(TestNamingUtil.getCanonicalTestName(result));
	    	}
	    	
	    	if (driverMode == DriverMode.METHOD_MODE) {
	    		//TODO: analyze necessity to turn off device display after each method
	    		//executor.screenOff();
	    		LOGGER.debug("Deinitialize driver in @AfterMethod.");
				quitDriver();
				
				DriverPool.deregisterBrowserMobProxy();
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
    	
		quitExtraDriver();
	    if (Configuration.getDriverMode() == DriverMode.CLASS_MODE && getDriver() != null)
	    {
	    	executor.screenOff();
	    	LOGGER.debug("Deinitialize driver in UITest->AfterClass.");
			quitDriver();
			
			DriverPool.deregisterBrowserMobProxy();
	    }
	    
		super.executeAfterTestClass(context);    	
    }
   
    @AfterSuite(alwaysRun = true)
    public void executeAfterTestSuite(ITestContext context)
    {
    	quitExtraDriver();
	    if (Configuration.getDriverMode() == DriverMode.SUITE_MODE && getDriver() != null)
	    {
	    	executor.screenOff();
	    	LOGGER.debug("Deinitialize driver in UITest->AfterSuite.");
			quitDriver();
			stopRecording(null);
			
			DriverPool.deregisterBrowserMobProxy();
	    }
	    
		super.executeAfterTestSuite(context);	    
	}
    
	
	// --------------------------------------------------------------------------
	// Web Drivers
	// --------------------------------------------------------------------------
	protected WebDriver getDriver() {
		long threadId = Thread.currentThread().getId();
		WebDriver drv = DriverPool.getDriverByThread(threadId);
		if (drv == null) {
			LOGGER.debug("Unable to find valid driver using threadId: " + threadId);
		}
		return drv;
	}
	 
	protected void setDriver(WebDriver driver) {
		webDrivers.set(driver);
	}
    
	protected boolean initDriver(String name, int maxCount) {
    	boolean init = false;
    	int count = 0;
    	while (!init & count++ < maxCount) {
    		try {
    			LOGGER.debug("initDriver start...");
    			
    			Device device = DevicePool.registerDevice2Thread();
    			
    			LOGGER.debug("DriverFactory start...");
    			WebDriver drv = DriverFactory.create(name, device);
    			LOGGER.debug("DriverFactory finish...");
    			DriverPool.registerDriver(drv);
    			
    			driver = drv;
    			setDriver(drv);
    			init = true;
    			// push custom device name for log4j default messages
    			if (device != null) {
    				NDC.push(" [" + device.getName() + "] ");
    			}
    			
    			LOGGER.debug("initDriver finish...");
    		}
    		catch (Throwable thr) {
    			//DevicePool.ignoreDevice();
    			DevicePool.deregisterDeviceFromThread();
    			LOGGER.error(String.format("Driver initialization '%s' FAILED! Retry %d of %d time - %s", name, count, maxCount, thr.getMessage()));
    			init_throwable = thr;
    			pause(Configuration.getInt(Parameter.INIT_RETRY_INTERVAL));
    		}
    	}

    	return init;
	}
	
	protected static void quitDriver() {
		long threadId = Thread.currentThread().getId();
		WebDriver drv = DriverPool.getDriver();
		
		try {
			if (drv == null) {
				LOGGER.error("Unable to find valid driver using threadId: " + threadId);
			}

			LOGGER.debug("Driver exiting..." + drv);
	    	DriverPool.deregisterDriver();
	    	DevicePool.deregisterDeviceFromThread();
			drv.quit();
			
	    	LOGGER.debug("Driver exited..." + drv);
		} catch (Exception e) {
    		LOGGER.warn("Error discovered during driver quit: " + e.getMessage());
    		LOGGER.debug("======================================================================================================================================");
		} finally {
    		//TODO analyze how to forcibly kill session on device
			NDC.pop();
	    	webDrivers.remove();
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
	
}
