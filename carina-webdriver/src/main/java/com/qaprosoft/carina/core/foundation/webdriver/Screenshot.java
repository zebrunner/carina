/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation.webdriver;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.performance.ACTION_NAME;
import com.qaprosoft.carina.core.foundation.performance.Timer;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.messager.ZafiraMessager;
import com.qaprosoft.carina.core.foundation.webdriver.augmenter.DriverAugmenter;
import com.qaprosoft.carina.core.foundation.webdriver.screenshot.IScreenshotRule;
import com.qaprosoft.zafira.client.ZafiraSingleton;
import com.qaprosoft.zafira.listener.ZafiraListener;
import com.qaprosoft.zafira.log.MetaInfoLevel;
import com.qaprosoft.zafira.log.MetaInfoMessage;
import com.qaprosoft.zafira.models.dto.aws.FileUploadType;

import io.appium.java_client.AppiumDriver;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

/**
 * Screenshot manager for operation with screenshot capturing, resizing and removing of old screenshot folders.
 * 
 * @author Alex Khursevich
 */
public class Screenshot {
    private static final Logger LOGGER = Logger.getLogger(Screenshot.class);

    private static List<IScreenshotRule> rules = Collections.synchronizedList(new ArrayList<IScreenshotRule>());

    private static ExecutorService executorService = Executors.newFixedThreadPool(50);
    
    private static final String AMAZON_KEY_FORMAT = FileUploadType.Type.SCREENSHOTS.getPath() + "/%s/";
    
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy");
    
    private Screenshot() {
    	//hide default constructor
    }

    /**
     * Adds screenshot rule
     * 
     * @param rule IScreenshotRule
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
     * 
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
     * 
     * @param rule IScreenshotRule
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

    public static String captureByRule(WebDriver driver, String comment) {
        boolean isTakeScreenshotRules = false;
        for (IScreenshotRule iScreenshotRule : rules) {
            isTakeScreenshotRules = iScreenshotRule.isTakeScreenshot();
            if (isTakeScreenshotRules) {
                break;
            }
        }
        return capture(driver, isTakeScreenshotRules, comment, false);
    }

    /**
     * @deprecated  As of release 5.x, replaced by {@link #capture(WebDriver driver, String comment)}
     * 
     * Captures screenshot based on auto_screenshot global parameter, creates thumbnail and copies both images to specified screenshots location.
     * 
     * @param driver
     *            instance used for capturing.
     * @return screenshot name.
     */
    @Deprecated
    public static String capture(WebDriver driver) {
        return capture(driver, Configuration.getBoolean(Parameter.AUTO_SCREENSHOT));
    }

    /**
     * Captures screenshot explicitly ignoring auto_screenshot global parameter, creates thumbnail and copies both images to specified screenshots
     * location.
     * 
     * @param driver
     *            instance used for capturing.
     * @param comment String
     * @return screenshot name.
     */
    public static String captureFailure(WebDriver driver, String comment) {
        LOGGER.debug("Screenshot->captureFailure starting...");
        String screenName = capture(driver, true, comment, true);

        // XML layout extraction
        File uiDumpFile = IDriverPool.getDefaultDevice().generateUiDump(screenName);
        if (uiDumpFile != null) {
            uiDumpFile.getPath().split("\\/");
        }
        LOGGER.debug("Screenshot->captureFailure finished.");
        return screenName;
    }

    /**
     * Captures full size screenshot based on auto_screenshot global parameter, creates thumbnail and copies both images to specified screenshots
     * location.
     * 
     * @param driver
     *            instance used for capturing.
     * @param comment String
     * @return screenshot name.
     */
    public static String captureFullSize(WebDriver driver, String comment) {
        return capture(driver, true /* explicitly make full size screenshot */, comment, true);
    }

    /**
     * Captures screenshot with comment based on auto_screenshot global parameter, creates thumbnail and copies both images to specified screenshots
     * location.
     * 
     * @param driver
     *            instance used for capturing.
     * @param comment String
     * @return screenshot name.
     */
    public static String capture(WebDriver driver, String comment) {
        return capture(driver, Configuration.getBoolean(Parameter.AUTO_SCREENSHOT), comment, false);
    }

