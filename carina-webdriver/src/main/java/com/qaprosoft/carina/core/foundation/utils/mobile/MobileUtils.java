package com.qaprosoft.carina.core.foundation.utils.mobile;

import io.appium.java_client.MobileDriver;
import io.appium.java_client.TouchAction;
import org.apache.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;

import io.appium.java_client.AppiumDriver;

import java.time.Duration;

public class MobileUtils {
    protected static final Logger LOGGER = Logger.getLogger(MobileUtils.class);

    public enum Direction {
        LEFT, RIGHT, UP, DOWN, VERTICAL, HORIZONTAL, VERTICAL_DOWN_FIRST, HORIZONTAL_RIGHT_FIRST
    }

    protected static final long IMPLICIT_TIMEOUT = Configuration.getLong(Parameter.IMPLICIT_TIMEOUT);

    protected static final long EXPLICIT_TIMEOUT = Configuration.getLong(Parameter.EXPLICIT_TIMEOUT);

    protected static final int MINIMUM_TIMEOUT = 2;

    protected static final int DEFAULT_SWIPE_TIMEOUT = 1000;

    public enum JSDirection {
        UP("up"), DOWN("down");

        String directionName;

        JSDirection(String directionName) {
            this.directionName = directionName;
        }

        public String getName() {
            return directionName;
        }

    }

    /**
     * nanoSwipe
     * 
     * @param element ExtendedWebElement
     * @param direction Direction
     */
    public static void nanoSwipe(ExtendedWebElement element, Direction direction) {
        swipeInDevice(element, direction, 0.01, 0.99, 500);
    }

    /**
     * swipeInDevice
     * 
     * @param element ExtendedWebElement
     * @param direction Direction
     */
    public static void swipeInDevice(ExtendedWebElement element, Direction direction) {
        swipeInDevice(element, direction, 0.1, 0.9, 1000);
    }

    /**
     * swipeInDevice
     * 
     * @param element ExtendedWebElement
     * @param direction Direction
     * @param minCoefficient double
     * @param maxCoefficient double
     * @param duration int
     * @return boolean
     */
    @SuppressWarnings({ "rawtypes", "incomplete-switch" })
    public static boolean swipeInDevice(ExtendedWebElement element, Direction direction, double minCoefficient, double maxCoefficient, int duration) {
        if (element.isElementNotPresent(5)) {
            LOGGER.warn("Cannot swipe! Impossible to find element " + element.getName());
            return false;
        }
        int startx = 0;
        int starty = 0;
        int endx = 0;
        int endy = 0;
        String name = element.getName();

        Point elementLocation = element.getElement().getLocation();
        Dimension elementDimensions = element.getElement().getSize();

        LOGGER.debug(String.format("'%s' location %s", name, elementLocation.toString()));
        LOGGER.debug(String.format("'%s' size %s", name, elementDimensions.toString()));
        switch (direction) {
        case LEFT:
            startx = (int) (elementLocation.getX() + Math.round(maxCoefficient * elementDimensions.getWidth()));
            starty = elementLocation.getY() + Math.round(elementDimensions.getHeight() / 2);
            endx = (int) (elementLocation.getX() + Math.round(minCoefficient * elementDimensions.getWidth()));
            endy = starty;
            break;
        case RIGHT:
            startx = (int) (elementLocation.getX() + Math.round(minCoefficient * elementDimensions.getWidth()));
            starty = elementLocation.getY() + Math.round(elementDimensions.getHeight() / 2);
            endx = (int) (elementLocation.getX() + Math.round(maxCoefficient * elementDimensions.getWidth()));
            endy = starty;
            break;
        case UP:
            startx = elementLocation.getX() + Math.round(elementDimensions.getWidth() / 2);
            starty = (int) (elementLocation.getY() + Math.round(maxCoefficient * elementDimensions.getHeight()));
            endx = startx;
            endy = (int) (elementLocation.getY() + Math.round(minCoefficient * elementDimensions.getHeight()));
            break;
        case DOWN:
            startx = elementLocation.getX() + Math.round(elementDimensions.getWidth() / 2);
            starty = (int) (elementLocation.getY() + Math.round(minCoefficient * elementDimensions.getHeight()));
            endx = startx;
            endy = (int) (elementLocation.getY() + Math.round(maxCoefficient * elementDimensions.getHeight()));
            break;
        }
        LOGGER.debug(String.format("Swipe from (X = %d; Y = %d) to (X = %d; Y = %d)", startx, starty, endx, endy));
        try {
            WebDriver driver = DriverPool.getDriver();

            ((AppiumDriver) driver).swipe(startx, starty, endx, endy, duration);
            return true;
        } catch (Exception e) {
            LOGGER.error(String.format("Error during Swipe from (X = %d; Y = %d) to (X = %d; Y = %d): %s", startx, starty, endx, endy, e));
        }
        return false;
    }

