package com.qaprosoft.carina.core.foundation.webdriver;

import static org.mockito.Mockito.mock;

import org.mockito.Mock;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.R;

public class DriverPoolExTest {

	private final static String DEFAULT = "default";
	private final static String CUSTOM1 = "custom-1-driver";
	private final static String CUSTOM2 = "custom-2-driver";

	@Mock
	private WebDriver mockDriverDefault;

	@Mock
	private WebDriver mockDriverCustom1;

	@Mock
	private WebDriver mockDriverCustom2;

	@BeforeSuite(alwaysRun = true)
	public void beforeSuite() {
		R.CONFIG.put("driver_type", "desktop");
		R.CONFIG.put("driver_mode", "class_mode");
		R.CONFIG.put("max_driver_count", "2");
		this.mockDriverDefault = mock(WebDriver.class);
		this.mockDriverCustom1 = mock(WebDriver.class);
		this.mockDriverCustom2 = mock(WebDriver.class);
	}

	@Test
	public void registerDefaultDriver() {
		DriverPoolEx.registerDriver(mockDriverDefault);
		Assert.assertEquals(1,  DriverPoolEx.size(), "Number of registered driver is not valid!");
		Assert.assertTrue(DriverPoolEx.isDriverRegistered(), "Default driver is not registered!");
		Assert.assertTrue(DriverPoolEx.isDriverRegistered(DEFAULT), "Default driver is not registered!");
	}

	@Test(dependsOnMethods = { "registerDefaultDriver" })
	public void getDefaultDriver() {
		WebDriver driver = DriverPoolEx.getDriver();
		Assert.assertEquals(mockDriverDefault, driver, "Returned driver is not the same as registered!");
	}

	@Test(dependsOnMethods = "registerDefaultDriver", expectedExceptions = {
			AssertionError.class }, expectedExceptionsMessageRegExp = "Driver 'default' is already registered for thread: 1")
	public void registerTwiceDefaultDriver() {
		DriverPoolEx.registerDriver(mockDriverDefault);
	}

	@Test(dependsOnMethods = { "getDefaultDriver" })
	public void deregisterDefaultDriver() {
		DriverPoolEx.deregisterDriver();
		Assert.assertFalse(DriverPoolEx.isDriverRegistered(), "Default driver is not deregistered!");
		Assert.assertEquals(0,  DriverPoolEx.size(), "Number of registered driver is not valid!");
	}

	@Test(dependsOnMethods = { "deregisterDefaultDriver" })
	public void registerCustom1Driver() {
		DriverPoolEx.registerDriver(mockDriverCustom1, CUSTOM1);
		Assert.assertTrue(DriverPoolEx.isDriverRegistered(CUSTOM1), "Custom1 driver is not registered!");
		Assert.assertEquals(1,  DriverPoolEx.size(), "Number of registered driver is not valid!");

	}

	@Test(dependsOnMethods = "registerCustom1Driver")
	public void getCustom1Driver() {
		WebDriver driver = DriverPoolEx.getDriver(CUSTOM1);
		Assert.assertEquals(mockDriverCustom1, driver, "Returned driver is not the same as registered!");
	}

	@Test(dependsOnMethods = "getCustom1Driver", expectedExceptions = {
			AssertionError.class }, expectedExceptionsMessageRegExp = "Unable to register driver as you reached max number of drivers per thread: 2")
	public void reachMaxDriverCountTest() {
		DriverPoolEx.registerDriver(mockDriverDefault);
		
		DriverPoolEx.registerDriver(mockDriverCustom2, CUSTOM2);
		Assert.assertFalse(DriverPoolEx.isDriverRegistered(CUSTOM2),
				CUSTOM2 + " driver is registered in spite of the max_drivercount=2");
		Assert.assertEquals(2,  DriverPoolEx.size(), "Number of registered driver is not valid!");
	}

	@Test(dependsOnMethods = { "reachMaxDriverCountTest" })
	public void deregisterCustom1Driver() {
		DriverPoolEx.deregisterDriver(CUSTOM1);
		Assert.assertFalse(DriverPoolEx.isDriverRegistered(CUSTOM1), CUSTOM1 + " driver is not deregistered!");
		Assert.assertEquals(1,  DriverPoolEx.size(), "Number of registered driver is not valid!");

		DriverPoolEx.deregisterDrivers();
		Assert.assertEquals(0,  DriverPoolEx.size(), "Number of registered driver is not valid!");
	}
	
	
	@Test()
	public void deregisterAllDrivers() {
		DriverPoolEx.registerDriver(mockDriverDefault);
		Assert.assertEquals(1,  DriverPoolEx.size(), "Number of registered driver is not valid!");
		DriverPoolEx.registerDriver(mockDriverCustom1, CUSTOM1);
		Assert.assertEquals(2,  DriverPoolEx.size(), "Number of registered driver is not valid!");
		DriverPoolEx.deregisterDrivers();
		Assert.assertEquals(0,  DriverPoolEx.size(), "Number of registered driver is not valid!");
	}

}