    /**
     * @deprecated  As of release 5.x, replaced by {@link #capture(WebDriver driver, String comment)}
     * 
     * Captures screenshot, creates thumbnail and copies both images to specified screenshots location.
     * 
     * @param driver
     *            instance used for capturing.
     * @param isTakeScreenshot
     *            perform actual capture or not
     * @return screenshot name.
     */
    @Deprecated
    public static String capture(WebDriver driver, boolean isTakeScreenshot) {
        return capture(driver, isTakeScreenshot, "", false);

    }

    /**
     * @deprecated  As of release 5.x, replaced by {@link #capture(WebDriver driver, String comment)}
     * 
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
    public static String capture(WebDriver driver, boolean isTakeScreenshot, String comment) {
        return capture(driver, isTakeScreenshot, comment, false);
    }

    /**
     * Captures driver screenshot for Alice-AI metadata and put it to appropriate metadata location
     * 
     * @param driver
     *            instance used for capturing.
     * @param screenName
     *            String
     * @return screenshot name.
     */

    public static String captureMetadata(WebDriver driver, String screenName) {

        String screenPath = "";

        try {
            screenPath = ReportContext.getMetadataFolder().getAbsolutePath() + "/" + screenName.replaceAll("\\W+", "_") + ".png";

            WebDriver augmentedDriver = driver;
            if (!driver.toString().contains("AppiumNativeDriver")) {
                // do not augment for Appium 1.x anymore
                augmentedDriver = new DriverAugmenter().augment(driver);
            }

            BufferedImage screen;

            // Create screenshot
            screen = takeVisibleScreenshot(augmentedDriver);

            ImageIO.write(screen, "PNG", new File(screenPath));

        } catch (IOException e) {
            LOGGER.error("Unable to capture screenshot due to the I/O issues!", e);
        } catch (Exception e) {
            LOGGER.error("Unable to capture screenshot!", e);
        }

        return screenPath;
    }

    /**
     * Captures application screenshot, creates thumbnail and copies both images to specified screenshots location.
     * 
     * @param driver
     *            instance used for capturing.
     * @param isTakeScreenshot
     *            perform actual capture or not
     * @param comment
     *            String
     * @param fullSize
     *            Boolean
     * @return screenshot name.
     */