    /**
     * swipeInContainerTillElement
     * 
     * @param element ExtendedWebElement
     * @param container ExtendedWebElement
     * @return boolean
     */
    public static boolean swipeInContainerTillElement(ExtendedWebElement element, ExtendedWebElement container) {
        return swipeInContainerTillElement(element, container, 10);
    }

    /**
     * swipeInContainerTillElement
     * 
     * @param element ExtendedWebElement
     * @param container ExtendedWebElement
     * @param swipeTimes int
     * @return boolean
     */
    public static boolean swipeInContainerTillElement(ExtendedWebElement element, ExtendedWebElement container, int swipeTimes) {
        boolean isPresent = element.isElementPresent(2);
        LOGGER.info("Swipe to element: ".concat(element.getNameWithLocator().toString()));

        int defaultSwipeTimes = swipeTimes;
        while (!isPresent && swipeTimes-- > 0) {
            LOGGER.debug("Element not present! Swipe up will be executed.");
            swipeInDevice(container, Direction.UP, 0.2, 0.8, 1000);
            LOGGER.info("Swipe was executed. Attempts remain: " + swipeTimes);
            isPresent = element.isElementPresent(1);
            LOGGER.info("Result: " + isPresent);
        }

        if (!isPresent) {
            LOGGER.info("Swipe down to element: ".concat(element.getNameWithLocator().toString()));
            swipeTimes = defaultSwipeTimes;

            while (!isPresent && swipeTimes-- > 0) {
                LOGGER.debug("Element not present! Swipe down will be executed.");
                swipeInDevice(container, Direction.DOWN, 0.2, 0.8, 1000);
                LOGGER.info("Swipe was executed. Attempts remain: " + swipeTimes);
                isPresent = element.isElementPresent(1);
                LOGGER.info("Result: " + isPresent);
            }
        }

        return isPresent;
    }

    /**
     * swipeInContainerTillElementWithStartDirection
     * 
     * @param element ExtendedWebElement
     * @param container ExtendedWebElement
     * @param direction can be Direction.DOWN or Direction.UP
     * @return boolean
     */
    public static boolean swipeInContainerTillElementWithStartDirection(ExtendedWebElement element, ExtendedWebElement container,
            Direction direction) {
        int swipeTimes = 10;
        boolean isPresent = element.isElementPresent(2);
        LOGGER.info("Swipe to element: ".concat(element.getNameWithLocator().toString()));

        Direction oppositeDirection = Direction.DOWN;

        if (direction.equals(Direction.DOWN)) {
            oppositeDirection = Direction.UP;
        }

        while (!isPresent && swipeTimes-- > 0) {
            LOGGER.debug("Element not present! Swipe up will be executed.");
            swipeInDevice(container, direction, 0.2, 0.8, 1000);
            LOGGER.info("Swipe was executed. Attempts remain: " + swipeTimes);
            isPresent = element.isElementPresent(1);
            LOGGER.info("Result: " + isPresent);
        }

        if (!isPresent) {
            LOGGER.info("Swipe to element: ".concat(element.getNameWithLocator().toString()));
            swipeTimes = 10;

            while (!isPresent && swipeTimes-- > 0) {
                LOGGER.debug("Element not present! Swipe down will be executed.");
                swipeInDevice(container, oppositeDirection, 0.2, 0.8, 1000);
                LOGGER.info("Swipe was executed. Attempts remain: " + swipeTimes);
                isPresent = element.isElementPresent(1);
                LOGGER.info("Result: " + isPresent);
            }
        }

        return isPresent;
    }

