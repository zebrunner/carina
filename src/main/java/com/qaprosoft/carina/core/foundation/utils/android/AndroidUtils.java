package com.qaprosoft.carina.core.foundation.utils.android;

import static com.qaprosoft.carina.core.foundation.webdriver.DriverPool.getDriver;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;

import io.appium.java_client.MobileBy;
import io.appium.java_client.MobileDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.SwipeElementDirection;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDeviceActionShortcuts;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;

public class AndroidUtils {

	protected static final Logger LOGGER = Logger.getLogger(AndroidUtils.class);

	protected static final long IMPLICIT_TIMEOUT = Configuration
			.getLong(Parameter.IMPLICIT_TIMEOUT);

	protected static final long EXPLICIT_TIMEOUT = Configuration
			.getLong(Parameter.EXPLICIT_TIMEOUT);

	public static final int MINIMUM_TIMEOUT = 2;

	public static final int DEFAULT_SWIPE_TIMEOUT = 1000;

	public enum Direction {
		LEFT, RIGHT, UP, DOWN, VERTICAL, VERTICAL_DOWN_FIRST, HORIZONTAL, HORIZONTAL_RIGHT_FIRST
	}

	/**
	 * Useful Android utilities. For usage: import
	 * com.qaprosoft.carina.core.foundation.utils.android.AndroidUtils;
	 *
	 */

	/**
	 * execute Key Event
	 *
	 * @param keyCode int
	 */

	public static void executeKeyEvent(int keyCode) {
		WebDriver driver = DriverPool.getDriver();
		LOGGER.info("Execute key event: " + keyCode);
		HashMap<String, Integer> keyCodeMap = new HashMap<String, Integer>();
		keyCodeMap.put("keycode", keyCode);
		((JavascriptExecutor) driver).executeScript("mobile: keyevent",
				keyCodeMap);

	}

	/**
	 * press Key Code
	 *
	 * @param keyCode int
	 * @return boolean
	 */
	protected boolean pressKeyCode(int keyCode) {
		try {
			LOGGER.info("Press key code: " + keyCode);
			((AndroidDeviceActionShortcuts) DriverPool.getDriver())
					.pressKeyCode(keyCode);
			return true;
		} catch (Exception e) {
			LOGGER.error("Exception:" + e);
		}
		return false;
	}

	/**
	 * scrollTo specified text
	 *
	 * @param text
	 *            - String
	 * @return boolean
	 */
	public static boolean scrollTo(final String text) {
		boolean scrolled = false;
		int repeat = 1;
		int tries = 3;
		// TODO: investigate how to:
		// AndroidUIAutomator("setMaxSearchSwipes(200)");
		do {
			try {
				LOGGER.info("Scroll to '" + text + "'");
				((AndroidDriver<?>) DriverPool.getDriver())
						.findElementByAndroidUIAutomator("new UiScrollable(new UiSelector().scrollable(true).instance(0)).scrollIntoView(new UiSelector().textContains(\""
								+ text + "\").instance(0))");
				scrolled = true;
			} catch (Exception e) {
				LOGGER.warn("Exception occurred for scroll operation!  "
						+ String.format(
								"For try #'%s'. Scrolling to text '%s'",
								repeat, text));
				repeat++;
				scrolled = false;
			}
		} while (!scrolled && repeat < tries);

		if (!scrolled) {
			try {
				LOGGER.info("Another solution Scroll to '" + text + "'");

						scrollToText(text);
				scrolled = true;
			} catch (Exception e) {
				LOGGER.warn("Exception occurred for scroll operation using Solution 2.  "
						+ String.format("Scrolling to text '%s'", text));
				scrolled = false;
			}
		}
		return scrolled;
	}

	/**
	 * scrollTo specified text using findElementByAndroidUIAutomator solution
	 *
	 * @param text
	 *            - String
	 * @return boolean
	 */
	public static boolean scrollTo1(final String text) {
		boolean scrolled = false;
		int repeat = 1;
		int tries = 3;
		// TODO: investigate how to:
		// AndroidUIAutomator("setMaxSearchSwipes(200)");
		do {
			try {
				LOGGER.info("Scroll to '" + text + "' using findElementByAndroidUIAutomator.");
				((AndroidDriver<?>) DriverPool.getDriver())
						.findElementByAndroidUIAutomator("new UiScrollable(new UiSelector().scrollable(true).instance(0)).scrollIntoView(new UiSelector().textContains(\""
								+ text + "\").instance(0))");
				scrolled = true;
			} catch (Exception e) {
				LOGGER.warn("Exception occurred for scroll operation!  "
						+ String.format(
								"For try #'%s'. Scrolling to text '%s'",
								repeat, text));
				repeat++;
				scrolled = false;
			}
		} while (!scrolled && repeat < tries);

		return scrolled;
	}