    private static String capture(WebDriver driver, boolean isTakeScreenshot, String comment, boolean fullSize) {
        String screenName = "";

        // TODO: AUTO-2883 make full size screenshot generation only when fullSize == true
        // For the rest of cases returned previous implementation

        LOGGER.debug("Screenshot->capture starting...");
        
        if (isTakeScreenshot) {
            try {
            	if (!isCaptured(comment)) {
            		LOGGER.error("Unable to capture screenshot as driver seems invalid: " + comment);
            		return screenName;
            	}
            	
            	Timer.start(ACTION_NAME.CAPTURE_SCREENSHOT);
                // Define test screenshot root
                File testScreenRootDir = ReportContext.getTestDir();

                // Capture full page screenshot and resize
                screenName = System.currentTimeMillis() + ".png";
                String screenPath = testScreenRootDir.getAbsolutePath() + "/" + screenName;

                WebDriver augmentedDriver = driver;
                
                
                //hotfix to converting proxy into the valid driver
                if (driver instanceof Proxy) {
    				try {
        				InvocationHandler innerProxy = Proxy.getInvocationHandler((Proxy) driver);
        				// "arg$2" is by default RemoteWebDriver;
        				// "arg$1" is EventFiringWebDriver
        				// wrap into try/catch to make sure we don't affect test execution
        				Field locatorField = innerProxy.getClass().getDeclaredField("arg$2");
        				locatorField.setAccessible(true);
        				
        				augmentedDriver = driver = (WebDriver) locatorField.get(innerProxy); 
    				} catch (Exception e) {
    					//do nothing and receive augmenting warning in the logs
    				}
                }
    				
                if (!driver.toString().contains("AppiumNativeDriver")) {
                    // do not augment for Appium 1.x anymore
                    augmentedDriver = new DriverAugmenter().augment(driver);
                }

                BufferedImage screen;

                // Create screenshot
                if (fullSize) {
                    screen = takeFullScreenshot(driver, augmentedDriver);
                } else {
                    screen = takeVisibleScreenshot(augmentedDriver);
                }

                if (screen == null) {
                	//do nothing and return empty 
                	return "";
                }
                BufferedImage thumbScreen = screen;

                if (Configuration.getInt(Parameter.BIG_SCREEN_WIDTH) != -1
                        && Configuration.getInt(Parameter.BIG_SCREEN_HEIGHT) != -1) {
                    resizeImg(screen, Configuration.getInt(Parameter.BIG_SCREEN_WIDTH),
                            Configuration.getInt(Parameter.BIG_SCREEN_HEIGHT), screenPath);
                }

                File screenshot = new File(screenPath);

                ImageIO.write(screen, "PNG", screenshot);

                // Create screenshot thumbnail
                String thumbScreenPath = screenPath.replace(screenName, "/thumbnails/" + screenName);
                ImageIO.write(thumbScreen, "PNG", new File(thumbScreenPath));
                resizeImg(thumbScreen, Configuration.getInt(Parameter.SMALL_SCREEN_WIDTH),
                        Configuration.getInt(Parameter.SMALL_SCREEN_HEIGHT), thumbScreenPath);

                // Uploading screenshot to Amazon S3
                uploadToAmazonS3(screenshot);

                // add screenshot comment to collector
                ReportContext.addScreenshotComment(screenName, comment);
            } catch (IOException e) {
                LOGGER.error("Unable to capture screenshot due to the I/O issues!", e);
            } catch (WebDriverException e) {
            	LOGGER.error("Unable to capture screenshot due to the WebDriverException!", e);
            	e.printStackTrace();
            } catch (Exception e) {
                LOGGER.error("Unable to capture screenshot due to the Exception!", e);
            } finally {
            	Timer.stop(ACTION_NAME.CAPTURE_SCREENSHOT);
            }
        }
        LOGGER.debug("Screenshot->capture finished.");
        return screenName;
    }

    /**
     * Upload screenshot file to Amazon S3 using Zafira Client
     * @param screenshot - existing screenshot {@link File}
     */
    private static void uploadToAmazonS3(File screenshot) {
        if (!Configuration.getBoolean(Parameter.S3_SAVE_SCREENSHOTS)) {
            LOGGER.debug("there is no sense to continue as saving screenshots onto S3 is disabled.");
            return;
        }
        final String correlationId = UUID.randomUUID().toString();
        final String ciTestId = ZafiraListener.getThreadCiTestId();
        try {
            ZafiraMessager.<MetaInfoMessage>custom(MetaInfoLevel.META_INFO, new MetaInfoMessage()
                    .addHeader("AMAZON_PATH", null)
                    .addHeader("AMAZON_PATH_CORRELATION_ID", correlationId));
            executorService.execute(() -> {
                try {
                    int expiresIn = Configuration.getInt(Configuration.Parameter.ARTIFACTS_EXPIRATION_SECONDS);
                	LOGGER.debug("Uploading to AWS: " + screenshot.getName() + ". Expires in " + expiresIn + " seconds.");
                    String url = ZafiraSingleton.INSTANCE.getClient().uploadFile(screenshot, expiresIn, String.format(AMAZON_KEY_FORMAT, DATE_FORMAT.format(new Date())));
                    LOGGER.debug("Uploaded to AWS: " + screenshot.getName());
                    ZafiraMessager.<MetaInfoMessage>custom(MetaInfoLevel.META_INFO, new MetaInfoMessage()
                            .addHeader("AMAZON_PATH", url)
                            .addHeader("CI_TEST_ID", ciTestId)
                            .addHeader("AMAZON_PATH_CORRELATION_ID", correlationId));
                    LOGGER.debug("Updated AWS metadata: " + screenshot.getName());
                } catch (Exception e) {
                    LOGGER.debug("Can't save file to Amazon S3!", e);
                }
            });
        } catch (Exception e) {
            LOGGER.debug("Can't save file to Amazon S3!", e);
        }
    }

