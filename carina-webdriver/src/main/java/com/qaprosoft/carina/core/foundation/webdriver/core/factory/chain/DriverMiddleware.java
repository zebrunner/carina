package com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
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
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.chain.BrowserstackMiddleware;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.chain.CapabilitiesMiddleware;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.chain.LambdatestMiddleware;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.chain.MCloudMiddleware;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.chain.SauceLabsMiddleware;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;

public abstract class DriverMiddleware {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected CapabilitiesMiddleware capabilitiesMiddleware;
    private DriverMiddleware next;

    public DriverMiddleware() {
        this.capabilitiesMiddleware = CapabilitiesMiddleware.link(
                new BrowserstackMiddleware(),
                new MCloudMiddleware(),
                new SauceLabsMiddleware(),
                new LambdatestMiddleware());
    }

    public static DriverMiddleware link(DriverMiddleware first, DriverMiddleware... chain) {

        DriverMiddleware head = first;
        for (DriverMiddleware nextInChain : chain) {
            head.next = nextInChain;
            head = nextInChain;
        }
        return first;
    }

    protected abstract boolean isSuitable(Capabilities capabilities);

    /**
     * Creates new instance of {@link WebDriver} according to configuration capabilities
     */
    public DriverMiddleware getDriverMiddleware() {
        return this.getDriverMiddleware(null);
    }

    public DriverMiddleware getDriverMiddleware(Capabilities capabilities) {
        if (!isSuitable(capabilities)) {
            if (next == null) {
                throw new RuntimeException("Cannot choose driver");
            }
            return next.getDriverMiddleware(capabilities);
        }
        return this;
    }

    /**
     * Creates new instance of {@link WebDriver} according to specified {@link Capabilities}.
     *
     * @param testName - where driver is initiated
     * @param seleniumHost - selenium server URL
     * @param capabilities - driver capabilities
     * @return instance of {@link WebDriver}
     */
    public abstract WebDriver getDriver(String testName, String seleniumHost, Capabilities capabilities);

    protected final URL getURL(String hostUrl) {
        try {
            return new URL(hostUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed selenium URL!", e);
        }
    }

    /**
     * Sets browser window according to capabilities.resolution value, otherwise
     * maximizes window.
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
        } catch (

        Exception e) {
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
}
