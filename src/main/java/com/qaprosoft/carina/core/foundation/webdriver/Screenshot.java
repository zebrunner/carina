/*
 * Copyright 2013-2015 QAPROSOFT (http://qaprosoft.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qaprosoft.carina.core.foundation.webdriver;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.qaprosoft.amazon.AmazonS3Manager;
import com.qaprosoft.carina.core.foundation.log.TestLogCollector;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;
import com.qaprosoft.carina.core.foundation.webdriver.augmenter.DriverAugmenter;
import com.qaprosoft.carina.core.foundation.webdriver.screenshot.IScreenshotRule;

import io.appium.java_client.AppiumDriver;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

/**
 * Screenshot manager for operation with screenshot capturing, resizing and removing of old screenshot folders.
 * 
 * @author Alex Khursevich
 */
public class Screenshot
{
	private static final Logger LOGGER = Logger.getLogger(Screenshot.class);

	private static List<IScreenshotRule> rules = Collections.synchronizedList(new ArrayList<IScreenshotRule>());
	
	/**
	 * Adds screenshot rule
	 * @param rule
	 * @return list of existing rules
	 */
	public static List<IScreenshotRule> addScreenshotRule(IScreenshotRule rule) {
	    LOGGER.debug("Following rule will be added: ".concat(rule.getClass().getName()));
	    rules.add(rule);
	    LOGGER.debug("Actual range of screenshot rules: ".concat(rules.toString()));
	    return rules;
	}
	
	/**
	 * Adds screenshot rules
	 * @param rulesList - list of new rules
	 * @return list of existing rules
	 */
	public static List<IScreenshotRule> addScreenshotRules(List<IScreenshotRule> rulesList) {
	    for (IScreenshotRule iScreenshotRule : rulesList) {
	        LOGGER.debug("Following rule will be added: ".concat(iScreenshotRule.getClass().getName()));
        }
        rules.addAll(rulesList);
        LOGGER.debug("Actual range of screenshot rules: ".concat(rules.toString()));
        return rules;
    }
	
	/**
	 * Deletes rule
	 * @param rule
	 * @return list of existing rules
	 */
	public static List<IScreenshotRule> removeScreenshotRule(IScreenshotRule rule) {
        LOGGER.debug("Following rule will be removed if it exists: ".concat(rule.getClass().getName()));
        rules.remove(rule);
        LOGGER.debug("Actual range of screenshot rules: ".concat(rules.toString()));
        return rules;
    }
	
	public static List<IScreenshotRule> clearRules() {
	    LOGGER.debug("All rules will be deleted.");
	    rules.clear();
        return rules;
    }
	
	public static String captureByRule(WebDriver driver, String comment)
    {
	    boolean isTakeScreenshotRules = false;
	    for (IScreenshotRule iScreenshotRule : rules) {
	        isTakeScreenshotRules = iScreenshotRule.isTakeScreenshot();
	        if (isTakeScreenshotRules) {
	            break;
	        }
	    }
	    return capture(driver, Configuration.getBoolean(Parameter.AUTO_SCREENSHOT) && isTakeScreenshotRules, comment, false);
    }
	
	/**
	 * Captures screenshot based on auto_screenshot global parameter, creates thumbnail and copies both images to specified screenshots location.
	 * 
	 * @param driver
	 *            instance used for capturing.
	 * @return screenshot name.
	 */
	@Deprecated
	public static String capture(WebDriver driver)
	{
		return capture(driver, Configuration.getBoolean(Parameter.AUTO_SCREENSHOT));
	}

	/**
	 * Captures screenshot explicitly ignoring auto_screenshot global parameter, creates thumbnail and copies both images to specified screenshots location.
	 * 
	 * @param driver
	 *            instance used for capturing.
	 * @param comment String
	 * @return screenshot name.
	 */
	public static String captureFailure(WebDriver driver, String comment)
	{
		return capture(driver, true, comment, true);
	}
	
	/**
	 * Captures full size screenshot based on auto_screenshot global parameter, creates thumbnail and copies both images to specified screenshots location.
	 * 
	 * @param driver
	 *            instance used for capturing.
	 * @param comment String
	 * @return screenshot name.
	 */
	public static String captureFullSize(WebDriver driver, String comment)
	{
		return capture(driver, true /*explicitly make full size screenshot*/, comment, true);
	}
	