    /**
     * Resizes image according to specified dimensions.
     * 
     * @param bufferedImage
     *            - image to resize.
     * @param width
     *            - new image width.
     * @param height
     *            - new image height.
     * @param path
     *            - path to screenshot file.
     */
    private static void resizeImg(BufferedImage bufferedImage, int width, int height, String path) {
        try {
            BufferedImage bufImage = Scalr.resize(bufferedImage, Scalr.Method.BALANCED, Scalr.Mode.FIT_TO_WIDTH, width, height,
                    Scalr.OP_ANTIALIAS);
            if (bufImage.getHeight() > height) {
                bufImage = Scalr.crop(bufImage, bufImage.getWidth(), height);
            }
            ImageIO.write(bufImage, "png", new File(path));
        } catch (Exception e) {
            LOGGER.error("Image scaling problem!", e);
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
    private static BufferedImage takeFullScreenshot(WebDriver driver, WebDriver augmentedDriver) throws Exception {
        BufferedImage screenShot;
        if (driver.getClass().toString().contains("java_client")) {
            // Mobile Native app
            File screenshot = ((AppiumDriver<?>) driver).getScreenshotAs(OutputType.FILE);
            screenShot = ImageIO.read(screenshot);
        } else if (Configuration.getDriverType().equals(SpecialKeywords.MOBILE)) {
            // Mobile web
            screenShot = ImageIO.read(((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE));
        } else {
            // regular web
            ru.yandex.qatools.ashot.Screenshot screenshot = new AShot()
                    .shootingStrategy(ShootingStrategies.viewportPasting(100)).takeScreenshot(augmentedDriver);
            screenShot = screenshot.getImage();
        }

        return screenShot;
    }

    /**
     * Makes screenshot of visible part of the page
     *
     * @param augmentedDriver
     *            - webDriver.
     * @exception IOException
     * 
     * @return screenshot image
     */
    private static BufferedImage takeVisibleScreenshot(WebDriver augmentedDriver) throws Exception {
    	return ImageIO.read(((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE));
    }
    

	/**
	 * Analyze if screenshot can be captured using the most common reason when
	 * driver is died etc.
	 * 
	 * @param message
	 *            - error message (stacktrace).
	 * 
	 * @return boolean
	 */
	public static boolean isCaptured(String message) {
		// [VD] do not use below line as it is too common!
		// || message.contains("timeout")
		if (message == null) {
			// unable to detect driver invalid status so return true
			return true;
		}
		// disable screenshot if error message contains any of this info
		boolean disableScreenshot = message.contains("StaleObjectException")
				|| message.contains("StaleElementReferenceException")
				|| message.contains("A session is either terminated or not started")
				|| message.contains("was terminated due to FORWARDING_TO_NODE_FAILED")
				|| message.contains("was terminated due to CLIENT_STOPPED_SESSION")
				|| message.contains("InvalidElementStateException") || message.contains("stale element reference")
				|| message.contains("no such element: Unable to locate element")
				|| message.contains("no such window: window was already closed")
				|| message.contains("An element could not be located on the page using the given search parameters")
				|| message.contains("current view have 'secure' flag set")
				|| message.contains("Error communicating with the remote browser. It may have died")
				|| message.contains("unexpected alert open") || message.contains("chrome not reachable")
				|| message.contains("cannot forward the request Connect to")
				|| message.contains("Session ID is null. Using WebDriver after calling quit")
				|| message.contains("was terminated due to TIMEOUT")
				|| message.contains("was terminated due to BROWSER_TIMEOUT")
				|| message.contains("Could not proxy command to remote server. Original error: Error: read ECONNRESET")
				|| message.contains("Session ID is null. Using WebDriver after calling quit()")
				|| message.contains("Unable to find elements by Selenium")
				|| message.contains("Unable to locate element")
				|| message.contains("Session timed out or not found");
		return !disableScreenshot;
	}
}
