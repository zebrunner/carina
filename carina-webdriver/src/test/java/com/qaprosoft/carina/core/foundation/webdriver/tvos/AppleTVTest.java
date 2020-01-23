package com.qaprosoft.carina.core.foundation.webdriver.tvos;

import static com.qaprosoft.carina.core.foundation.commons.SpecialKeywords.DESKTOP;
import static com.qaprosoft.carina.core.foundation.commons.SpecialKeywords.MOBILE;
import static com.qaprosoft.carina.core.foundation.commons.SpecialKeywords.MOBILE_DEVICE_PLATFORM;
import static com.qaprosoft.carina.core.foundation.commons.SpecialKeywords.TVOS;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.factory.DeviceType.Type;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;

public class AppleTVTest {

	private static final String PLATFORM_NAME = "platformName";

	@Test
	public void getTvOSCapabilityTest() {
		R.CONFIG.put(MOBILE_DEVICE_PLATFORM, TVOS);
		Assert.assertEquals(R.CONFIG.get(MOBILE_DEVICE_PLATFORM), TVOS);
	}

	@Test
	public void getTvOSDriverTypeTest() {
		DesiredCapabilities capability = new DesiredCapabilities();
		capability.setCapability(PLATFORM_NAME, TVOS);
		Assert.assertEquals(Configuration.getDriverType(capability), MOBILE);
	}

	@Test
	public void negativeTvOSDriverTypeTest() {
		DesiredCapabilities capability = new DesiredCapabilities();
		capability.setCapability(PLATFORM_NAME, TVOS);
		Assert.assertNotEquals(Configuration.getDriverType(capability), DESKTOP);
	}

	@Test
	public void getTvOSPlatformTest() {
		R.CONFIG.put(SpecialKeywords.PLATFORM_NAME, TVOS);
		Assert.assertEquals(Configuration.getDriverType(), MOBILE);
	}

	@Test
	public void negativeTvOSPlatformTest() {
		R.CONFIG.put(SpecialKeywords.PLATFORM_NAME, TVOS);
		Assert.assertNotEquals(Configuration.getDriverType(), DESKTOP);
	}

	@Test
	public void isTvOSTvTest() {
		DesiredCapabilities capability = new DesiredCapabilities();
		capability.setCapability("deviceType", TVOS);
		Assert.assertTrue(new Device(capability).isTv());
	}

	@Test
	public void getDeviceTypeTest() {
		DesiredCapabilities capability = new DesiredCapabilities();
		capability.setCapability("deviceType", TVOS);
		Device device = new Device(capability);
		device.setName(TVOS);
		device.setOs(TVOS);
		Assert.assertEquals(device.getDeviceType(), Type.APPLE_TV);
	}

}