package com.qaprosoft.carina.core.foundation.utils.ios;

import java.time.Duration;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.qaprosoft.carina.core.foundation.utils.mobile.MobileUtils;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;

import io.appium.java_client.MobileDriver;
import io.appium.java_client.TouchAction;
import io.appium.java_client.ios.IOSDriver;

/**
 * Useful iOS utilities. For usage: import
 * com.qaprosoft.carina.core.foundation.utils.ios.IosUtils;
 *
 */
public class IosUtils extends MobileUtils {

    private static final Logger LOGGER = Logger.getLogger(IosUtils.class);

    /**
     * Swipe Until Element Presence
     *
     * @param element ExtendedWebElement
     * @return boolean
     */
    public static boolean swipeUntilElementPresence(final ExtendedWebElement element) {
        boolean isPeresent = swipeUntilElementPresence(element, 20);
        if (!isPeresent)
            return swipeUntilElementPresence(element, 20);
        return isPeresent;
    }

    /**
     * Swipe Until Element Presence for several times
     *
     * @param element ExtendedWebElement
     * @param swipeTimes int
     * @return boolean
     */
    public static boolean swipeUntilElementPresence(final ExtendedWebElement element, int swipeTimes) {
        IOSDriver<?> driver = ((IOSDriver<?>) DriverPool.getDriver());
        Dimension scrSize;
        int screenWidth;
        int screenHeight;
        boolean isPresent = element.isElementPresent(MINIMUM_TIMEOUT);
        try {
            scrSize = driver.manage().window().getSize();
            screenWidth = scrSize.getWidth() / 2;
            screenHeight = scrSize.getHeight() / 2;
            LOGGER.info("Swipe down to element: ".concat(element.toString()));
            while (!isPresent && swipeTimes-- > 0) {
                LOGGER.info("Element not present! Swipe will be executed.");
                driver.swipe(screenWidth, screenHeight, screenWidth, 10, 1000);
                LOGGER.info("Swipe was executed. Attemps remain: " + swipeTimes);
                isPresent = element.isElementPresent(MINIMUM_TIMEOUT);
                LOGGER.info("Result: " + isPresent);
            }
        } catch (Exception e) {
            LOGGER.info("Can not swipe");
        }
        return isPresent;
    }

    /**
     * Swipe Coordinates
     *
     * @param startX int
     * @param startY int
     * @param endX int
     * @param endY int
     * @param duration int
     */
    public static void swipeCoord(int startX, int startY, int endX, int endY, int duration) {
        WebDriver driver = DriverPool.getDriver();
        ((IOSDriver<?>) driver).swipe(startX, startY, endX, endY, duration);
    }

    /**
     * Swipe Coordinates
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
     * Hide Keyboard
     * 
     */
    public static void hideKeyboard() {
        try {
            ((IOSDriver<?>) DriverPool.getDriver()).hideKeyboard();
        } catch (Exception e) {
            LOGGER.info("Keyboard was already hided");
        }
    }

    /**
     * Scroll with swipe for Native Apps
     * 
     * @param direction Direction should be UP or DOWN
     * @param countSwipes int
     */
    public static void scrollWithSwipe(Direction direction, int countSwipes) {
        int coordinateToSwipe = 0;
        try {
            IOSDriver<?> driver = ((IOSDriver<?>) DriverPool.getDriver());
            Dimension dimensions = driver.manage().window().getSize();
            int screenWidth = dimensions.getWidth() / 2;
            int screenHeight = dimensions.getHeight();
            driver.context("NATIVE_APP");
            if (direction == Direction.UP) {
                coordinateToSwipe = screenHeight;
            } else if (direction == Direction.DOWN) {
                coordinateToSwipe = 5;
            } else
                LOGGER.info(String.format("Choosen wrong direction: %s! Please, use UP or DOWN", direction.toString()));
            while (countSwipes-- > 0) {
                LOGGER.info("Remain attemps to swipe: " + countSwipes);
                driver.swipe(screenWidth, screenHeight / 2, screenWidth, coordinateToSwipe, 1500);
            }
        } catch (Exception e) {
            LOGGER.info("Can not swipe");
        }
    }

    /**
     * Tap with TouchAction by coordinates
     * 
     * @param startx int
     * @param starty int
     */
    public static void tap(int startx, int starty) {
        TouchAction touchAction = new TouchAction((MobileDriver<?>) DriverPool.getDriver());
        touchAction.tap(startx, starty).perform();
    }

    /**
     * Tap several times using JS
     * 
     * @param startx int
     * @param starty int
     * @param times int
     */
    public static void tap(int startx, int starty, int times) {
        WebDriver driver = DriverPool.getDriver();

        for (int i = 0; i < times; i++) {
            LOGGER.info(String.format("Tap #: %d. X: %d. Y:%d", (i + 1), startx, starty));
            ((IOSDriver<?>) driver).executeScript("mobile: tap", new HashMap<String, Double>() {
                {
                    put("duration", 0.1);
                    put("x", (double) startx);
                    put("y", (double) starty);
                }
            });
        }
    }

    /**
     * Tap element (using TouchAction)
     * 
     * @param element WebElement
     */
    public static void tapElement(WebElement element) {
        Point point = element.getLocation();
        Dimension size = element.getSize();
        tap(point.getX() + size.getWidth() / 2, point.getY() + size.getHeight() / 2);
    }

    /**
     * Scroll with JS (up or down)
     * 
     * @param direction JSDirection
     */
    public static void scrollWithJS(JSDirection direction) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) DriverPool.getDriver();
            HashMap<String, String> scrollObject = new HashMap<String, String>();
            scrollObject.put("direction", direction.getName());
            js.executeScript("mobile: scroll", scrollObject);
        } catch (Exception e) {
            LOGGER.info("There is no space for scrolling with JS");
        }

    }

    /**
     * Swipe
     * 
     * @param startx int
     * @param starty int
     * @param endx int
     * @param endy int
     * @param duration int
     */
    public static void swipe(int startx, int starty, int endx, int endy, int duration) {
        int xOffset = endx - startx;
        int yOffset = endy - starty;
        new TouchAction((IOSDriver<?>) DriverPool.getDriver()).press(startx, starty).waitAction(Duration.ofMillis(duration)).moveTo(xOffset, yOffset)
                .release().perform();
    }

    /**
     * Swipe up
     * 
     * @param duration int
     */
    public static void swipeUp(final int duration) {
        WebDriver driver = DriverPool.getDriver();
        int x = driver.manage().window().getSize().width / 2;
        int y = driver.manage().window().getSize().height;
        LOGGER.info("Swipe up will be executed.");
        ((IOSDriver<?>) driver).swipe(x, y / 2, x, y * 4 / 5, duration);
    }

    /**
     * Swipe up several times
     * 
     * @param times int
     * @param duration int
     */
    public static void swipeUp(final int times, final int duration) {
        for (int i = 0; i < times; i++) {
            swipeUp(duration);
        }
    }

    /**
     * Swipe down several times
     * 
     * @param times int
     * @param duration int
     */
    public static void swipeDown(final int times, final int duration) {
        for (int i = 0; i < times; i++) {
            swipeDown(duration);
        }
    }

    /**
     * Swipe down
     * 
     * @param duration
     */
    public static void swipeDown(final int duration) {
        WebDriver driver = DriverPool.getDriver();
        int x = driver.manage().window().getSize().width / 2;
        int y = driver.manage().window().getSize().height / 2;
        LOGGER.info("Swipe down will be executed.");
        ((IOSDriver<?>) driver).swipe(x, y, x, y / 2, duration);
    }

}