	/**
	 * scrollTo specified text
	 *
	 * @param text
	 *            - String
	 * @return boolean
	 */
	public static boolean scrollTo2(final String text) {
		boolean scrolled = false;

		try {
			LOGGER.info("Scroll to '" + text + "' using AndroidDriver default solution.");
			scrollToText(text);
			scrolled = true;
		} catch (Exception e) {
			LOGGER.warn("Exception occurred for scroll operation using Solution 2.  "
					+ String.format("Scrolling to text '%s'", text));
			scrolled = false;
		}
		return scrolled;
	}

	/**
	 * swipe Until Element Presence
	 *
	 * @param element
	 *            ExtendedWebElement
	 * @return boolean
	 */
	public static boolean swipeUntilElementPresence(
			final ExtendedWebElement element) {
		int swipeTimes = 20;
		WebDriver driver = DriverPool.getDriver();
		Dimension scrSize;
		int x;
		int y;
		boolean isPresent = element.isElementPresent(MINIMUM_TIMEOUT);
		LOGGER.info("Swipe down to element: ".concat(element.toString()));
		while (!isPresent && swipeTimes-- > 0) {
			LOGGER.debug("Element not present! Swipe down will be executed.");
			scrSize = driver.manage().window().getSize();
			x = scrSize.width / 2;
			y = scrSize.height / 2;
			((AndroidDriver<?>) driver).swipe(x, y, x, y / 2, 500);
			LOGGER.info("Swipe was executed. Attempts remain: " + swipeTimes);
			isPresent = element.isElementPresent(1);
			LOGGER.info("Result: " + isPresent);
		}
		if (!isPresent) {
			LOGGER.info("Swipe up to element: ".concat(element.toString()));
			swipeTimes = 20;
			while (!isPresent && swipeTimes-- > 0) {
				LOGGER.debug("Element not present! Swipe up will be executed.");
				scrSize = driver.manage().window().getSize();
				x = scrSize.width / 2;
				y = scrSize.height / 2;
				((AndroidDriver<?>) driver).swipe(x, y / 2, x, y, 500);
				LOGGER.info("Swipe was executed. Attempts remain: " + swipeTimes);
				isPresent = element.isElementPresent(1);
				LOGGER.info("Result: " + isPresent);
			}
		}
		return isPresent;
	}

	/**
	 * universal Scroll To text with different methods
	 *
	 * @param scrollToText
	 *            String
	 * @param containerElement
	 *            ExtendedWebElement
	 * @return boolean
	 */
	public static boolean universalScrollTo(String scrollToText,
			ExtendedWebElement containerElement) {
		return universalScrollTo(scrollToText, containerElement, 3, false);
	}

	/**
	 * universal Scroll To text with different methods
	 *
	 * @param scrollToText
	 *            String
	 * @param containerElement
	 *            ExtendedWebElement
	 * @param tries
	 *            - how much tries should be spent for scrolling. If 0 - it will
	 *            be quick check for not present element with scrolling try.
	 * @param oldMethod
	 *            boolean
	 * @return boolean
	 */
	public static boolean universalScrollTo(String scrollToText,
			ExtendedWebElement containerElement, int tries, boolean oldMethod) {
		boolean scrolled = false;
		if ((tries > 0) && (!oldMethod)) {
			scrolled = AndroidUtils.scrollTo(scrollToText);
		}
		if (scrolled) {
			return true;
		} else {
			WebDriver driver = DriverPool.getDriver();
			LOGGER.info("Scrolling with old scroll method. Just old method:"
					+ oldMethod + ". With " + tries + " tries.");
			try {
				try {
					driver.manage().timeouts()
							.implicitlyWait(1, TimeUnit.SECONDS);
				} catch (Exception e) {
					LOGGER.error("Strange error with implicitlyWait" + e);
				}

				RemoteWebElement element = (RemoteWebElement) driver
						.findElement(By.name(scrollToText));
				if (element.isDisplayed()) {
					try {
						driver.manage()
								.timeouts()
								.implicitlyWait(IMPLICIT_TIMEOUT,
										TimeUnit.SECONDS);
					} catch (Exception e) {
						LOGGER.error("Strange error with implicitlyWait" + e);
					}

					return true;
				}
			} catch (Exception e) {
				// restore timeout
				try {
					driver.manage().timeouts()
							.implicitlyWait(IMPLICIT_TIMEOUT, TimeUnit.SECONDS);
				} catch (Exception err) {
					LOGGER.error("Strange error with implicitlyWait" + err);
				}
			}
			LOGGER.info(String.format(
					"Scrolling to text '%s', Scroll container: %s",
					scrollToText, containerElement.getNameWithLocator()));

			try {
				final HashMap<String, String> scrollMap = new HashMap<String, String>();

				final JavascriptExecutor executor = (JavascriptExecutor) driver;
				scrollMap.put("text", scrollToText);

				scrollMap.put("element", ((RemoteWebElement) driver
						.findElement(containerElement.getBy())).getId());

				LOGGER.info(scrollMap);
				scrolled = false;
				int i = 0;
				while (!scrolled && ++i <= tries) {
					try {
						LOGGER.info("attempt #" + i);
						executor.executeScript("mobile: scrollTo", scrollMap);
						scrolled = true;
					} catch (Exception e) {
						LOGGER.warn("Exception occurred for scroll operation! "
								+ String.format(
										"Scrolling to text '%s', Scroll container: %s",
										scrollToText,
										containerElement.getNameWithLocator()));
					}
				}
			} catch (Exception e) {
				LOGGER.error("Error happened during call JavascriptExecutor executor : "
						+ e);
				scrolled = false;
			}
		}
		LOGGER.info("Successfully scrolled to text '" + scrollToText + "': "
				+ scrolled);
		return scrolled;
	}

