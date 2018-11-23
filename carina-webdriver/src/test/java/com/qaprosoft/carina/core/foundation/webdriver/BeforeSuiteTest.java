package com.qaprosoft.carina.core.foundation.webdriver;

import static org.mockito.Mockito.mock;

import org.mockito.Mock;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.webdriver.TestPhase.Phase;

public class BeforeSuiteTest implements IDriverPool {

	private static final String BEFOR_CLASS_DRIVER_NAME = "custom-1-driver";
	private static final String BEFOR_METHOD_DRIVER_NAME = "custom-2-driver";
	private static final String METHOD_DRIVER_NAME = "custom-3-driver";

	@Mock
	private WebDriver mockSuiteDriver1;

	@Mock
	private WebDriver mockBeforeClassDriver;

	@Mock
	private WebDriver mockBeforeMethodDriver;

	@Mock
	private WebDriver mockMethodDriver;

	@AfterClass(alwaysRun = true)
	public void afterClass() {
		quitDrivers();
	}

	@Test
	public void beforeSuiteDriverSuccessfulRegistered() {
		R.CONFIG.put("max_driver_count", "6");
		TestPhase.setActivePhase(Phase.BEFORE_SUITE);
		this.mockSuiteDriver1 = mock(WebDriver.class);
		registerDriver(mockSuiteDriver1, IDriverPool.DEFAULT);
		Assert.assertEquals(driversPool.size(), 1,
				"Driver pool is empty after before suite driver has been registered");
		Assert.assertEquals(getDriver(), mockSuiteDriver1, "Incorrect driver has been returned");
		changeBeforeSuiteDriverThread();
	}

	@Test(dependsOnMethods = { "beforeSuiteDriverSuccessfulRegistered" }, expectedExceptions = {
			AssertionError.class }, expectedExceptionsMessageRegExp = "Driver 'default' is already registered for thread: 1")
	public void beforeClassBehaviourSameDriverName() {
		TestPhase.setActivePhase(Phase.BEFORE_CLASS);
		this.mockBeforeClassDriver = mock(WebDriver.class);
		registerDriver(mockBeforeClassDriver, IDriverPool.DEFAULT);
	}

	@Test(dependsOnMethods = { "beforeClassBehaviourSameDriverName" })
	public void beforeClassBehaviourNewDriverName() {
		TestPhase.setActivePhase(Phase.BEFORE_CLASS);
		this.mockBeforeClassDriver = mock(WebDriver.class);
		registerDriver(mockBeforeClassDriver, BEFOR_CLASS_DRIVER_NAME);
		Assert.assertEquals(driversPool.size(), 2, "driver with new name was not registered in before class");
		Assert.assertEquals(getDriver(BEFOR_CLASS_DRIVER_NAME), mockBeforeClassDriver,
				"Incorrect driver has been returned");
	}

	@Test(dependsOnMethods = { "beforeClassBehaviourNewDriverName" }, expectedExceptions = {
			AssertionError.class }, expectedExceptionsMessageRegExp = "Driver 'default' is already registered for thread: 1")
	public void beforeMethodBehaviourSameDriverName() {
		TestPhase.setActivePhase(Phase.BEFORE_METHOD);
		this.mockBeforeMethodDriver = mock(WebDriver.class);
		registerDriver(mockBeforeMethodDriver, IDriverPool.DEFAULT);
	}

	@Test(dependsOnMethods = { "beforeMethodBehaviourSameDriverName" })
	public void beforeMethodBehaviourNewDriverName() {
		TestPhase.setActivePhase(Phase.BEFORE_METHOD);
		this.mockBeforeMethodDriver = mock(WebDriver.class);
		registerDriver(mockBeforeMethodDriver, BEFOR_METHOD_DRIVER_NAME);
		Assert.assertEquals(driversPool.size(), 3, "Driver with new name was not registered in before method");
		Assert.assertEquals(getDriver(BEFOR_METHOD_DRIVER_NAME), mockBeforeMethodDriver,
				"Incorrect driver has been returned");
	}

	@Test(dependsOnMethods = { "beforeMethodBehaviourNewDriverName" }, expectedExceptions = {
			AssertionError.class }, expectedExceptionsMessageRegExp = "Driver 'default' is already registered for thread: 1")
	public void blockedMethodDriverRegistration() {
		TestPhase.setActivePhase(Phase.METHOD);
		this.mockMethodDriver = mock(WebDriver.class);
		registerDriver(mockMethodDriver, IDriverPool.DEFAULT);
	}

	@Test(dependsOnMethods = { "blockedMethodDriverRegistration" })
	public void successfulMethodDriverRegistration() {
		TestPhase.setActivePhase(Phase.METHOD);
		this.mockMethodDriver = mock(WebDriver.class);
		registerDriver(mockMethodDriver, METHOD_DRIVER_NAME);
		Assert.assertEquals(driversPool.size(), 4, "Driver with new name was not registered in the method");
		Assert.assertEquals(getDriver(METHOD_DRIVER_NAME), mockMethodDriver, "Incorrect driver has been returned");
	}

	private void changeBeforeSuiteDriverThread() {
		for (CarinaDriver cDriver : driversPool) {
			if (Phase.BEFORE_SUITE.equals(cDriver.getPhase())) {
				long newThreadID = cDriver.getThreadId() + 1;
				cDriver.setThreadId(newThreadID);
			}
		}
	}

}
