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
package com.qaprosoft.carina.core.foundation.utils.mobile;

import static org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.DeviceRotation;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.Point;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.html5.Location;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.Messager;
import com.qaprosoft.carina.core.foundation.utils.android.AndroidService;
import com.qaprosoft.carina.core.foundation.utils.android.DeviceTimeZone;
import com.qaprosoft.carina.core.foundation.webdriver.DriverHelper;
import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;

import io.appium.java_client.HasAppStrings;
import io.appium.java_client.HasDeviceTime;
import io.appium.java_client.HasOnScreenKeyboard;
import io.appium.java_client.HidesKeyboard;
import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.LocksDevice;
import io.appium.java_client.PullsFiles;
import io.appium.java_client.PushesFiles;
import io.appium.java_client.SupportsLegacyAppManagement;
import io.appium.java_client.appmanagement.ApplicationState;
import io.appium.java_client.appmanagement.BaseActivateApplicationOptions;
import io.appium.java_client.appmanagement.BaseInstallApplicationOptions;
import io.appium.java_client.appmanagement.BaseRemoveApplicationOptions;
import io.appium.java_client.appmanagement.BaseTerminateApplicationOptions;
import io.appium.java_client.remote.SupportsContextSwitching;
import io.appium.java_client.remote.SupportsLocation;
import io.appium.java_client.remote.SupportsRotation;
import io.appium.java_client.screenrecording.BaseStartScreenRecordingOptions;
import io.appium.java_client.screenrecording.BaseStopScreenRecordingOptions;
import io.appium.java_client.screenrecording.CanRecordScreen;

public interface IMobileUtils extends IDriverPool {