	/**
	 * tap And Swipe specific ExtendedWebElement element to required direction
	 *
	 * @param elem
	 *            ExtendedWebElement
	 * @param direction
	 *            SwipeElementDirection
	 * @param duration
	 *            of swipe (int)
	 * @return boolean
	 */
	public static boolean tapAndSwipe(ExtendedWebElement elem,
			SwipeElementDirection direction, int duration) {
		return tapAndSwipe(elem.getBy(), direction, duration);
	}

	/**
	 * tap And Swipe specific element to left by default
	 *
	 * @param elem
	 *            By
	 * @return boolean
	 */
	public static boolean tapAndSwipe(By elem) {
		return tapAndSwipe(elem, SwipeElementDirection.LEFT, 1000);
	}

	/**
	 * tap And Swipe specific element to required direction
	 *
	 * @param elem
	 *            By
	 * @param direction
	 *            SwipeElementDirection
	 * @param duration
	 *            of swipe (int)
	 * @return boolean
	 */
	public static boolean tapAndSwipe(By elem, SwipeElementDirection direction,
			int duration) {
		MobileElement element;
		WebDriver driver = DriverPool.getDriver();
		try {
			element = (MobileElement) driver.findElement(elem);
			element.swipe(direction, duration);
			return true;
		} catch (Exception e) {
			LOGGER.error("Exception occurred when "
					+ "element.swipe(SwipeElementDirection."
					+ direction.toString() + ", " + duration + ")  "
					+ "was provided in tapAndSwipe functionality. Error: "
					+ e.getLocalizedMessage());
		}
		return false;
	}

	/**
	 * swipe Up
	 *
	 * @param elem By
	 * @param time int
	 */
	public static void swipeUp(By elem, int time) {
		tapAndSwipe(elem, SwipeElementDirection.UP, time);
	}

