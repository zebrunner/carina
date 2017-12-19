package com.qaprosoft.carina.core.foundation.utils.mobile;

import java.time.Duration;

import org.apache.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;

import io.appium.java_client.MobileDriver;
import io.appium.java_client.TouchAction;

public class MobileUtils {
    protected static final Logger LOGGER = Logger.getLogger(MobileUtils.class);

    public enum Direction {
        LEFT, RIGHT, UP, DOWN, VERTICAL, HORIZONTAL, VERTICAL_DOWN_FIRST, HORIZONTAL_RIGHT_FIRST
    }

    protected static final long IMPLICIT_TIMEOUT = Configuration.getLong(Parameter.IMPLICIT_TIMEOUT);

    protected static final long EXPLICIT_TIMEOUT = Configuration.getLong(Parameter.EXPLICIT_TIMEOUT);

    protected static final int MINIMUM_TIMEOUT = 2;

    private static final int DEFAULT_TOUCH_ACTION_DURATION = 1000;
    private static final int DEFAULT_MAX_SWIPE_COUNT = 5;
    
    
    
    /**
     * Tap with TouchAction by the center of element
     *
     * @param element ExtendedWebElement
     */
    public static void tap(ExtendedWebElement element) {
        Point point = element.getLocation();
        Dimension size = element.getSize();
        tap(point.getX() + size.getWidth() / 2, point.getY() + size.getHeight() / 2);
    }
    
    /**
     * Tap with TouchAction by coordinates with default 1000ms duration
     *
     * @param startx int
     * @param starty int
     */
    public static void tap(int startx, int starty) {
    	tap(startx, starty, DEFAULT_TOUCH_ACTION_DURATION);
    }
    
    
    /**
     * tap with TouchActions slowly to imitate log tap on element
     * @param elem ExtendedWebElement
     * element
     */
    public static void longTap(ExtendedWebElement elem) {
        int width = elem.getSize().getWidth();
        int height = elem.getSize().getHeight();
        
        int x = elem.getLocation().getX() + width / 2;
        int y = elem.getLocation().getY() + height / 2;
        try {
            MobileUtils.swipe(x, y, x, y, 2500);
        } catch (Exception e) {
            LOGGER.error("Exception: " + e);
        }
    }
    
    /**
     * Tap and Hold (LongPress) on element
     *
     * @param element ExtendedWebElement
     * @return boolean
     */
    public static boolean longPress(ExtendedWebElement element) {
        try {
            WebDriver driver = DriverPool.getDriver();
            TouchAction action = new TouchAction((MobileDriver<?>) driver);
            action.longPress(element.getElement()).release().perform();
            return true;
        } catch (Exception e) {
            LOGGER.info("Error occurs: " + e);
        }
        return false;
    }
    
    /**
     * Tap with TouchAction by coordinates with custom duration
     *
     * @param startx int
     * @param starty int
     * @param duration int
     */
    public static void tap(int startx, int starty, int duration) {
        TouchAction touchAction = new TouchAction((MobileDriver<?>) DriverPool.getDriver());
        touchAction.tap(startx, starty).waitAction(Duration.ofMillis(duration)).perform();
    }

    /**
     * swipe till element using TouchActions
     * 
     * @param element ExtendedWebElement
     * @return boolean
     */
    public static boolean swipe(final ExtendedWebElement element) {
    	return swipe(element, null, Direction.UP, DEFAULT_MAX_SWIPE_COUNT, DEFAULT_TOUCH_ACTION_DURATION);
    }
    
    /**
     * swipe till element using TouchActions
     * 
     * @param element ExtendedWebElement
     * @param count int
     * @return boolean
     */
    public static boolean swipe(final ExtendedWebElement element, int count) {
    	return swipe(element, null, Direction.UP, count, DEFAULT_TOUCH_ACTION_DURATION);
    }
    
    /**
     * swipe till element using TouchActions
     * 
     * @param element ExtendedWebElement
     * @param direction Direction
     * @return boolean
     */
    public static boolean swipe(final ExtendedWebElement element, Direction direction) {
    	return swipe(element, null, direction, DEFAULT_MAX_SWIPE_COUNT, DEFAULT_TOUCH_ACTION_DURATION);
    }
    
