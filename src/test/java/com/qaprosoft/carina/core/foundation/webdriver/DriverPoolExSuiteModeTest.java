package com.qaprosoft.carina.core.foundation.webdriver;

import static org.mockito.Mockito.mock;

import org.mockito.Mock;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.R;

public class DriverPoolExSuiteModeTest {

	private final static String DEFAULT = "default";

	@Mock
	private WebDriver mockDriverDefault;


	@BeforeSuite(alwaysRun = true)
	public void beforeSuite() {
		R.CONFIG.put("driver_type", "desktop");
		R.CONFIG.put("driver_mode", "suite_mode");
		R.CONFIG.put("max_driver_count", "2");
		this.mockDriverDefault = mock(WebDriver.class);
	}

	@Test
	public void registerDefaultSuiteDriver() {
		DriverPoolEx.registerDriver(mockDriverDefault);
		Assert.assertEquals(1,  DriverPoolEx.size(), "Number of registered driver is not valid!");
		Assert.assertTrue(DriverPoolEx.isDriverRegistered(), "Default driver is not registered!");
		Assert.assertTrue(DriverPoolEx.isDriverRegistered(DEFAULT), "Default driver is not registered!");
		
		Assert.assertEquals(mockDriverDefault,  DriverPoolEx.single_driver, "Number of registered driver is not valid!");
		Assert.assertEquals(mockDriverDefault,  DriverPoolEx.getDriver(), "Number of registered driver is not valid!");
	}

	@Test(dependsOnMethods = { "registerDefaultSuiteDriver" })
	public void deregisterDefaultDriver() {
		DriverPoolEx.deregisterDriver();
		Assert.assertFalse(DriverPoolEx.isDriverRegistered(), "Default driver is not deregistered!");
		Assert.assertEquals(0,  DriverPoolEx.size(), "Number of registered driver is not valid!");
	}

}