    static final Logger UTILS_LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN,
        VERTICAL,
        HORIZONTAL,
        VERTICAL_DOWN_FIRST,
        HORIZONTAL_RIGHT_FIRST
    }

    public enum Zoom {
        IN,
        OUT
    }

    // TODO: [VD] make private after migration to java 9+
    /**
     * @deprecated this constant is not used in IMobileUtils
     */
    @Deprecated(forRemoval = true, since = "8.x")
    static final long EXPLICIT_TIMEOUT = Configuration.getLong(Parameter.EXPLICIT_TIMEOUT);

    /**
     * @deprecated this constant is not used in IMobileUtils
     */
    @Deprecated(forRemoval = true)
    static final int MINIMUM_TIMEOUT = 2;

    static final int DEFAULT_TOUCH_ACTION_DURATION = 1000;
    static final int DEFAULT_MAX_SWIPE_COUNT = 50;
    static final int DEFAULT_MIN_SWIPE_COUNT = 1;

    static DriverHelper helper = new DriverHelper();

    /**
     * Tap the center of element
     *
     * @param element Element to touch
     */
    default public void tap(ExtendedWebElement element) {
        UTILS_LOGGER.info("tap on {}", element.getName());
        Point point = element.getLocation();
        Dimension size = element.getSize();
        tap(point.getX() + size.getWidth() / 2, point.getY() + size.getHeight() / 2);
    }

    /**
     * Tap by coordinates with default 1000ms duration
     *
     * @param startx x coordinate
     * @param starty y coordinate
     */
    default public void tap(int startx, int starty) {
        tap(startx, starty, DEFAULT_TOUCH_ACTION_DURATION);
    }

    /**
     * Tap slowly to imitate log tap on element
     *
     * @param elem Element to long tap
     */
    default public void longTap(ExtendedWebElement elem) {
        UTILS_LOGGER.info("Long tap on {} element", elem.getName());

        Dimension size = elem.getSize();
        int width = size.getWidth();
        int height = size.getHeight();

        Point point = elem.getLocation();
        int x = point.getX() + width / 2;
        int y = point.getY() + height / 2;

        try {
            swipe(x, y, x, y, 2500);
        } catch (WebDriverException e) {
            UTILS_LOGGER.error("Exception when call longTap method: ", e);
        }
    }

    /**
     * Tap and Hold (LongPress) on element
     *
     * @param element Element to long press
     * @return is long press successful
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public boolean longPress(ExtendedWebElement element) {
        boolean isActionSuccessful = false;
        // todo change this value to optimal or try to found constant
        final Duration longPressDuration = Duration.ofMillis(1000);

        UTILS_LOGGER.info("longPress on {}", element.getName());
        // TODO: SZ migrate to FluentWaits

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Point elementLocation = element.getLocation();

        Sequence longPressSequence = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), elementLocation.getX(),
                        elementLocation.getY()))
                .addAction(finger.createPointerDown(LEFT.asArg()))
                .addAction(new Pause(finger, longPressDuration))
                .addAction(finger.createPointerUp(LEFT.asArg()));

        Interactive driver = null;

        try {
            driver = (Interactive) getDriver();
            driver.perform(List.of(longPressSequence));
            isActionSuccessful = true;
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support longPress method", e);
        } catch (WebDriverException e) {
            UTILS_LOGGER.info("Error occurs during longPress: " + e, e);
        }
        return isActionSuccessful;
    }

    /**
     * Tap by coordinates with custom duration
     *
     * @param startx x coordinate
     * @param starty y coordinate
     * @param duration touch hold time
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void tap(int startx, int starty, int duration) {
        // TODO: add Screenshot.capture()

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");

        Sequence tapSequence = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), startx, starty))
                .addAction(finger.createPointerDown(LEFT.asArg()))
                .addAction(new Pause(finger, Duration.ofMillis(duration)))
                .addAction(finger.createPointerUp(LEFT.asArg()));

        Interactive driver = null;
        try {
            driver = (Interactive) getDriver();
            driver.perform(List.of(tapSequence));
            Messager.TAP_EXECUTED.info(String.valueOf(startx), String.valueOf(starty));
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support tap method", e);
        } catch (WebDriverException e) {
            Messager.TAP_NOT_EXECUTED.error(String.valueOf(startx), String.valueOf(starty));
            throw e;
        }
    }

    /**
     * swipe till element
     *
     * @param element ExtendedWebElement
     * @return boolean
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public boolean swipe(final ExtendedWebElement element) {
        return swipe(element, null, Direction.UP, DEFAULT_MAX_SWIPE_COUNT, DEFAULT_TOUCH_ACTION_DURATION);
    }

    /**
     * Swipe till element
     *
     * @param element ExtendedWebElement
     * @param count int
     * @return boolean
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public boolean swipe(final ExtendedWebElement element, int count) {
        return swipe(element, null, Direction.UP, count, DEFAULT_TOUCH_ACTION_DURATION);
    }

    /**
     * swipe till element
     *
     * @param element ExtendedWebElement
     * @param direction Direction
     * @return boolean
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public boolean swipe(final ExtendedWebElement element, Direction direction) {
        return swipe(element, null, direction, DEFAULT_MAX_SWIPE_COUNT, DEFAULT_TOUCH_ACTION_DURATION);
    }

    /**
     * swipe till element
     *
     * @param element ExtendedWebElement
     * @param count int
     * @param duration int
     * @return boolean
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public boolean swipe(final ExtendedWebElement element, int count, int duration) {
        return swipe(element, null, Direction.UP, count, duration);
    }

    /**
     * swipe till element
     *
     * @param element ExtendedWebElement
     * @param direction Direction
     * @param count int
     * @param duration int
     * @return boolean
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public boolean swipe(final ExtendedWebElement element, Direction direction, int count, int duration) {
        return swipe(element, null, direction, count, duration);
    }

    /**
     * Swipe inside container in default direction - Direction.UP
     * Number of attempts is limited by count argument
     * <p>
     *
     * @param element ExtendedWebElement
     * @param container ExtendedWebElement
     * @param count int
     * @return boolean
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public boolean swipe(ExtendedWebElement element, ExtendedWebElement container, int count) {
        return swipe(element, container, Direction.UP, count, DEFAULT_TOUCH_ACTION_DURATION);
    }

    /**
     * Swipe inside container in default direction - Direction.UP
     * Number of attempts is limited by 5
     * <p>
     *
     * @param element ExtendedWebElement
     * @param container ExtendedWebElement
     * @return boolean
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public boolean swipe(ExtendedWebElement element, ExtendedWebElement container) {
        return swipe(element, container, Direction.UP, DEFAULT_MAX_SWIPE_COUNT, DEFAULT_TOUCH_ACTION_DURATION);
    }

    /**
     * Swipe inside container in specified direction
     * Number of attempts is limited by 5
     * <p>
     *
     * @param element
     *            ExtendedWebElement
     * @param container
     *            ExtendedWebElement
     * @param direction
     *            Direction
     * @return boolean
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public boolean swipe(ExtendedWebElement element, ExtendedWebElement container, Direction direction) {
        return swipe(element, container, direction, DEFAULT_MAX_SWIPE_COUNT, DEFAULT_TOUCH_ACTION_DURATION);
    }

    /**
     * Swipe inside container in specified direction with default pulling timeout in 1000ms
     * Number of attempts is limited by count argument
     * <p>
     *
     * @param element
     *            ExtendedWebElement
     * @param container
     *            ExtendedWebElement
     * @param direction
     *            Direction
     * @param count
     *            int
     * @return boolean
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public boolean swipe(ExtendedWebElement element, ExtendedWebElement container, Direction direction,
            int count) {
        return swipe(element, container, direction, count, DEFAULT_TOUCH_ACTION_DURATION);
    }

    /**
     * Swipe to element inside container in specified direction while element
     * will not be present on the screen. If element is on the screen already,
     * scrolling will not be performed.
     * <p>
     *
     * @param element
     *            element to which it will be scrolled
     * @param container
     *            element, inside which scrolling is expected. null to scroll
     * @param direction
     *            direction of scrolling. HORIZONTAL and VERTICAL support swiping in both directions automatically
     * @param count
     *            for how long to scroll, ms
     * @param duration
     *            pulling timeout, ms
     * @return boolean
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public boolean swipe(ExtendedWebElement element, ExtendedWebElement container, Direction direction,
            int count, int duration) {

        boolean isVisible = element.isVisible(1);
        if (isVisible) {
            // no sense to continue;
            UTILS_LOGGER.info("element already present before swipe: " + element.getNameWithLocator().toString());
            return true;
        } else {
            UTILS_LOGGER.info("swiping to element: " + element.getNameWithLocator().toString());
        }

        Direction oppositeDirection = Direction.DOWN;
        boolean bothDirections = false;

        switch (direction) {
        case UP:
            oppositeDirection = Direction.DOWN;
            break;
        case DOWN:
            oppositeDirection = Direction.UP;
            break;
        case LEFT:
            oppositeDirection = Direction.RIGHT;
            break;
        case RIGHT:
            oppositeDirection = Direction.LEFT;
            break;
        case HORIZONTAL:
            direction = Direction.LEFT;
            oppositeDirection = Direction.RIGHT;
            bothDirections = true;
            break;
        case HORIZONTAL_RIGHT_FIRST:
            direction = Direction.RIGHT;
            oppositeDirection = Direction.LEFT;
            bothDirections = true;
            break;
        case VERTICAL:
            direction = Direction.UP;
            oppositeDirection = Direction.DOWN;
            bothDirections = true;
            break;
        case VERTICAL_DOWN_FIRST:
            direction = Direction.DOWN;
            oppositeDirection = Direction.UP;
            bothDirections = true;
            break;
        default:
            throw new RuntimeException("Unsupported direction for swipeInContainerTillElement: " + direction);
        }

        int currentCount = count;

        while (!isVisible && currentCount-- > 0) {
            UTILS_LOGGER.debug("Element not present! Swipe " + direction + " will be executed to element: " + element.getNameWithLocator().toString());
            swipeInContainer(container, direction, duration);

            UTILS_LOGGER.info("Swipe was executed. Attempts remain: " + currentCount);
            isVisible = element.isVisible(1);
        }

        currentCount = count;
        while (bothDirections && !isVisible && currentCount-- > 0) {
            UTILS_LOGGER.debug(
                    "Element not present! Swipe " + oppositeDirection + " will be executed to element: " + element.getNameWithLocator().toString());
            swipeInContainer(container, oppositeDirection, duration);
            UTILS_LOGGER.info("Swipe was executed. Attempts remain: " + currentCount);
            isVisible = element.isVisible(1);
        }

        UTILS_LOGGER.info("Result: " + isVisible);
        return isVisible;
    }

    /**
     * Swipe by coordinates
     *
     * @param startx int
     * @param starty int
     * @param endx int
     * @param endy int
     * @param duration int Millis
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void swipe(int startx, int starty, int endx, int endy, int duration) {
        UTILS_LOGGER.debug("Starting swipe...");
        WebDriver drv = getDriver();
        UTILS_LOGGER.debug("Getting driver dimension size...");
        Dimension scrSize = drv.manage().window().getSize();
        UTILS_LOGGER.debug("Finished driver dimension size...");
        // explicitly limit range of coordinates
        if (endx >= scrSize.width) {
            UTILS_LOGGER.warn("endx coordinate is bigger then device width! It will be limited!");
            endx = scrSize.width - 1;
        } else {
            endx = Math.max(1, endx);
        }

        if (endy >= scrSize.height) {
            UTILS_LOGGER.warn("endy coordinate is bigger then device height! It will be limited!");
            endy = scrSize.height - 1;
        } else {
            endy = Math.max(1, endy);
        }

        UTILS_LOGGER.debug("startx: {}; starty: {}; endx: {}; endy: {}; duration: {}", startx, starty, endx, endy, duration);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");

        Sequence swipe = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), startx, starty))
                .addAction(finger.createPointerDown(LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(duration), PointerInput.Origin.viewport(), endx, endy))
                .addAction(finger.createPointerUp(LEFT.asArg()));
        Interactive driver = null;
        try {
            driver = (Interactive) drv;
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support swipe method", e);
        }

        driver.perform(List.of(swipe));
        UTILS_LOGGER.debug("Finished swipe...");
    }

    /**
     * swipeInContainer
     *
     * @param container ExtendedWebElement
     * @param direction Direction
     * @param duration int
     * @return boolean
     */
    default public boolean swipeInContainer(ExtendedWebElement container, Direction direction, int duration) {
        return swipeInContainer(container, direction, DEFAULT_MIN_SWIPE_COUNT, duration);
    }

    /**
     * swipeInContainer
     *
     * @param container ExtendedWebElement
     * @param direction Direction
     * @param count int
     * @param duration int
     * @return boolean
     */
    default public boolean swipeInContainer(ExtendedWebElement container, Direction direction, int count, int duration) {

        int startx = 0;
        int starty = 0;
        int endx = 0;
        int endy = 0;

        Point elementLocation = null;
        Dimension elementDimensions = null;

        if (container == null) {
            // whole screen/driver is a container!
            WebDriver driver = getDriver();
            elementLocation = new Point(0, 0); // initial left corner for that case

            elementDimensions = driver.manage().window().getSize();
        } else {
            if (container.isElementNotPresent(5)) {
                Assert.fail("Cannot swipe! Impossible to find element " + container.getName());
            }
            elementLocation = container.getLocation();
            elementDimensions = container.getSize();
        }

        double minCoefficient = 0.3;
        double maxCoefficient = 0.6;

        // calculate default coefficient based on OS type
        String os = IDriverPool.getDefaultDevice().getOs();
        if (os.equalsIgnoreCase(SpecialKeywords.ANDROID)) {
            minCoefficient = 0.25;
            maxCoefficient = 0.5;
        } else if (os.equalsIgnoreCase(SpecialKeywords.IOS) || os.equalsIgnoreCase(SpecialKeywords.MAC) || os.equalsIgnoreCase(SpecialKeywords.TVOS)) {
            minCoefficient = 0.25;
            maxCoefficient = 0.8;
        }

        switch (direction) {
        case LEFT:
            starty = endy = elementLocation.getY() + Math.round(elementDimensions.getHeight() / 2f);

            startx = (int) (elementLocation.getX() + Math.round(maxCoefficient * elementDimensions.getWidth()));
            endx = (int) (elementLocation.getX() + Math.round(minCoefficient * elementDimensions.getWidth()));
            break;
        case RIGHT:
            starty = endy = elementLocation.getY() + Math.round(elementDimensions.getHeight() / 2f);

            startx = (int) (elementLocation.getX() + Math.round(minCoefficient * elementDimensions.getWidth()));
            endx = (int) (elementLocation.getX() + Math.round(maxCoefficient * elementDimensions.getWidth()));
            break;
        case UP:
            startx = endx = elementLocation.getX() + Math.round(elementDimensions.getWidth() / 2f);

            starty = (int) (elementLocation.getY() + Math.round(maxCoefficient * elementDimensions.getHeight()));
            endy = (int) (elementLocation.getY() + Math.round(minCoefficient * elementDimensions.getHeight()));
            break;
        case DOWN:
            startx = endx = elementLocation.getX() + Math.round(elementDimensions.getWidth() / 2f);

            starty = (int) (elementLocation.getY() + Math.round(minCoefficient * elementDimensions.getHeight()));
            endy = (int) (elementLocation.getY() + Math.round(maxCoefficient * elementDimensions.getHeight()));
            break;
        default:
            throw new RuntimeException("Unsupported direction: " + direction);
        }

        UTILS_LOGGER.debug(String.format("Swipe from (X = %d; Y = %d) to (X = %d; Y = %d)", startx, starty, endx, endy));

        try {
            for (int i = 0; i < count; ++i) {
                swipe(startx, starty, endx, endy, duration);
            }
            return true;
        } catch (Exception e) {
            UTILS_LOGGER.error(String.format("Error during Swipe from (X = %d; Y = %d) to (X = %d; Y = %d): %s", startx, starty, endx, endy, e));
        }
        return false;
    }

    /**
     * Swipe up several times
     *
     * @param times int
     * @param duration int
     */
    default public void swipeUp(final int times, final int duration) {
        for (int i = 0; i < times; i++) {
            swipeUp(duration);
        }
    }

    /**
     * Swipe up
     *
     * @param duration int
     */
    default public void swipeUp(final int duration) {
        UTILS_LOGGER.info("Swipe up will be executed.");
        swipeInContainer(null, Direction.UP, duration);
    }

    /**
     * Swipe down several times
     *
     * @param times int
     * @param duration int
     */
    default public void swipeDown(final int times, final int duration) {
        for (int i = 0; i < times; i++) {
            swipeDown(duration);
        }
    }

    /**
     * Swipe down
     *
     * @param duration int
     */
    default public void swipeDown(final int duration) {
        UTILS_LOGGER.info("Swipe down will be executed.");
        swipeInContainer(null, Direction.DOWN, duration);
    }

    /**
     * Swipe left several times
     *
     * @param times int
     * @param duration int
     */
    default public void swipeLeft(final int times, final int duration) {
        for (int i = 0; i < times; i++) {
            swipeLeft(duration);
        }
    }

    /**
     * Swipe left
     *
     * @param duration int
     */
    default public void swipeLeft(final int duration) {
        UTILS_LOGGER.info("Swipe left will be executed.");
        swipeLeft(null, duration);
    }

    /**
     * Swipe left in container
     *
     * @param container
     *            ExtendedWebElement
     * @param duration
     *            int
     */
    default public void swipeLeft(ExtendedWebElement container, final int duration) {
        UTILS_LOGGER.info("Swipe left will be executed.");
        swipeInContainer(container, Direction.LEFT, duration);
    }

    /**
     * Swipe right several times
     *
     * @param times int
     * @param duration int
     */
    default public void swipeRight(final int times, final int duration) {
        for (int i = 0; i < times; i++) {
            swipeRight(duration);
        }
    }

    /**
     * Swipe right
     *
     * @param duration int
     */
    default public void swipeRight(final int duration) {
        UTILS_LOGGER.info("Swipe right will be executed.");
        swipeRight(null, duration);
    }

    /**
     * Swipe right in container
     *
     * @param container
     *            ExtendedWebElement
     * @param duration
     *            int
     */
    default public void swipeRight(ExtendedWebElement container, final int duration) {
        UTILS_LOGGER.info("Swipe right will be executed.");
        swipeInContainer(container, Direction.RIGHT, duration);
    }

    /**
     * Set Android Device Default TimeZone And Language based on config or to GMT and En
     * Without restoring actual focused apk.
     * 
     * @deprecated IMobileUtils should contains only methods for Android <b>and</b> IOS, so use
     *             {@link com.qaprosoft.carina.core.foundation.utils.android.IAndroidUtils#setDeviceDefaultTimeZoneAndLanguage()}
     */
    @Deprecated(forRemoval = true, since = "8.x")
    default public void setDeviceDefaultTimeZoneAndLanguage() {
        setDeviceDefaultTimeZoneAndLanguage(false);
    }

    /**
     * Set default TimeZone And Language based on config or to GMT and En
     *
     * @param returnAppFocus - if true store actual Focused apk and activity, than restore after setting Timezone and Language.
     * @deprecated IMobileUtils should contains only methods for Android <b>and</b> IOS, so use
     *             {@link com.qaprosoft.carina.core.foundation.utils.android.IAndroidUtils#setDeviceDefaultTimeZoneAndLanguage(boolean)}
     */
    @Deprecated(forRemoval = true, since = "8.x")
    default public void setDeviceDefaultTimeZoneAndLanguage(boolean returnAppFocus) {
        try {
            String baseApp = "";
            String os = IDriverPool.getDefaultDevice().getOs();
            if (os.equalsIgnoreCase(SpecialKeywords.ANDROID)) {

                AndroidService androidService = AndroidService.getInstance();

                if (returnAppFocus) {
                    baseApp = androidService.getCurrentFocusedApkDetails();
                }

                String deviceTimezone = Configuration.get(Configuration.Parameter.DEFAULT_DEVICE_TIMEZONE);
                String deviceTimeFormat = Configuration.get(Configuration.Parameter.DEFAULT_DEVICE_TIME_FORMAT);
                String deviceLanguage = Configuration.get(Configuration.Parameter.DEFAULT_DEVICE_LANGUAGE);

                DeviceTimeZone.TimeFormat timeFormat = DeviceTimeZone.TimeFormat.parse(deviceTimeFormat);
                DeviceTimeZone.TimeZoneFormat timeZone = DeviceTimeZone.TimeZoneFormat.parse(deviceTimezone);

                UTILS_LOGGER.info("Set device timezone to {}", timeZone);
                UTILS_LOGGER.info("Set device time format to {}", timeFormat);
                UTILS_LOGGER.info("Set device language to {}", deviceLanguage);

                boolean timeZoneChanged = androidService.setDeviceTimeZone(timeZone.getTimeZone(), timeZone.getSettingsTZ(), timeFormat);
                boolean languageChanged = androidService.setDeviceLanguage(deviceLanguage);

                UTILS_LOGGER.info("Device TimeZone was changed to timeZone '{}' : {}. Device Language was changed to language '{}': {}",
                        deviceTimezone,
                        timeZoneChanged, deviceLanguage, languageChanged);

                if (returnAppFocus) {
                    androidService.openApp(baseApp);
                }

            } else {
                UTILS_LOGGER.info("Current OS is {}. But we can set default TimeZone and Language only for Android.", os);
            }
        } catch (Exception e) {
            UTILS_LOGGER.error("Error while setting to device default timezone and language!", e);
        }
    }

    /**
     * Hides the keyboard if it is showing
     * 
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void hideKeyboard() {
        HidesKeyboard driver = null;
        try {
            driver = (HidesKeyboard) getDriver();
            driver.hideKeyboard();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support hideKeyboard method", e);
        } catch (Exception e) {
            if (!e.getMessage().contains("Soft keyboard not present, cannot hide keyboard")) {
                UTILS_LOGGER.error("Exception appears during hideKeyboard: ", e);
            }
        }
    }

    /**
     * Check if keyboard is showing
     *
     * @return true if keyboard is displayed. False otherwise
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public boolean isKeyboardShown() {
        HasOnScreenKeyboard driver = null;
        try {
            driver = (HasOnScreenKeyboard) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support isKeyboardShown method", e);
        }
        return driver.isKeyboardShown();
    }

    default public void zoom(Zoom type) {
        UTILS_LOGGER.info("Zoom will be performed :{}", type);
        WebDriver driver = getDriver();
        Dimension scrSize = driver.manage().window().getSize();
        int height = scrSize.getHeight();
        int width = scrSize.getWidth();

        UTILS_LOGGER.debug("Screen height : {}", height);
        UTILS_LOGGER.debug("Screen width : {}", width);

        Point point1 = new Point(width / 2, height / 2 - 30);
        Point point2 = new Point(width / 2, height / 10 * 3);
        Point point3 = new Point(width / 2, height / 2 + 30);
        Point point4 = new Point(width / 2, (7 * height) / 10);

        if (type == Zoom.OUT) {
            zoom(point1.getX(), point1.getY(), point2.getX(), point2.getY(), point3.getX(), point3.getY(), point4.getX(), point4.getY(),
                    DEFAULT_TOUCH_ACTION_DURATION);
        } else if (type == Zoom.IN) {
            zoom(point2.getX(), point2.getY(), point1.getX(), point1.getY(), point4.getX(), point4.getY(), point3.getX(), point3.getY(),
                    DEFAULT_TOUCH_ACTION_DURATION);
        }
    }

    /**
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void zoom(int startx1, int starty1, int endx1, int endy1, int startx2, int starty2, int endx2, int endy2, int duration) {
        UTILS_LOGGER.debug(
                "Zoom action will be performed with parameters : startX1 : {} ;  startY1: {} ; endX1: {} ; endY1: {}; startX2 : {} ;  startY2: {} ; endX2: {} ; endY2: {}",
                startx1, starty1, endx1, endy1, startx2, starty2, endx2, endy2);
        
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");

        Sequence zoomPartOne = new Sequence(finger, 0);
        zoomPartOne.addAction(finger.createPointerMove(Duration.ofMillis(0),
                PointerInput.Origin.viewport(), startx1, starty1));
        zoomPartOne.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        zoomPartOne.addAction(new Pause(finger, Duration.ofMillis(duration)));
        zoomPartOne.addAction(finger.createPointerMove(Duration.ofMillis(600), // todo investigate the best duration
                PointerInput.Origin.viewport(), endx1, endy1));
        zoomPartOne.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        PointerInput anotherFinger = new PointerInput(PointerInput.Kind.TOUCH, "another finger");

        Sequence zoomPartTwo = new Sequence(anotherFinger, 0);
        zoomPartTwo.addAction(anotherFinger.createPointerMove(Duration.ofMillis(0),
                PointerInput.Origin.viewport(), startx2, starty2));
        zoomPartTwo.addAction(anotherFinger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        zoomPartTwo.addAction(new Pause(anotherFinger, Duration.ofMillis(duration)));
        zoomPartTwo.addAction(anotherFinger.createPointerMove(Duration.ofMillis(600), // todo investigate the best duration
                PointerInput.Origin.viewport(), endx2, endy2));
        zoomPartTwo.addAction(anotherFinger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        Interactive driver = null;
        try {
            driver = (Interactive) getDriver();
            driver.perform(List.of(zoomPartOne, zoomPartTwo));
            UTILS_LOGGER.info("Zoom has been performed");
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support zoom method", e);
        } catch (WebDriverException e) {
            UTILS_LOGGER.error("Error during zooming", e);
        }
    }

    /**
     * Check if started driver/application is running in foreground
     *
     * @return boolean
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public boolean isAppRunning() {
        String bundleId = "";
        String os = getDevice().getOs();

        HasCapabilities driver = null;
        try {
            driver = (HasCapabilities) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support isAppRunning method", e);
        }

        Capabilities capabilities = driver.getCapabilities();
        // get bundleId or appId of the application started by driver
        if (os.equalsIgnoreCase(SpecialKeywords.ANDROID)) {
            bundleId = capabilities.getCapability(SpecialKeywords.APP_PACKAGE).toString();
        } else if (os.equalsIgnoreCase(SpecialKeywords.IOS) || os.equalsIgnoreCase(SpecialKeywords.MAC)
                || os.equalsIgnoreCase(SpecialKeywords.TVOS)) {
            bundleId = capabilities.getCapability(SpecialKeywords.BUNDLE_ID).toString();
        }

        return isAppRunning(bundleId);
    }

    /**
     * Check running in foreground application by bundleId or appId
     *
     * @param bundleId the bundle identifier for iOS (or appPackage for Android) of the app to query the state of.
     * @return true, if app's status equals {@link ApplicationState#RUNNING_IN_FOREGROUND}, false otherwise
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public boolean isAppRunning(String bundleId) {
        InteractsWithApps driver = null;
        try {
            driver = (InteractsWithApps) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support isAppRunning method", e);
        }

        ApplicationState actualApplicationState = driver.queryAppState(bundleId);
        return ApplicationState.RUNNING_IN_FOREGROUND.equals(actualApplicationState);
    }

    /**
     * Terminate running driver/application
     * 
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void terminateApp() {
        String bundleId = "";
        String os = getDevice().getOs();

        HasCapabilities driver = null;
        try {
            driver = (HasCapabilities) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support terminateApp method", e);
        }

        Capabilities capabilities = driver.getCapabilities();

        // get bundleId or appId of the application started by driver
        if (os.equalsIgnoreCase(SpecialKeywords.ANDROID)) {
            bundleId = capabilities.getCapability(SpecialKeywords.APP_PACKAGE).toString();
        } else if (os.equalsIgnoreCase(SpecialKeywords.IOS) ||
                os.equalsIgnoreCase(SpecialKeywords.MAC) ||
                os.equalsIgnoreCase(SpecialKeywords.TVOS)) {
            bundleId = capabilities.getCapability(SpecialKeywords.BUNDLE_ID).toString();
        }
        terminateApp(bundleId);
    }

    /**
     * Terminate the particular application if it is running
     *
     * @param bundleId the bundle identifier (or app id) of the app to be terminated.
     * @return true if the app was running before and has been successfully stopped
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public boolean terminateApp(String bundleId) {
        return terminateApp(bundleId, null);
    }

    /**
     * Terminate the particular application if it is running
     *
     * @param bundleId the bundle identifier (or app id) of the app to be terminated.
     * @param options the set of termination options supported by the particular platform.
     * @return true if the app was running before and has been successfully stopped
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public boolean terminateApp(String bundleId, @Nullable BaseTerminateApplicationOptions<?> options) {
        InteractsWithApps driver = null;
        try {
            driver = (InteractsWithApps) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support terminateApp method", e);
        }
        return driver.terminateApp(bundleId, options);
    }

    /**
     * The application that has its package name set to current driver's
     * capabilities will be closed to background IN CASE IT IS CURRENTLY IN
     * FOREGROUND. Will be in recent app's list;
     * 
     * @deprecated https://github.com/appium/appium/issues/1580
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    @Deprecated(since = "8.x")
    default public void closeApp() {
        UTILS_LOGGER.info("Application will be closed to background");
        SupportsLegacyAppManagement driver = null;
        try {
            driver = (SupportsLegacyAppManagement) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support closeApp method", e);
        }

        driver.closeApp();
    }

    // TODO Update this method using findByImage strategy
    /**
     * Pressing bottom right button on the keyboard by coordinates: "search", "ok",
     * "next", etc. - various keys appear at this position. Tested at Nexus 6P
     * Android 8.0.0 standard keyboard. Coefficients of coordinates for other
     * devices and custom keyboards could be different.
     * 
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void pressBottomRightKey() {
        WebDriver driver = getDriver();
        Dimension size = helper.performIgnoreException(() -> driver.manage().window().getSize());
        int height = size.getHeight();
        int width = size.getWidth();

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");

        Sequence pressBottomRightKeySequence = new Sequence(finger, 1)
                .addAction(
                        finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), (int) (width * 0.915), (int) (height * 0.945)))
                .addAction(finger.createPointerDown(LEFT.asArg()))
                .addAction(new Pause(finger, Duration.ofMillis(DEFAULT_TOUCH_ACTION_DURATION)))
                .addAction(finger.createPointerUp(LEFT.asArg()));

        Interactive drv = null;
        try {
            drv = (Interactive) driver;
            drv.perform(List.of(pressBottomRightKeySequence));
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support pressBottomRightKey method", e);
        } catch (WebDriverException e) {
            UTILS_LOGGER.error("Error when try to press bottom right key", e);
        }
    }

    default public boolean isChecked(final ExtendedWebElement element) {
        // TODO: SZ migrate to FluentWaits
        return element.isElementPresent(5)
                && (element.getElement().isSelected() || element.getAttribute("checked").equals("true"));
    }

    /**
     * Checks if an app is installed on the device
     *
     * @param packageName bundleId – bundleId of the app
     * @return true if app is installed
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public boolean isApplicationInstalled(String packageName) {
        InteractsWithApps driver = null;
        try {
            driver = (InteractsWithApps) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support isApplicationInstalled method", e);
        }

        boolean installed = driver.isAppInstalled(packageName);

        UTILS_LOGGER.info("Application by package name ({}) installed: {}", packageName, installed);
        return installed;
    }

    /**
     * Activates the given app if it installed, but not running or if it is running in the background<br>
     *
     * @param packageName bundleId – the bundle identifier (or app id) of the app to activate
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void startApp(String packageName) {
        startApp(packageName, null);
    }

    /**
     * Activates the given app if it installed, but not running or if it is running in the background<br>
     *
     * @param packageName bundleId – the bundle identifier (or app id) of the app to activate
     * @param options the set of activation options supported by the particular platform
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void startApp(String packageName, @Nullable BaseActivateApplicationOptions<?> options) {
        UTILS_LOGGER.info("Starting {}", packageName);
        InteractsWithApps driver = null;
        try {
            driver = (InteractsWithApps) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support startApp method", e);
        }
        driver.activateApp(packageName, options);
    }

    /**
     * Install an app on the mobile device
     *
     * @param apkPath path to app to install or a remote URL
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void installApp(String apkPath) {
        installApp(apkPath, null);
    }

    /**
     * Install an app on the mobile device
     *
     * @param appPath  path to app to install or a remote URL
     * @param options Set of the corresponding installation options for the particular platform
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void installApp(String appPath, @Nullable BaseInstallApplicationOptions<?> options) {
        UTILS_LOGGER.info("Will install application with apk-file from {}", appPath);
        InteractsWithApps driver = null;
        try {
            driver = (InteractsWithApps) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support installApp method", e);
        }
        driver.installApp(appPath, options);
    }

    /**
     * Remove the specified app from the device (uninstall)
     *
     * @param packageName bundleId – the bundle identifier (or app id) of the app to remove
     * @return true if the uninstall was successful
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public boolean removeApp(String packageName) {
        return removeApp(packageName, null);
    }

    /**
     * Remove the specified app from the device (uninstall)
     *
     * @param packageName bundleId – the bundle identifier (or app id) of the app to remove
     * @param options the set of uninstall options supported by the particular platform
     * @return true if the uninstall was successful
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public boolean removeApp(String packageName, @Nullable BaseRemoveApplicationOptions<?> options) {
        InteractsWithApps driver = null;
        try {
            driver = (InteractsWithApps) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support removeApp method", e);
        }

        boolean isRemoved = driver.removeApp(packageName, options);
        UTILS_LOGGER.info("Application ({}) was successfully removed: {}", packageName, isRemoved);
        return isRemoved;
    }

    /**
     * Runs the current app as a background app for the time
     * requested<br>
     * This is a synchronous method, it blocks while the
     * application is in background.
     *
     * @param duration The time to run App in background. Minimum time resolution is one millisecond.
     *            Passing zero or a negative value will switch to Home screen and return immediately.
     */
    default void runAppInBackground(Duration duration) {
        InteractsWithApps driver = null;
        try {
            driver = (InteractsWithApps) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support runAppInBackground method", e);
        }
        driver.runAppInBackground(duration);
    }

    /**
     * Method to reset test application.<br>
     * App's settings will be reset. User will be logged out. Application will be closed to background.
     * 
     * @deprecated https://github.com/appium/appium/issues/15807
     */
    @Deprecated(since = "8.x")
    default public void clearAppCache() {
        UTILS_LOGGER.info("Initiation application reset...");
        SupportsLegacyAppManagement driver = null;
        try {
            driver = (SupportsLegacyAppManagement) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support deprecated clearAppCache  method", e);
        }
        driver.resetApp();
    }

    /**
     * Switches to the given context
     *
     * @param name the name of the context to switch to
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void switchContext(String name) {
        SupportsContextSwitching driver = null;
        try {
            driver = (SupportsContextSwitching) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support switchContext  method", e);
        }
        driver.context(name);
    }

    /**
     * Get the names of available contexts
     *
     * @return list of available context names
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public Set<String> getAvailableContexts() {
        SupportsContextSwitching driver = null;
        try {
            driver = (SupportsContextSwitching) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support getAvailableContexts  method", e);
        }
        return driver.getContextHandles();
    }

    /**
     * Get the name of the current context
     *
     * @return context name or null if it cannot be determined
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public String getContext() {
        SupportsContextSwitching driver = null;
        try {
            driver = (SupportsContextSwitching) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support getContext  method", e);
        }
        return driver.getContext();
    }

    /**
     * Get device rotation
     * 
     * @return rotation, see {@link DeviceRotation}
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public DeviceRotation getRotation() {
        SupportsRotation driver = null;
        try {
            driver = (SupportsRotation) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support getContext method", e);
        }
        return driver.rotation();
    }

    /**
     * Change the rotation of the device
     * 
     * @param rotation rotation, see {@link DeviceRotation}
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void rotate(DeviceRotation rotation) {
        SupportsRotation driver = null;
        try {
            driver = (SupportsRotation) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support rotate method", e);
        }
        driver.rotate(rotation);
    }

    /**
     * Change the orientation of the device
     *
     * @param rotation – rotation, see {@link ScreenOrientation}
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void rotate(ScreenOrientation rotation) {
        SupportsRotation driver = null;
        try {
            driver = (SupportsRotation) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support rotate method", e);
        }
        driver.rotate(rotation);
    }

    /**
     * Get device orientation
     * 
     * @return orientation, see {@link ScreenOrientation}
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public ScreenOrientation getOrientation() {
        SupportsRotation driver = null;
        try {
            driver = (SupportsRotation) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support getOrientation method", e);
        }
        return driver.getOrientation();
    }

    /**
     * Gets the physical location
     *
     * @return a {@link Location} containing the location information. Returns null if the location is
     *         not available
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public Location getLocation() {
        SupportsLocation driver = null;
        try {
            driver = (SupportsLocation) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support getLocation method", e);
        }
        return driver.location();
    }

    /**
     * Set the physical location
     * 
     * @param location a {@link Location} containing the location information
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void setLocation(Location location) {
        SupportsLocation driver = null;
        try {
            driver = (SupportsLocation) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support setLocation method", e);
        }
        driver.setLocation(location);
    }

    /**
     * Gets device date and time for both iOS(host time is returned for simulators) and Android devices<br>
     * The default format is `YYYY-MM-DDTHH:mm:ssZ`, which complies to ISO-8601.
     *
     * @return device time as string
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public String getDeviceTime() {
        HasDeviceTime driver = null;
        try {
            driver = (HasDeviceTime) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support getDeviceTime method", e);
        }
        return driver.getDeviceTime();
    }

    /**
     * Gets device date and time for both iOS(host time is returned for simulators) and Android devices<br>
     *
     * @param format The set of format specifiers. Read
     *            https://momentjs.com/docs/ to get the full list of supported
     *            datetime format specifiers. The default format is
     *            `YYYY-MM-DDTHH:mm:ssZ`, which complies to ISO-8601
     * @return device time as string
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public String getDeviceTime(String format) {
        HasDeviceTime driver = null;
        try {
            driver = (HasDeviceTime) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support getDeviceTime method", e);
        }
        return driver.getDeviceTime();
    }

    /**
     * Pull a file from the remote system<br>
     * On Android the application under test should be built with debuggable flag enabled in order to get access to its container
     * on the internal file system
     *
     * @param remotePath If the path starts with <em>@applicationId/</em>/ prefix, then the file
     *            will be pulled from the root of the corresponding application container.
     *            Otherwise, the root folder is considered as / on Android and
     *            on iOS it is a media folder root (real devices only).
     * @return A byte array of Base64 encoded data
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public byte[] pullFile(String remotePath) {
        PullsFiles driver = null;
        try {
            driver = (PullsFiles) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support pullFile method", e);
        }
        return driver.pullFile(remotePath);
    }

    /**
     * Pull a folder content from the remote system<br>
     * On Android the application under test should be built with debuggable flag enabled in order to get access to its container
     * on the internal file system.
     *
     * @param remotePath If the path starts with <em>@applicationId/</em> prefix, then the folder
     *            will be pulled from the root of the corresponding application container.
     *            Otherwise, the root folder is considered as / on Android and
     *            on iOS it is a media folder root (real devices only).
     * @return A byte array of Base64 encoded zip archive data.
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public byte[] pullFolder(String remotePath) {
        PullsFiles driver = null;
        try {
            driver = (PullsFiles) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support pullFolder method", e);
        }
        return driver.pullFile(remotePath);
    }

    /**
     * Get the state of an application
     *
     * @param bundleId the bundle identifier (or app id) of the app to get the state of
     * @return state of app, one of {@link ApplicationState} value
     */
    default public ApplicationState getAppState(String bundleId) {
        InteractsWithApps driver = null;
        try {
            driver = (InteractsWithApps) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support getAppState method", e);
        }
        return driver.queryAppState(bundleId);
    }

    /**
     * Get all defined Strings from an app for the default language
     *
     * @return a map with localized strings defined in the app
     */
    default public Map<String, String> getAppStringMap() {
        HasAppStrings driver = null;
        try {
            driver = (HasAppStrings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support getAppStringMap method", e);
        }
        return driver.getAppStringMap();
    }

    /**
     * Get all defined Strings from an app for the specified language
     *
     * @param language strings language code
     * @return a map with localized strings defined in the app
     */
    default public Map<String, String> getAppStringMap(String language) {
        HasAppStrings driver = null;
        try {
            driver = (HasAppStrings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support getAppStringMap method", e);
        }
        return driver.getAppStringMap(language);
    }

    /**
     * Get all defined Strings from an app for the specified language and strings filename
     *
     * @param language strings language code
     * @param stringFile strings filename
     * @return a map with localized strings defined in the app
     */
    default public Map<String, String> getAppStringMap(String language, String stringFile) {
        HasAppStrings driver = null;
        try {
            driver = (HasAppStrings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support getAppStringMap method", e);
        }
        return driver.getAppStringMap(language, stringFile);
    }

    /**
     * This method locks a device<br>
     * It will return silently if the device is already locked
     */
    default public void lockDevice() {
        LocksDevice driver = null;
        try {
            driver = (LocksDevice) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support lockDevice method", e);
        }
        driver.lockDevice();
    }

    /**
     * Lock the device (bring it to the lock screen) for a given number of
     * seconds or forever (until the command for unlocking is called)<br>
     * The call is ignored if the device has been already locked.
     *
     * @param duration for how long to lock the screen. Minimum time resolution is one second.
     *            A negative/zero value will lock the device and return immediately.
     */
    default public void lockDevice(Duration duration) {
        LocksDevice driver = null;
        try {
            driver = (LocksDevice) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support lockDevice method", e);
        }
        driver.lockDevice(duration);
    }

    /**
     * Unlock the device if it is locked<br>
     * This method will return silently if the device is not locked
     */
    default public void unlockDevice() {
        LocksDevice driver = null;
        try {
            driver = (LocksDevice) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support unlockDevice method", e);
        }
        driver.unlockDevice();
    }

    /**
     * Check if the device is locked
     *
     * @return true if the device is locked or false otherwise.
     */
    default public boolean isDeviceLocked() {
        LocksDevice driver = null;
        try {
            driver = (LocksDevice) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support isDeviceLocked method", e);
        }
        return driver.isDeviceLocked();
    }

    /**
     * Saves base64 encoded data as a media file on the remote system.
     *
     * @param remotePath Path to file to write data to on remote device
     *            Only the filename part matters there on Simulator, so the remote end
     *            can figure out which type of media data it is and save
     *            it into a proper folder on the target device. Check
     *            'xcrun simctl addmedia' output to get more details on
     *            supported media types.
     *            If the path starts with <em>@applicationId/</em> prefix, then the file
     *            will be pushed to the root of the corresponding application container.
     * @param base64Data Base64 encoded byte array of media file data to write to remote device
     */
    default public void pushFile(String remotePath, byte[] base64Data) {
        PushesFiles driver = null;
        try {
            driver = (PushesFiles) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support pushFile method", e);
        }
        driver.pushFile(remotePath, base64Data);
    }

    /**
     * Saves base64 encoded data as a media file on the remote system
     *
     * @param remotePath See the documentation on {@link #pushFile(String, byte[])}
     * @param file Is an existing local file to be written to the remote device
     * @throws IOException when there are problems with a file or current file system
     */
    default public void pushFile(String remotePath, File file) throws IOException {
        PushesFiles driver = null;
        try {
            driver = (PushesFiles) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support pushFile method", e);
        }
        driver.pushFile(remotePath, file);
    }

    /**
     * Start asynchronous screen recording process.
     *
     * @param <T> The platform-specific {@link BaseStartScreenRecordingOptions}
     * @param options see the documentation on the {@link BaseStartScreenRecordingOptions}
     *            descendant for the particular platform.
     * @return `not used`.
     */
    @SuppressWarnings("rawtypes")
    default public <T extends BaseStartScreenRecordingOptions> String startRecordingScreen(T options) {
        CanRecordScreen driver = null;
        try {
            driver = (CanRecordScreen) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support startRecordingScreen method", e);
        }
        return driver.startRecordingScreen(options);
    }

    /**
     * Start asynchronous screen recording process with default options
     *
     * @return `not used`.
     */
    default public String startRecordingScreen() {
        CanRecordScreen driver = null;
        try {
            driver = (CanRecordScreen) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support startRecordingScreen method", e);
        }
        return driver.startRecordingScreen();
    }

    /**
     * Gather the output from the previously started screen recording to a media file
     *
     * @param <T> The platform-specific {@link BaseStopScreenRecordingOptions}
     * @param options see the documentation on the {@link BaseStopScreenRecordingOptions}
     *            descendant for the particular platform
     * @return Base-64 encoded content of the recorded media file or an empty string
     *         if the file has been successfully uploaded to a remote location (depends on the actual options)
     */
    @SuppressWarnings("rawtypes")
    default public <T extends BaseStopScreenRecordingOptions> String stopRecordingScreen(T options) {
        CanRecordScreen driver = null;
        try {
            driver = (CanRecordScreen) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support stopRecordingScreen method", e);
        }
        return driver.stopRecordingScreen(options);
    }

    /**
     * Gather the output from the previously started screen recording to a media file
     * with default options
     *
     * @return Base-64 encoded content of the recorded media file
     */
    default public String stopRecordingScreen() {
        CanRecordScreen driver = null;
        try {
            driver = (CanRecordScreen) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support stopRecordingScreen method", e);
        }
        return driver.stopRecordingScreen();
    }

}