	/**
	 * swipe In Container
	 *
	 * @param elem
	 *            - scrollable container
	 * @param times
	 *            - swipe times
	 * @param direction
	 *            -Direction {LEFT, RIGHT, UP, DOWN}
	 * @param duration
	 *            - duration in msec.
	 */
	public static void swipeInContainer(ExtendedWebElement elem, int times,
			Direction direction, int duration) {

		// Default direction left
		double directMultX1 = 0.9;
		double directMultX2 = 0.1;
		double directMultY1 = 0.5;
		double directMultY2 = 0.5;

		WebDriver driver = DriverPool.getDriver();

		if (direction.equals(Direction.RIGHT)) {
			directMultX1 = 0.2;
			directMultX2 = 0.9;
			directMultY1 = 0.5;
			directMultY2 = 0.5;
			LOGGER.info("Swipe right");
		} else if (direction.equals(Direction.LEFT)) {
			directMultX1 = 0.9;
			directMultX2 = 0.2;
			directMultY1 = 0.5;
			directMultY2 = 0.5;
			LOGGER.info("Swipe left");
		} else if (direction.equals(Direction.UP)) {
			directMultX1 = 0.1;
			directMultX2 = 0.1;
			directMultY1 = 0.2;
			directMultY2 = 0.9;
			LOGGER.info("Swipe up");
		} else if (direction.equals(Direction.DOWN)) {
			directMultX1 = 0.1;
			directMultX2 = 0.1;
			directMultY1 = 0.9;
			directMultY2 = 0.2;
			LOGGER.info("Swipe down");
		} else if (direction.equals(Direction.VERTICAL)
				|| direction.equals(Direction.HORIZONTAL)
				|| direction.equals(Direction.HORIZONTAL_RIGHT_FIRST)
				|| direction.equals(Direction.VERTICAL_DOWN_FIRST)) {
			LOGGER.info("Incorrect swipe direction: " + direction.toString());
			return;
		}

		int x = elem.getElement().getLocation().getX();
		int y = elem.getElement().getLocation().getY();
		int width = elem.getElement().getSize().getWidth();
		int height = elem.getElement().getSize().getHeight();
		int screen_size_x = driver.manage().window().getSize().getWidth();
		int screen_size_y = driver.manage().window().getSize().getHeight();

		LOGGER.debug("x=" + x + ", y=" + y + ", width=" + width + ", height="
				+ height + ", screen width=" + screen_size_x
				+ ", screen height=" + screen_size_y);
		LOGGER.info("Swiping in container:" + elem.getNameWithLocator());
		for (int i = 0; i <= times; i++) {
			int pointX1 = (int) (x + (width * directMultX1));
			int pointY1 = (int) (y + (height * directMultY1));
			int pointX2 = (int) (x + (width * directMultX2));
			int pointY2 = (int) (y + (height * directMultY2));

			LOGGER.debug("Direction:" + direction + ". Try #" + i
					+ ". Points: X1Y1=" + pointX1 + ", " + pointY1 + ", X2Y2="
					+ pointX2 + ", " + pointY2);
			try {
				((AndroidDriver<?>) driver).swipe(pointX1, pointY1, pointX2,
						pointY2, duration);
			} catch (Exception e) {
				LOGGER.error("Exception: " + e);
			}
		}
	}