    /**
     * swipeUntilElementPresence
     * 
     * @param element ExtendedWebElement
     * @param times int
     * @param duration int
     * @return boolean
     */
    public static boolean swipeUntilElementPresence(final ExtendedWebElement element, int times, int duration) {
        WebDriver driver = DriverPool.getDriver();
        Dimension scrSize;
        int x;
        int y;
        boolean isPresent = element.isElementPresent(1);
        LOGGER.info("Swipe down to element: ".concat(element.toString()));
        while (!isPresent && times-- > 0) {
            LOGGER.debug("Element not present! Swipe down will be executed.");
            LOGGER.debug("Page source: ".concat(driver.getPageSource()));
            scrSize = driver.manage().window().getSize();
            x = scrSize.width / 2;
            // swipe coordinates for ios should be less than for android to
            // avoid elements' skipping
            if (!DevicePool.getDevice().getOs().equalsIgnoreCase("android")) {
                y = scrSize.height / 4;
            } else {
                y = scrSize.height / 2;
            }
            ((AppiumDriver<?>) driver).swipe(x, y, x, y / 2, duration);
            LOGGER.info("Swipe was executed. Attempts remain: " + times);
            isPresent = element.isElementPresent(1);
            LOGGER.info("Result: " + isPresent);
        }
        if (!isPresent) {
            LOGGER.info("Swipe up to element: ".concat(element.toString()));
            while (!isPresent && times-- > 0) {
                LOGGER.debug("Element not present! Swipe up will be executed.");
                scrSize = driver.manage().window().getSize();
                x = scrSize.width / 2;
                y = scrSize.height / 2;
                ((AppiumDriver<?>) driver).swipe(x, y / 2, x, y, duration);
                LOGGER.info("Swipe was executed. Attempts remain: " + times);
                isPresent = element.isElementPresent(1);
                LOGGER.info("Result: " + isPresent);
            }
        }
        return isPresent;
    }

    public static boolean swipeUntilElementPresence(final ExtendedWebElement element, int times) {
        return swipeUntilElementPresence(element, times, 500);
    }

    /**
     * swipeInDevice
     * 
     * @param direction Direction
     * @param duration int
     */
    public static void swipeInDevice(Direction direction, int duration) {
        int startx = 0;
        int starty = 0;
        int endx = 0;
        int endy = 0;

        WebDriver driver = DriverPool.getDriver();
        Dimension dim = driver.manage().window().getSize();

        switch (direction) {
        case LEFT:
            startx = dim.width / 4;
            starty = dim.height / 2;
            endx = dim.width / 2;
            endy = starty;
            break;
        case RIGHT:
            startx = dim.width / 2;
            starty = dim.height / 2;
            endx = dim.width / 4;
            endy = starty;
            break;
        case UP:
            startx = dim.width / 2;
            starty = dim.height / 4;
            endx = startx;
            endy = dim.height / 2;
            break;
        case DOWN:
            startx = dim.width / 2;
            starty = dim.height / 2;
            endx = startx;
            endy = dim.height / 4;
            break;
        case HORIZONTAL:
        case VERTICAL:
        default:
            break;
        }
        LOGGER.debug(String.format("Swipe from (X = %d; Y = %d) to (X = %d; Y = %d)", startx, starty, endx, endy));
        try {
            ((AppiumDriver<?>) driver).swipe(startx, starty, endx, endy, duration);
        } catch (Exception e) {
            LOGGER.error(String.format("Error during Swipe from (X = %d; Y = %d) to (X = %d; Y = %d): %s", startx, starty, endx, endy, e));
        }
    }

    /**
     * swipe Coordinates
     *
     * @param startX int
     * @param startY int
     * @param endX int
     * @param endY int
     * @param duration int
     */
    public static void swipeCoord(int startX, int startY, int endX, int endY, int duration) {
        WebDriver driver = DriverPool.getDriver();
        ((AppiumDriver<?>) driver).swipe(startX, startY, endX, endY, duration);
    }

    /**
     * swipe Coordinates
     *
     * @param startX int
     * @param startY int
     * @param endX int
     * @param endY int
     */
    public static void swipeCoord(int startX, int startY, int endX, int endY) {
        swipeCoord(startX, startY, endX, endY, DEFAULT_SWIPE_TIMEOUT);
    }

    /**
     * Example of swipe By TouchAction (platform independent)
     * Should be checked on different applications.
     * If ok and will work on latest java-appium-client than all swipe methods should be updated.
     *
     * @param startx int
     * @param starty int
     * @param endx int
     * @param endy int
     * @param duration int Millis
     */
    public static void swipeByTouchAction(int startx, int starty, int endx, int endy, int duration) {
        int xOffset = endx - startx;
        int yOffset = endy - starty;
        new TouchAction((MobileDriver<?>) DriverPool.getDriver()).press(startx, starty).waitAction(Duration.ofMillis(duration))
                .moveTo(xOffset, yOffset).release().perform();
    }
}