	/**
	 * Captures screenshot with comment based on auto_screenshot global parameter, creates thumbnail and copies both images to specified screenshots location.
	 * 
	 * @param driver
	 *            instance used for capturing.
	 * @param comment String
	 * @return screenshot name.
	 */
	public static String capture(WebDriver driver, String comment)
	{
		return capture(driver, Configuration.getBoolean(Parameter.AUTO_SCREENSHOT), comment, false);
	}

	/**
	 * Captures screenshot, creates thumbnail and copies both images to specified screenshots location.
	 * 
	 * @param driver
	 *            instance used for capturing.
	 * @param isTakeScreenshot
	 *            perform actual capture or not
	 * @return screenshot name.
	 */
	@Deprecated
	public static String capture(WebDriver driver, boolean isTakeScreenshot)
	{
		return capture(driver, isTakeScreenshot, "", false);

	}
	
	/**
	 * Captures screenshot, creates thumbnail and copies both images to specified screenshots location.
	 * 
	 * @param driver
	 *            instance used for capturing.
	 * @param isTakeScreenshot
	 *            perform actual capture or not
	 * @param comment
	 *            String
	 * @return screenshot name.
	 */
	@Deprecated
	public static String capture(WebDriver driver, boolean isTakeScreenshot, String comment)
	{
		return capture(driver, isTakeScreenshot, comment, false);

	}

	/**
	 * Captures web-browser screenshot, creates thumbnail and copies both images to specified screenshots location.
	 * 
	 * @param driver
	 *            instance used for capturing.
	 * @param isTakeScreenshot
	 *            perform actual capture or not
	 * @param comment
	 * 			  String
	 * @param fullSize
	 * 			  Boolean
	 * @return screenshot name.
	 */
	private static String capture(WebDriver driver, boolean isTakeScreenshot, String comment, boolean fullSize) {
		String screenName = "";
		
		// TODO: AUTO-2883 make full size screenshot generation only when fullSize == true
		// For the rest of cases returned previous implementation

		if (isTakeScreenshot && !DriverFactory.HTML_UNIT.equalsIgnoreCase(Configuration.get(Parameter.BROWSER))) {
			if (driver == null) {
				LOGGER.warn("Unable to capture screenshot as driver is null.");
				return null;
			}
			if (driver.toString().contains("null")) {
				LOGGER.warn("Unable to capture screenshot as driver is not valid anymore.");
				return null;
			}

			try {
				// Define test screenshot root
				String test = "";
				if (TestNamingUtil.isTestNameRegistered()) {
					test = TestNamingUtil.getTestNameByThread();
				} else {
					test = TestNamingUtil.getCanonicTestNameByThread();
				}

				if (test == null || StringUtils.isEmpty(test)) {
					LOGGER.warn("Unable to capture screenshot as Test Name was not found.");
					return null;
				}

				File testScreenRootDir = ReportContext.getTestDir(test);

				// Capture full page screenshot and resize
				String fileID = test.replaceAll("\\W+", "_") + "-" + System.currentTimeMillis();
				screenName = fileID + ".png";
				String screenPath = testScreenRootDir.getAbsolutePath() + "/" + screenName;

				WebDriver augmentedDriver = driver;
				if (!driver.toString().contains("AppiumNativeDriver")) {
					// do not augment for Appium 1.x anymore
					augmentedDriver = new DriverAugmenter().augment(driver);
				}
				
				BufferedImage screen;

				//Create screenshot
				if (fullSize) {
					screen = takeFullScreenshot(driver, augmentedDriver);
				} else {
					screen = takeVisibleScreenshot(driver, augmentedDriver);
				}

				BufferedImage thumbScreen = screen;

				if (Configuration.getInt(Parameter.BIG_SCREEN_WIDTH) != -1
						&& Configuration.getInt(Parameter.BIG_SCREEN_HEIGHT) != -1) {
					resizeImg(screen, Configuration.getInt(Parameter.BIG_SCREEN_WIDTH),
							Configuration.getInt(Parameter.BIG_SCREEN_HEIGHT), screenPath);
				}

				ImageIO.write(screen, "PNG", new File(screenPath));

				// Create screenshot thumbnail
				String thumbScreenPath = screenPath.replace(screenName, "/thumbnails/" + screenName);
				ImageIO.write(thumbScreen, "PNG", new File(thumbScreenPath));
				resizeImg(thumbScreen, Configuration.getInt(Parameter.SMALL_SCREEN_WIDTH),
						Configuration.getInt(Parameter.SMALL_SCREEN_HEIGHT), thumbScreenPath);

				// Uploading screenshot to Amazon S3
				uploadToAmazonS3(test, screenPath, screenName, comment);

				// add screenshot comment to collector
				TestLogCollector.addScreenshotComment(screenName, comment);

			} catch (IOException e) {
				LOGGER.error("Unable to capture screenshot due to the I/O issues!", e);
			} catch (Exception e) {
				LOGGER.error("Unable to capture screenshot!", e);
			}
		}
		return screenName;
	}