	/**
	 * Quick solution for scrolling To Button or element.
	 *
	 * @param extendedWebElement ExtendedWebElement
	 * @return boolean
	 */
	public static boolean scrollTo(final ExtendedWebElement extendedWebElement) {
		int i = 0;
		try {
			WebDriver driver = DriverPool.getDriver();
			int x = driver.manage().window().getSize().getWidth();
			int y = driver.manage().window().getSize().getHeight();
			LOGGER.info("Swipe down");
			while (!extendedWebElement.isElementPresent(1) && ++i <= 10) {
				LOGGER.debug("Swipe down. Attempt #"+i);
				((AndroidDriver<?>) driver)
						.swipe((int) (x * 0.1), (int) (y * 0.9),
								(int) (x * 0.1), (int) (y * 0.2), 2000);

			}
			if (!extendedWebElement.isElementPresent(1)) {
				LOGGER.info("Swipe up");
				i = 0;
				x = driver.manage().window().getSize().getWidth();
				y = driver.manage().window().getSize().getHeight();
				while (!extendedWebElement.isElementPresent(1) && ++i <= 10) {
					LOGGER.debug("Swipe up. Attempt #"+i);
					((AndroidDriver<?>) driver).swipe((int) (x * 0.1),
							(int) (y * 0.2), (int) (x * 0.1), (int) (y * 0.9),
							2000);
				}
			}
			return extendedWebElement.isElementPresent(1);
		} catch (Exception e) {
			LOGGER.info("Error happen during scrollTo ExtendedWebElement: " + e);
			return true;
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
	public static void swipeCoord(int startX, int startY, int endX, int endY,
			int duration) {
		WebDriver driver = DriverPool.getDriver();
		((AndroidDriver<?>) driver).swipe(startX, startY, endX, endY, duration);
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
	 * swipe In Container To required Element
	 *
	 * @param extendedWebElement
	 *            - expected element
	 * @param container
	 *            - scrollable container
	 * @param direction
	 *            - Direction {LEFT, RIGHT, UP, DOWN, HORIZONTAL, VERTICAL }
	 * @param duration
	 *            - duration
	 * @param times
	 *            - times
	 * @return boolean
	 */
	public static boolean swipeInContainerToElement(
			final ExtendedWebElement extendedWebElement,
			ExtendedWebElement container, Direction direction, int duration,
			int times) {
		int i = 0;
		boolean bothWay = false;
		Direction oppositeDirection = Direction.DOWN;
		try {
			WebDriver driver = DriverPool.getDriver();
			if (extendedWebElement.isElementPresent(1)) {
				LOGGER.info("Element already present");
				return true;
			}

			if (direction.equals(Direction.HORIZONTAL)) {
				bothWay = true;
				direction = Direction.LEFT;
				oppositeDirection = Direction.RIGHT;
			} else if (direction.equals(Direction.HORIZONTAL_RIGHT_FIRST)) {
				bothWay = true;
				direction = Direction.RIGHT;
				oppositeDirection = Direction.LEFT;
			} else if (direction.equals(Direction.VERTICAL_DOWN_FIRST)) {
				bothWay = true;
				direction = Direction.DOWN;
				oppositeDirection = Direction.UP;
			} else if (direction.equals(Direction.VERTICAL)) {
				bothWay = true;
				direction = Direction.UP;
				oppositeDirection = Direction.DOWN;
			}

			while (!extendedWebElement.isElementPresent(1) && ++i <= times) {
				LOGGER.debug("Swipe " + direction.toString());
				swipeInContainer(container, 1, direction, duration);
			}
			if (!extendedWebElement.isElementPresent(1) && bothWay) {
				LOGGER.info("Swipe in opposite direction");
				i = 0;

				while (!extendedWebElement.isElementPresent(1) && ++i <= times) {
					LOGGER.debug("Swipe " + direction.toString());
					swipeInContainer(container, 1, oppositeDirection, duration);
				}
			}
			return extendedWebElement.isElementPresent(1);
		} catch (Exception e) {
			LOGGER.info("Error happened during swipe in container for element: "
					+ e);
			return true;
		}
	}

	/**
	 * Hide keyboard if needed
	 */
	public static void hideKeyboard() {
		try {
			((AndroidDriver<?>) DriverPool.getDriver()).hideKeyboard();
		} catch (Exception e) {
			LOGGER.info("Keyboard was already hided or error occurs: " + e);
		}
	}

	/**
	 * wait Until Element Not Present
	 *
	 * @param locator
	 *            By
	 * @param timeout
	 *            long
	 * @param pollingTime
	 *            long
	 */
	public static void waitUntilElementNotPresent(final By locator,
			final long timeout, final long pollingTime) {
		LOGGER.info(String.format("Wait until element %s disappear",
				locator.toString()));
		WebDriver driver = DriverPool.getDriver();
		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
		try {
			if (new WebDriverWait(driver, timeout, pollingTime)
					.until(ExpectedConditions
							.invisibilityOfElementLocated(locator))) {
				LOGGER.info(String.format(
						"Element located by: %s not present.",
						locator.toString()));
			} else {
				LOGGER.info(String.format(
						"Element located by: %s is still present.",
						locator.toString()));
			}
		} catch (TimeoutException e) {
			LOGGER.debug(e.getMessage());
			LOGGER.info(String.format(
					"Element located by: %s is still present.",
					locator.toString()));
		}
		driver.manage().timeouts()
				.implicitlyWait(IMPLICIT_TIMEOUT, TimeUnit.SECONDS);
	}

	/**
	 * Tap and Hold (LongPress) on element in Android
	 *
	 * @param element
	 *            ExtendedWebElement
	 * @return boolean
	 */
	public static boolean longPress(ExtendedWebElement element) {
		try {
			WebDriver driver = DriverPool.getDriver();
			TouchAction action = new TouchAction((MobileDriver) driver);
			action.longPress(element.getElement()).release().perform();
			return true;
		} catch (Exception e) {
			LOGGER.info("Error occurs: " + e);
		}
		return false;
	}


    public static ExtendedWebElement scrollToText(String text) {
        AndroidElement androidElement = ((AndroidDriver<AndroidElement>) getDriver()).findElement(MobileBy
                .AndroidUIAutomator("new UiScrollable(new UiSelector()).scrollIntoView(new UiSelector().text(\"" + text + "\"));"));


        return new ExtendedWebElement(androidElement, getDriver());
    }

    public static ExtendedWebElement scrollToText(String scrollViewId, String text) {
        AndroidElement androidElement = ((AndroidDriver<AndroidElement>) getDriver()).findElement(MobileBy
                .AndroidUIAutomator("new UiScrollable(new UiSelector().resourceId(\"" + scrollViewId + "\")).scrollIntoView(new UiSelector().text(\"" + text + "\"));"));


        return new ExtendedWebElement(androidElement, getDriver());
    }
}
