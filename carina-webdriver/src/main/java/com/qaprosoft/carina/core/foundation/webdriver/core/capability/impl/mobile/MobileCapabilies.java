package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile;

import org.openqa.selenium.remote.DesiredCapabilities;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

public class MobileCapabilies extends AbstractCapabilities {

	@Override
	public DesiredCapabilities getCapability(String testName) {
		DesiredCapabilities capabilities = new DesiredCapabilities();

		// add capabilities based on dynamic _config.properties variables
		capabilities = initCapabilities(capabilities);

		// handle variant with extra capabilities from external property file
		DesiredCapabilities extraCapabilities = getExtraCapabilities();

		if (extraCapabilities != null) {
			capabilities.merge(extraCapabilities);
		}

		return capabilities;
	}

}
