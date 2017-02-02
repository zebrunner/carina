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

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.SessionNotFoundException;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.qaprosoft.amazon.AmazonS3Manager;
import com.qaprosoft.carina.core.foundation.log.TestLogCollector;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.report.zafira.ZafiraIntegrator;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;
import com.qaprosoft.carina.core.foundation.webdriver.augmenter.DriverAugmenter;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

/**
 * Screenshot manager for operation with screenshot capturing, resizing and
 * removing of old screenshot folders.
 * 
 * @author Alex Khursevich
 */
public class Screenshot {
	private static final Logger LOGGER = Logger.getLogger(Screenshot.class);

	/**
	 * Captures web-browser screenshot, creates thumbnail and copies both images
	 * to specified screenshots location.
	 * 
	 * @param driver
	 *            instance used for capturing.
	 * @return screenshot name.
	 */
	public static String capture(WebDriver driver) {
		return capture(driver, Configuration.getBoolean(Parameter.AUTO_SCREENSHOT));
	}

	/**
	 * Captures web-browser screenshot, creates thumbnail and copies both images
	 * to specified screenshots location.
	 * 
	 * @param driver
	 *            instance used for capturing.
	 * @param comment
	 * @return screenshot name.
	 */
	public static String capture(WebDriver driver, String comment) {
		return capture(driver, Configuration.getBoolean(Parameter.AUTO_SCREENSHOT), comment);
	}

	/**
	 * Captures web-browser screenshot, creates thumbnail and copies both images
	 * to specified screenshots location.
	 * 
	 * @param driver
	 *            instance used for capturing.
	 * @param isTakeScreenshot
	 *            perform actual capture or not
	 * @return screenshot name.
	 */
	public static String capture(WebDriver driver, boolean isTakeScreenshot) {
		return capture(driver, isTakeScreenshot, "");

	}

	/**
	 * Captures web-browser screenshot, creates thumbnail and copies both images
	 * to specified screenshots location.
	 * 
	 * @param driver
	 *            instance used for capturing.
	 * @param isTakeScreenshot
	 *            perform actual capture or not
	 * @param comment
	 * @return screenshot name.
	 */
	public static String capture(WebDriver driver, boolean isTakeScreenshot, String comment) {
		String screenName = "";

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
				String fullScreenPath = testScreenRootDir.getAbsolutePath() + "/" + screenName;

				WebDriver augmentedDriver = driver;
				if (!driver.toString().contains("AppiumNativeDriver")) {
					// do not augment for Appium 1.x anymore
					augmentedDriver = new DriverAugmenter().augment(driver);
				}

				ru.yandex.qatools.ashot.Screenshot screenshot = new AShot()
						.shootingStrategy(ShootingStrategies.viewportPasting(100)).takeScreenshot(augmentedDriver);
				BufferedImage fullScreen = screenshot.getImage();

				if (Configuration.getInt(Parameter.BIG_SCREEN_WIDTH) != -1
						&& Configuration.getInt(Parameter.BIG_SCREEN_HEIGHT) != -1) {
					resizeImg(fullScreen, Configuration.getInt(Parameter.BIG_SCREEN_WIDTH),
							Configuration.getInt(Parameter.BIG_SCREEN_HEIGHT), fullScreenPath);
				}
				ImageIO.write(fullScreen, "PNG", new File(fullScreenPath));

				// Create screenshot thumbnail
				String thumbScreenPath = fullScreenPath.replace(screenName, "/thumbnails/" + screenName);
				BufferedImage thumbScreen = screenshot.getImage();
				ImageIO.write(thumbScreen, "PNG", new File(thumbScreenPath));
				resizeImg(thumbScreen, Configuration.getInt(Parameter.SMALL_SCREEN_WIDTH),
						Configuration.getInt(Parameter.SMALL_SCREEN_HEIGHT), thumbScreenPath);

				// Uploading screenshot to Amazon S3
				uploadToAmazonS3(test, fullScreenPath, screenName, comment);

				// add screenshot comment to collector
				TestLogCollector.addScreenshotComment(screenName, comment);

			} catch (IOException e) {
				LOGGER.error("Unable to capture screenshot due to the I/O issues!", e);
			} catch (SessionNotFoundException e) {
				LOGGER.error(e.getMessage());
			} catch (Exception e) {
				LOGGER.error("Unable to capture screenshot!", e);
			}
		}
		return screenName;
	}

	private static void uploadToAmazonS3(String test, String fullScreenPath, String screenName, String comment) {
		if (!Configuration.getBoolean(Parameter.S3_SAVE_SCREENSHOTS)) {
			LOGGER.debug("there is no sense to continue as saving screenshots onto S3 is disabled.");
			return;
		}
		Long runId = ZafiraIntegrator.getRunId();
		String testName = ReportContext.getTestDir(test).getName();
		String key = runId + "/" + testName + "/" + screenName;
		if (runId == -1) {
			key = "/LOCAL/" + ReportContext.getRootID() + "/" + testName + "/" + screenName;
		}
		LOGGER.debug("Key: " + key);
		LOGGER.debug("FullScreenPath: " + fullScreenPath);
		String screenshotBucket = Configuration.get(Parameter.S3_SCREENSHOT_BUCKET_NAME);

		ObjectMetadata metadata = new ObjectMetadata();
		if (!comment.isEmpty()) {
			metadata.addUserMetadata(SpecialKeywords.COMMENT, comment);
		}

		AmazonS3Manager.getInstance().put(screenshotBucket, key, fullScreenPath, metadata);
	}

	/**
	 * Resizes image according to specified dimensions.
	 * 
	 * @param imageFile
	 *            - image to resize.
	 * @param width
	 *            - new image width.
	 * @param height
	 *            - new image height.
	 */
	public static void resizeImg(BufferedImage bufImage, int width, int height, String path) {
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
}
