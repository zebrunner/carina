/*
 * Copyright 2013 QAPROSOFT (http://qaprosoft.com/).
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;
import com.qaprosoft.carina.core.foundation.webdriver.augmenter.DriverAugmenter;

/**
 * Screenshot manager for operation with screenshot capturing, resizing and
 * removing of old screenshot folders.
 * 
 * @author Alex Khursevich
 */
public class Screenshot
{
	private static final Logger LOGGER = Logger.getLogger(Screenshot.class);

	public static String capture(WebDriver driver)
	{
		return capture(driver, Configuration.getBoolean(Parameter.AUTO_SCREENSHOT) && !Configuration.getBoolean(Parameter.TAKE_ONLY_FAIL_SCREENSHOT));
	}
	
	/**
	 * Captures web-browser screenshot, creates thumbnail and copies both images
	 * to specified sceenshots location.
	 * 
	 * @param driver
	 *            instance used for capturing.
	 * @return screenshot name.
	 */
	public static synchronized String capture(WebDriver driver, boolean isTakeScreenshot)
	{
		String screenName = "";
		
		if (isTakeScreenshot && !DriverFactory.HTML_UNIT.equalsIgnoreCase(Configuration.get(Parameter.BROWSER)))
		{
			if (driver.toString().contains("null")) {
				LOGGER.warn("Unable to capture screenshot as driver is not valid anymore.");
				return null;
			}
			
			try
			{
				// Define test screenshot root
				String test = TestNamingUtil.getTestNameByThread(Thread.currentThread().getId());
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
					//do not augment for Appium 1.x anymore
					augmentedDriver = new DriverAugmenter().augment(driver);
				} 
				
				File fullScreen = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);				
				//File fullScreen = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
				
				if (Configuration.getInt(Parameter.BIG_SCREEN_WIDTH) != -1 && Configuration.getInt(Parameter.BIG_SCREEN_HEIGHT) != -1){
					resizeImg(fullScreen, Configuration.getInt(Parameter.BIG_SCREEN_WIDTH), Configuration.getInt(Parameter.BIG_SCREEN_HEIGHT));
				}
				FileUtils.copyFile(fullScreen, new File(fullScreenPath));

				// Create screenshot thumbnail
				String thumbScreenPath = fullScreenPath.replace(screenName, "/thumbnails/" + screenName);
				File thumbScreen = new File(thumbScreenPath);
				FileUtils.copyFile(fullScreen, thumbScreen);
				resizeImg(thumbScreen, Configuration.getInt(Parameter.SMALL_SCREEN_WIDTH),
						Configuration.getInt(Parameter.SMALL_SCREEN_HEIGHT));
			}
			catch (IOException e)
			{
				LOGGER.error("Unable to capture screenshot due to the I/O issues!");
//				e.printStackTrace();
			}
			catch (Exception e)
			{
//				LOGGER.error("Unable to capture screenshot!");
				LOGGER.error("Unable to capture screenshot! " + e.getMessage());
				e.printStackTrace();
			}
		}
		return screenName;
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
	public static void resizeImg(File imageFile, int width, int height)
	{
		try
		{
			BufferedImage bufImage = ImageIO.read(imageFile);
			bufImage = Scalr.resize(bufImage, Scalr.Method.BALANCED, Scalr.Mode.FIT_TO_WIDTH, width, height, Scalr.OP_ANTIALIAS);
			if (bufImage.getHeight() > height)
			{
				bufImage = Scalr.crop(bufImage, bufImage.getWidth(), height);
			}
			ImageIO.write(bufImage, "png", imageFile);
		}
		catch (Exception e)
		{
			LOGGER.error("Image scaling problem!");
		}
	}
}