	private static void uploadToAmazonS3(String test, String fullScreenPath, String screenName, String comment)
	{
		if (!Configuration.getBoolean(Parameter.S3_SAVE_SCREENSHOTS))
		{
			LOGGER.debug("there is no sense to continue as saving screenshots onto S3 is disabled.");
			return;
		}
		// TODO: not good solution...
		Long runId = Long.valueOf(System.getProperty("zafira_run_id"));
		String testName = ReportContext.getTestDir(test).getName();
		String key = runId + "/" + testName + "/" + screenName;
		if (runId == -1)
		{
			key = "/LOCAL/" + ReportContext.getRootID() + "/" + testName + "/" + screenName;
		}
		LOGGER.debug("Key: " + key);
		LOGGER.debug("FullScreenPath: " + fullScreenPath);
		String screenshotBucket = Configuration.get(Parameter.S3_SCREENSHOT_BUCKET_NAME);

		ObjectMetadata metadata = new ObjectMetadata();
		if (!comment.isEmpty())
		{
			metadata.addUserMetadata(SpecialKeywords.COMMENT, comment);
		}

		AmazonS3Manager.getInstance().put(screenshotBucket, key, fullScreenPath, metadata);
	}

	/**
	 * Resizes image according to specified dimensions.
	 * 
	 * @param bufImage
	 *            - image to resize.
	 * @param width
	 *            - new image width.
	 * @param height
	 *            - new image height.
	 * @param path
	 *            - path to screenshot file.
	 */
	private static void resizeImg(BufferedImage bufImage, int width, int height, String path) {
		try {
			bufImage = Scalr.resize(bufImage, Scalr.Method.BALANCED, Scalr.Mode.FIT_TO_WIDTH, width, height,
					Scalr.OP_ANTIALIAS);
			if (bufImage.getHeight() > height) {
				bufImage = Scalr.crop(bufImage, bufImage.getWidth(), height);
			}
			ImageIO.write(bufImage, "png", new File(path));
		} catch (Exception e) {
			LOGGER.error("Image scaling problem!");
		}
	}
	
	/**
	 * Makes fullsize screenshot using javascript (May not work properly with
	 * popups and active js-elements on the page)
	 * 
	 * @param driver
	 *            - webDriver.
	 * @param augmentedDriver
	 *            - webDriver.
	 * @exception IOException
	 * 
	 * @return screenshot image
	 */
	private static BufferedImage takeFullScreenshot(WebDriver driver, WebDriver augmentedDriver) throws IOException {
		BufferedImage screenShot;
		if (driver.getClass().toString().contains("java_client")) {
			File screenshot = ((AppiumDriver<?>) driver).getScreenshotAs(OutputType.FILE);
			screenShot = ImageIO.read(screenshot);
		} else {
			ru.yandex.qatools.ashot.Screenshot screenshot = new AShot()
					.shootingStrategy(ShootingStrategies.viewportPasting(100)).takeScreenshot(augmentedDriver);
			screenShot = screenshot.getImage();
		}

		return screenShot;
	}

	/**
	 * Makes screenshot of visible part of the page
	 * 
	 * @param driver
	 *            - webDriver.
	 * @param augmentedDriver
	 *            - webDriver.
	 * @exception IOException
	 * 
	 * @return screenshot image
	 */
	private static BufferedImage takeVisibleScreenshot(WebDriver driver, WebDriver augmentedDriver) throws IOException {
		BufferedImage screenShot = ImageIO.read(((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE));
		return screenShot;
	}
}
