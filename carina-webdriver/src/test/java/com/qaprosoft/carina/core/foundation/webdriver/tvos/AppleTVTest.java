/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
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

	@Test(groups = {"AppleTVTestClass"}, dependsOnGroups = {"DesktopCapabilitiesTestClass"})
	public void getTvOSCapabilityTest() {
		R.CONFIG.put(MOBILE_DEVICE_PLATFORM, TVOS);
		Assert.assertEquals(R.CONFIG.get(MOBILE_DEVICE_PLATFORM), TVOS);
	}

	@Test(groups = {"AppleTVTestClass"}, dependsOnGroups = {"DesktopCapabilitiesTestClass"})
	public void getTvOSDriverTypeTest() {
		DesiredCapabilities capability = new DesiredCapabilities();
		capability.setCapability(PLATFORM_NAME, TVOS);
		Assert.assertEquals(Configuration.getDriverType(capability), MOBILE);
	}

	@Test(groups = {"AppleTVTestClass"}, dependsOnGroups = {"DesktopCapabilitiesTestClass"})
	public void negativeTvOSDriverTypeTest() {
		DesiredCapabilities capability = new DesiredCapabilities();
		capability.setCapability(PLATFORM_NAME, TVOS);
		Assert.assertNotEquals(Configuration.getDriverType(capability), DESKTOP);
	}

	@Test(groups = {"AppleTVTestClass"}, dependsOnGroups = {"DesktopCapabilitiesTestClass"})
	public void getTvOSPlatformTest() {
		R.CONFIG.put(SpecialKeywords.PLATFORM_NAME, TVOS);
		Assert.assertEquals(Configuration.getDriverType(), MOBILE);
	}

	@Test(groups = {"AppleTVTestClass"}, dependsOnGroups = {"DesktopCapabilitiesTestClass"})
	public void negativeTvOSPlatformTest() {
		R.CONFIG.put(SpecialKeywords.PLATFORM_NAME, TVOS);
		Assert.assertNotEquals(Configuration.getDriverType(), DESKTOP);
	}

	@Test(groups = {"AppleTVTestClass"}, dependsOnGroups = {"DesktopCapabilitiesTestClass"})
	public void isTvOSTvTest() {
		DesiredCapabilities capability = new DesiredCapabilities();
		capability.setCapability("deviceType", TVOS);
		Assert.assertTrue(new Device(capability).isTv());
	}

	@Test(groups = {"AppleTVTestClass"}, dependsOnGroups = {"DesktopCapabilitiesTestClass"})
	public void getDeviceTypeTest() {
		DesiredCapabilities capability = new DesiredCapabilities();
		capability.setCapability("deviceType", TVOS);
		Device device = new Device(capability);
		device.setName(TVOS);
		device.setOs(TVOS);
		Assert.assertEquals(device.getDeviceType(), Type.APPLE_TV);
	}

}
