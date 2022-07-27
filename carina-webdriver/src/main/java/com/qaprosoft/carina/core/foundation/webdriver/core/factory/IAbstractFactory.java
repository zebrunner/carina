package com.qaprosoft.carina.core.foundation.webdriver.core.factory;

import java.lang.invoke.MethodHandles;
import java.time.Duration;

import org.apache.commons.lang3.ArrayUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;

public abstract class IAbstractFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Creates new instance of {@link WebDriver} according to configuration capabilities
     */
    abstract public WebDriver create(String testName, String seleniumHost);

    /**
     * Creates new instance of {@link WebDriver} according to specified {@link Capabilities}.
     *
     * @param testName - where driver is initiated
     * @param seleniumHost - selenium server URL
     * @param capabilities - driver desired capabilitues
     * @return instance of {@link WebDriver}
     */
    abstract public WebDriver create(String testName, String seleniumHost, Capabilities capabilities);

    /**
     * If any listeners specified, converts RemoteWebDriver to EventFiringWebDriver and registers all listeners.
     *
     * @param driver - instance of @link WebDriver}
     * @param listeners - instances of {@link WebDriverEventListener}
     * @return driver with registered listeners
     */
    public WebDriver registerListeners(WebDriver driver, WebDriverEventListener... listeners) {
        if (!ArrayUtils.isEmpty(listeners)) {
            driver = new EventFiringWebDriver(driver);
            for (WebDriverEventListener listener : listeners) {
                ((EventFiringWebDriver) driver).register(listener);
            }
        }
        return driver;
    }

    /**
     * Sets browser window according to capabilites.resolution value, otherwise
     * maximizes window.
     * todo refactor method
     *
     * @param driver - instance of desktop @WebDriver
     * @param capabilities - driver capabilities
     */
    protected void resizeBrowserWindow(WebDriver driver, Capabilities capabilities) {
        try {
            Wait<WebDriver> wait = new FluentWait<>(driver)
                    .pollingEvery(Duration.ofMillis(Configuration.getInt(Configuration.Parameter.RETRY_INTERVAL)))
                    .withTimeout(Duration.ofSeconds(Configuration.getInt(Configuration.Parameter.EXPLICIT_TIMEOUT)))
                    .ignoring(WebDriverException.class)
                    .ignoring(NoSuchSessionException.class)
                    .ignoring(TimeoutException.class);
            if (capabilities.getCapability("resolution") != null) {
                String resolution = (String) capabilities.getCapability("resolution");
                int expectedWidth = Integer.parseInt(resolution.split("x")[0]);
                int expectedHeight = Integer.parseInt(resolution.split("x")[1]);
                wait.until(new Function<WebDriver, Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        driver.manage().window().setPosition(new Point(0, 0));
                        driver.manage().window().setSize(new Dimension(expectedWidth, expectedHeight));
                        Dimension actualSize = driver.manage().window().getSize();
                        if (actualSize.getWidth() == expectedWidth && actualSize.getHeight() == expectedHeight) {
                            LOGGER.debug(String.format("Browser window size set to %dx%d", actualSize.getWidth(), actualSize.getHeight()));
                        } else {
                            LOGGER.warn(String.format("Expected browser window %dx%d, but actual %dx%d",
                                    expectedWidth, expectedHeight, actualSize.getWidth(), actualSize.getHeight()));
                        }
                        return true;
                    }
                });
            } else {
                wait.until(new Function<WebDriver, Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        driver.manage().window().maximize();
                        LOGGER.debug("Browser window size was maximized!");
                        return true;
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("Unable to resize browser window", e);
        }
    }

    protected void registerDevice(RemoteWebDriver driver) {
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
}