    /**
     * swipe till element using TouchActions
     * 
     * @param element ExtendedWebElement
     * @param count int
     * @param duration int
     * @return boolean
     */
    public static boolean swipe(final ExtendedWebElement element, int count, int duration) {
    	return swipe(element, null, Direction.UP, count, duration);
    }
    
    /**
     * swipe till element using TouchActions
     * 
     * @param element ExtendedWebElement
     * @param direction Direction
     * @param count int
     * @param duration int
     * @return boolean
     */
    public static boolean swipe(final ExtendedWebElement element, Direction direction, int count, int duration) {
    	return swipe(element, null, direction, count, duration);
    }
    

    /**
	 * Swipe inside container in default direction - Direction.UP
	 * Number of attempts is limited by count argument
	 * <p>
	 *
	 * @param element
	 *            ExtendedWebElement
	 * @param container
	 *            ExtendedWebElement
	 * @param count
	 *            int
	 * @return boolean
	 */
	public static boolean swipe(ExtendedWebElement element, ExtendedWebElement container, int count) {
		return swipe(element, container, Direction.UP, count, DEFAULT_TOUCH_ACTION_DURATION);
	}
	

    /**
	 * Swipe inside container in default direction - Direction.UP
	 * Number of attempts is limited by 5 
	 * <p>
	 *
	 * @param element
	 *            ExtendedWebElement
	 * @param container
	 *            ExtendedWebElement
	 * @return boolean
	 */
	public static boolean swipe(ExtendedWebElement element, ExtendedWebElement container) {
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
	 */
	public static boolean swipe(ExtendedWebElement element, ExtendedWebElement container, Direction direction) {
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
	 */
	public static boolean swipe(ExtendedWebElement element, ExtendedWebElement container, Direction direction,
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
	 */
	public static boolean swipe(ExtendedWebElement element, ExtendedWebElement container, Direction direction,
			int count, int duration) {

		
		LOGGER.debug("Verify if element present before swipe: " + element.getNameWithLocator().toString());
		boolean isPresent = element.isElementPresent(1);
		if (isPresent) {
			//no sense to continue;
			LOGGER.debug("element already present before swipe: " + element.getNameWithLocator().toString());
			return true;
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
		case VERTICAL:
			direction = Direction.UP;
			oppositeDirection = Direction.DOWN;
			bothDirections = true;
			break;
		default:
			throw new RuntimeException("Unsupported direction for swipeInContainerTillElement: " + direction);
		}
		
		int currentCount = count;

		while (!isPresent && currentCount-- > 0) {
			LOGGER.debug("Element not present! Swipe " + direction + " will be executed to element: " + element.getNameWithLocator().toString());
			swipeInDevice(container, direction, duration);	
			
			LOGGER.info("Swipe was executed. Attempts remain: " + currentCount);
			isPresent = element.isElementPresent(1);
			LOGGER.info("Result: " + isPresent);
		}

		currentCount = count;
		while (bothDirections && !isPresent && currentCount-- > 0) {
			LOGGER.debug("Element not present! Swipe " + oppositeDirection + " will be executed to element: " + element.getNameWithLocator().toString());
			swipeInDevice(container, oppositeDirection, duration);
			LOGGER.info("Swipe was executed. Attempts remain: " + currentCount);
			isPresent = element.isElementPresent(1);
			LOGGER.info("Result: " + isPresent);
		}
		
		return isPresent;
	}
	
   


    /**
     * Swipe by coordinates using TouchAction (platform independent)
     *
     * @param startx int
     * @param starty int
     * @param endx int
     * @param endy int
     * @param duration int Millis
     */
    public static void swipe(int startx, int starty, int endx, int endy, int duration) {
    	LOGGER.debug("Starting swipe...");
    	WebDriver drv = DriverPool.getDriver();
    	
    	LOGGER.debug("Getting driver dimension size...");
    	Dimension scrSize = drv.manage().window().getSize();
    	LOGGER.debug("Finished driver dimension size...");
    	//explicitly limit range of coordinates
    	if (endx > startx) {
    		endx = Math.max(endx, scrSize.width - 1);
    	} else {
    		endx = Math.max(1, endx);
    	}
    	
    	if (endy > starty) {
    		endy = Math.max(endy, scrSize.height - 1);
    	} else {
    		endy = Math.max(1, endy);
    	}
    	
		
    	LOGGER.info("startx: " + startx + "; starty: " + starty + "; endx: " + endx + "; endy: " + endy + "; duration: " + duration);
        new TouchAction((MobileDriver<?>) drv).press(startx, starty).waitAction(Duration.ofMillis(duration))
                .moveTo(endx, endy).release().perform();
        
        LOGGER.debug("Finished swipe...");
    }
    
    /**
     * swipeInDevice
     * 
     * @param container ExtendedWebElement
     * @param direction Direction
     * @param duration int
     * @return boolean
     */
    private static boolean swipeInDevice(ExtendedWebElement container, Direction direction, int duration) {

        int startx = 0;
        int starty = 0;
        int endx = 0;
        int endy = 0;
        
        Point elementLocation = null;
        Dimension elementDimensions = null;
        
    	if (container == null) {
    		//whole screen/driver is a container!
    		WebDriver driver = DriverPool.getDriver();
    		elementLocation = new Point(0, 0); //initial left corner for that case
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
    	switch (DevicePool.getDevice().getOs()) {
    	case SpecialKeywords.ANDROID:
    		minCoefficient = 0.25;
    		maxCoefficient = 0.5;
    		break;
    	case SpecialKeywords.MAC:
    		minCoefficient = 0.25;
    		maxCoefficient = 0.8;
    		break;
    	default:
    		//do nothing as default coefficients already defined!
    	}
    	
    	
        switch (direction) {
        case LEFT:
            starty = endy = elementLocation.getY() + Math.round(elementDimensions.getHeight() / 2);
            
            startx = (int) (elementLocation.getX() + Math.round(maxCoefficient * elementDimensions.getWidth()));
            endx = (int) (elementLocation.getX() + Math.round(minCoefficient * elementDimensions.getWidth()));
            break;
        case RIGHT:
            starty = endy = elementLocation.getY() + Math.round(elementDimensions.getHeight() / 2);
            
            startx = (int) (elementLocation.getX() + Math.round(minCoefficient * elementDimensions.getWidth()));
            endx = (int) (elementLocation.getX() + Math.round(maxCoefficient * elementDimensions.getWidth()));
            break;
        case UP:
            startx = endx = elementLocation.getX() + Math.round(elementDimensions.getWidth() / 2);
            
            starty = (int) (elementLocation.getY() + Math.round(maxCoefficient * elementDimensions.getHeight()));
            endy = (int) (elementLocation.getY() + Math.round(minCoefficient * elementDimensions.getHeight()));
            break;
        case DOWN:
            startx = endx = elementLocation.getX() + Math.round(elementDimensions.getWidth() / 2);
            
            starty = (int) (elementLocation.getY() + Math.round(minCoefficient * elementDimensions.getHeight()));
            endy = (int) (elementLocation.getY() + Math.round(maxCoefficient * elementDimensions.getHeight()));
            break;
		default:
			throw new RuntimeException("Unsupported direction: " + direction);
        }
        
        LOGGER.debug(String.format("Swipe from (X = %d; Y = %d) to (X = %d; Y = %d)", startx, starty, endx, endy));
        try {
            swipe(startx, starty, endx, endy, duration);
            return true;
        } catch (Exception e) {
            LOGGER.error(String.format("Error during Swipe from (X = %d; Y = %d) to (X = %d; Y = %d): %s", startx, starty, endx, endy, e));
        }
        return false;
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
     * Swipe up
     * 
     * @param duration int
     */
    public static void swipeUp(final int duration) {
        WebDriver driver = DriverPool.getDriver();
        int x = driver.manage().window().getSize().width / 2;
        int y = driver.manage().window().getSize().height;
        LOGGER.info("Swipe up will be executed.");
        //TODO: recalculate based OS if needed or remove this method completely
        swipe(x, y / 2, x, y * 4 / 5, duration);
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
     * @param duration int
     */
    public static void swipeDown(final int duration) {
        WebDriver driver = DriverPool.getDriver();
        int x = driver.manage().window().getSize().width / 2;
        int y = driver.manage().window().getSize().height / 2;
        LOGGER.info("Swipe down will be executed.");
        //TODO: recalculate based OS if needed or remove this method completely
        swipe(x, y, x, y / 2, duration);
    }
}
