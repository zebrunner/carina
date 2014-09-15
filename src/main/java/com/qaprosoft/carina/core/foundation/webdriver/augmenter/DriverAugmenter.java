package com.qaprosoft.carina.core.foundation.webdriver.augmenter;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmentable;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;

public class DriverAugmenter extends Augmenter {

	public DriverAugmenter() {
		super();
	}

	@Override
	protected RemoteWebDriver extractRemoteWebDriver(WebDriver driver) {
		if (driver.getClass().isAnnotationPresent(Augmentable.class)
				|| driver
						.getClass()
						.getName()
						.startsWith(
								"org.openqa.selenium.remote.RemoteWebDriver$$EnhancerByCGLIB")
				|| driver
						.getClass()
						.getName()
						.startsWith(
								"com.qaprosoft.carina.core.foundation.webdriver")) {
			return (RemoteWebDriver) driver;
		} else {
			return null;
		}
	}
}
