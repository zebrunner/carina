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
package com.zebrunner.carina.webdriver.tvos;

import static com.zebrunner.carina.utils.commons.SpecialKeywords.DESKTOP;
import static com.zebrunner.carina.utils.commons.SpecialKeywords.MOBILE;
import static com.zebrunner.carina.utils.commons.SpecialKeywords.MOBILE_DEVICE_PLATFORM;
import static com.zebrunner.carina.utils.commons.SpecialKeywords.TVOS;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.CapabilityType;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.factory.DeviceType.Type;
import com.zebrunner.carina.webdriver.device.Device;

public class AppleTVTest {

	@Test(groups = {"AppleTVTestClass"}, dependsOnGroups = {"DesktopCapabilitiesTestClass"})
	public void getTvOSCapabilityTest() {
		R.CONFIG.put(MOBILE_DEVICE_PLATFORM, TVOS);
		Assert.assertEquals(R.CONFIG.get(MOBILE_DEVICE_PLATFORM), TVOS);
	}

	@Test(groups = {"AppleTVTestClass"}, dependsOnGroups = {"DesktopCapabilitiesTestClass"})
	public void getTvOSDriverTypeTest() {
        MutableCapabilities capability = new MutableCapabilities();
        capability.setCapability(CapabilityType.PLATFORM_NAME, TVOS);
		Assert.assertEquals(Configuration.getDriverType(capability), MOBILE);
	}

	@Test(groups = {"AppleTVTestClass"}, dependsOnGroups = {"DesktopCapabilitiesTestClass"})
	public void negativeTvOSDriverTypeTest() {
        MutableCapabilities capability = new MutableCapabilities();
        capability.setCapability(CapabilityType.PLATFORM_NAME, TVOS);
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
        MutableCapabilities capability = new MutableCapabilities();
        // deviceType is not standard capability
		capability.setCapability("deviceType", TVOS);
		Assert.assertTrue(new Device(capability).isTv());
	}

	@Test(groups = {"AppleTVTestClass"}, dependsOnGroups = {"DesktopCapabilitiesTestClass"})
	public void getDeviceTypeTest() {
        MutableCapabilities capability = new MutableCapabilities();
		capability.setCapability("deviceType", TVOS);
		Device device = new Device(capability);
		device.setName(TVOS);
		device.setOs(TVOS);
		Assert.assertEquals(device.getDeviceType(), Type.APPLE_TV);
	}
}
