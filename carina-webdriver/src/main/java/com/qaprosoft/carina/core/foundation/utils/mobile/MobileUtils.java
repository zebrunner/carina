package com.qaprosoft.carina.core.foundation.utils.mobile;

import java.time.Duration;

import org.apache.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;

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

    @Deprecated
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
     * nanoSwipe (1% of display) to desired direction
     * 
     * @param element ExtendedWebElement
     * @param direction Direction
     */
    public static void nanoSwipe(ExtendedWebElement element, Direction direction) {
        swipeInDevice(element, direction, 0.01, 0.99, 500);
    }

    /**
	 * Scroll inside container in default direction - Direction.UP
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
	public static boolean swipeInContainerTillElement(ExtendedWebElement element, ExtendedWebElement container, int count) {
		return swipeInContainerTillElement(element, container, Direction.UP, count);
	}
	

    /**
	 * Scroll inside container in default direction - Direction.UP
	 * Number of attempts is limited by 10 
	 * <p>
	 *
	 * @param element
	 *            ExtendedWebElement
	 * @param container
	 *            ExtendedWebElement
	 * @return boolean
	 */
	public static boolean swipeInContainerTillElement(ExtendedWebElement element, ExtendedWebElement container) {
		return swipeInContainerTillElement(element, container, Direction.UP);
	}

    /**
	 * Scroll inside container in specified direction 
	 * Number of attempts is limited by 10 
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
	public static boolean swipeInContainerTillElement(ExtendedWebElement element, ExtendedWebElement container,
			Direction direction) {
		return swipeInContainerTillElement(element, container, direction, 10);
	}

    /**
	 * Scroll inside container in specified direction with default pulling timeout in 1000 ms
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
	public static boolean swipeInContainerTillElement(ExtendedWebElement element, ExtendedWebElement container,
			Direction direction, int count) {
		return swipeInContainerTillElement(element, container, direction, count, DEFAULT_TOUCH_ACTION_DURATION);
	}
	
	
	
	/**
	 * Scroll to element inside container in specified direction while element
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
	//TODO: rename later to unified swipeTillElement
	public static boolean swipeInContainerTillElement(ExtendedWebElement element, ExtendedWebElement container, Direction direction, 
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
			if (container != null) {
				swipeInDevice(container, direction, 0.2, 0.8, duration);	
			} else {
				swipeInDevice(direction, duration);
			}
			
			LOGGER.info("Swipe was executed. Attempts remain: " + currentCount);
			isPresent = element.isElementPresent(1);
			LOGGER.info("Result: " + isPresent);
		}

		currentCount = count;
		while (bothDirections && !isPresent && currentCount-- > 0) {
			LOGGER.debug("Element not present! Swipe " + oppositeDirection + " will be executed to element: " + element.getNameWithLocator().toString());
			if (container != null) {
				swipeInDevice(container, oppositeDirection, 0.2, 0.8, duration);
			} else {
				swipeInDevice(direction, duration);
			}
			LOGGER.info("Swipe was executed. Attempts remain: " + currentCount);
			isPresent = element.isElementPresent(1);
			LOGGER.info("Result: " + isPresent);
		}
		
		return isPresent;
	}
	
	@Deprecated
	//TODO: remove after migrating all projects to use valid methods
	public static boolean swipeInContainerTillElementWithStartDirection(ExtendedWebElement element, ExtendedWebElement container,
			Direction direction) {
		return swipeInContainerTillElement(element, container, direction, 10);
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
        case RIGHT:
            startx = dim.width / 4;
            starty = dim.height / 2;
            endx = dim.width / 2;
            endy = starty;
            break;
        case LEFT:
            startx = dim.width / 2;
            starty = dim.height / 2;
            endx = dim.width / 4;
            endy = starty;
            break;
        case DOWN:
            startx = endx = dim.width / 2;
            starty = dim.height / 4; // from 25% of height
            endy = dim.height * 4 / 5; // to 80% of height
            break;
        case UP:
            startx = endx = dim.width / 2;
            starty = dim.height * 4 / 5; // from 80% of height
            endy = dim.height / 4; // // to 25% of height
            break;
        case HORIZONTAL:
        case VERTICAL:
        default:
        	throw new RuntimeException("Unsupported direction for swipeInDevice: " + direction);
        }
        LOGGER.debug(String.format("Swipe from (X = %d; Y = %d) to (X = %d; Y = %d)", startx, starty, endx, endy));
        try {
            swipe(startx, starty, endx, endy, duration);
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
    @Deprecated
    public static void swipeCoord(int startX, int startY, int endX, int endY, int duration) {
    	swipe(startX, startY, endX, endY, duration);
    }

    /**
     * swipe Coordinates
     *
     * @param startX int
     * @param startY int
     * @param endX int
     * @param endY int
     */
    @Deprecated
    public static void swipeCoord(int startX, int startY, int endX, int endY) {
        swipe(startX, startY, endX, endY, DEFAULT_TOUCH_ACTION_DURATION);
    }

    // *************************** TouchActions ********************************* //

    /**
     * Tap with TouchAction by coordinates
     *
     * @param startx int
     * @param starty int
     */
    public static void tap(int startx, int starty) {
    	tap(startx, starty, DEFAULT_TOUCH_ACTION_DURATION);
    }
    
    /**
     * Tap with TouchAction by coordinates
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
     * swipeTillElement Using TouchActions
     * 
     * @param element ExtendedWebElement
     * @return boolean
     */
    //swipeUntilElementPresence -> swipeTillElement
    public static boolean swipeTillElement(final ExtendedWebElement element) {
    	return swipeTillElement(element, DEFAULT_MAX_SWIPE_COUNT, DEFAULT_TOUCH_ACTION_DURATION);
    }
    
    /**
     * swipeTillElement Using TouchActions
     * 
     * @param element ExtendedWebElement
     * @param count int
     * @return boolean
     */
    public static boolean swipeTillElement(final ExtendedWebElement element, int count) {
    	return swipeTillElement(element, count, DEFAULT_TOUCH_ACTION_DURATION);
    }
    
    /**
     * swipeTillElement Using TouchActions
     * 
     * @param element ExtendedWebElement
     * @param direction Direction
     * @return boolean
     */
    public static boolean swipeTillElement(final ExtendedWebElement element, Direction direction) {
    	return swipeTillElement(element, direction, DEFAULT_MAX_SWIPE_COUNT, DEFAULT_TOUCH_ACTION_DURATION);
    }
    
    /**
     * swipeTillElement Using TouchActions
     * 
     * @param element ExtendedWebElement
     * @param count int
     * @param duration int
     * @return boolean
     */
    public static boolean swipeTillElement(final ExtendedWebElement element, int count, int duration) {
    	return swipeTillElement(element, Direction.UP, count, duration);
    }
    
    /**
     * swipeTillElement Using TouchActions
     * 
     * @param element ExtendedWebElement
     * @param direction Direction
     * @param count int
     * @param duration int
     * @return boolean
     */
    public static boolean swipeTillElement(final ExtendedWebElement element, Direction direction, int count, int duration) {
    	
    	return swipeInContainerTillElement(element, null, direction, count, duration);
    	
    }

    @Deprecated
    public static boolean swipeUntilElementPresence(final ExtendedWebElement element) {
    	return swipeTillElement(element);
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
     * @param element ExtendedWebElement
     * @param direction Direction
     * @param minCoefficient double
     * @param maxCoefficient double
     * @param duration int
     * @return boolean
     */
    //TODO: combine logic with swipeInDevice(direction, duration)
    private static boolean swipeInDevice(ExtendedWebElement element, Direction direction, double minCoefficient, double maxCoefficient, int duration) {
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
    
    
    @Deprecated
    public static boolean scrollTo(ExtendedWebElement element, int swipeTimes) {
    	return swipeTillElement(element, swipeTimes);
    }
    

}
