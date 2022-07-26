package com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.CapabilitiesBuilder;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.OptionsType;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.IAbstractFactory;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.listener.EventFiringAppiumCommandExecutor;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.options.SupportsAutomationNameOption;

public class AndroidFactory extends IAbstractFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public WebDriver create(String testName, Capabilities capabilities, String seleniumHost) {
        Capabilities uiAutomator2Options = CapabilitiesBuilder.builder()
                .withCapabilities(capabilities)
                .chooseOptionsType(OptionsType.ANDROID_UIAUTOMATOR2_APPIUM)
                .build();

        LOGGER.debug("capabilities: {}", capabilities);

        AndroidDriver driver = new AndroidDriver(getCommandExecutor(seleniumHost), uiAutomator2Options);
        registerDevice(driver);
        return driver;
    }

    /**
     * Determines if the driver is suitable for the current capabilities
     */
    public static boolean isSuitable(Capabilities capabilities) {
        if (!capabilities.getBrowserName().isEmpty()) {
            return false;
        }

        // for localhost, browserstack, saucelabs we do not create android driver
        String customCapabilities = Configuration.get(Configuration.Parameter.CUSTOM_CAPABILITIES);
        if (!customCapabilities.isEmpty()
                && (customCapabilities.toLowerCase().contains("localhost") ||
                        customCapabilities.toLowerCase().contains("browserstack") ||
                        customCapabilities.toLowerCase().contains("saucelabs"))) {
            return false;
        }

        if (capabilities.getPlatformName().toString().equalsIgnoreCase("android") ||
                capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION)
                        .toString()
                        .equalsIgnoreCase("uiautomator2")) {
            return true;
        }

        return false;

    }

    private void registerDevice(AndroidDriver driver) {
        try {
            Device device = new Device(driver.getCapabilities());
            IDriverPool.registerDevice(device);
            // will be performed just in case uninstall_related_apps flag marked as true
            device.uninstallRelatedApps();
        } catch (Exception e) {
            // use-case when something wrong happen during initialization and registration device information.
            // the most common problem might be due to the adb connection problem

            // make sure to initiate driver quit
            LOGGER.error("Unable to register device!", e);
            // TODO: try to handle use-case if quit in this place can hangs for minutes!
            LOGGER.error("starting driver quit...");
            driver.quit();
            LOGGER.error("finished driver quit...");
            throw e;
        }

    }

    private EventFiringAppiumCommandExecutor getCommandExecutor(String seleniumHost) {
        URL hostURL;
        try {
            hostURL = new URL(seleniumHost);
        } catch (MalformedURLException e) {
            throw new RuntimeException("selenium host is invalid", e);
        }

        return new EventFiringAppiumCommandExecutor(hostURL);
    }
}
