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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import com.qaprosoft.amazon.client.AmazonS3Client;
import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.performance.ACTION_NAME;
import com.qaprosoft.carina.core.foundation.performance.Timer;
import com.qaprosoft.carina.core.foundation.report.Artifacts;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.async.AsyncOperation;
import com.qaprosoft.carina.core.foundation.utils.messager.ZafiraMessager;
import com.qaprosoft.carina.core.foundation.webdriver.augmenter.DriverAugmenter;
import com.qaprosoft.carina.core.foundation.webdriver.screenshot.IScreenshotRule;
import com.qaprosoft.zafira.listener.ZafiraEventRegistrar;
import com.qaprosoft.zafira.log.domain.MetaInfoMessage;
import com.qaprosoft.zafira.log.log4j.level.MetaInfoLevel;
import com.qaprosoft.zafira.models.dto.aws.FileUploadType;

import io.appium.java_client.AppiumDriver;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

/**
 * Screenshot manager for operation with screenshot capturing, resizing and removing of old screenshot folders.
 *
 * @author Alex Khursevich
 */
public class Screenshot {
    private static final Logger LOGGER = Logger.getLogger(Screenshot.class);

    private static List<IScreenshotRule> rules = Collections.synchronizedList(new ArrayList<IScreenshotRule>());

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
        return capture(driver, isTakeScreenshotRules, comment, false, false);
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
        String screenName = capture(driver, true, comment, true, false);

        //do not generate UI dump if no screenshot 
        if (!screenName.isEmpty()) {
            // XML layout extraction
            File uiDumpFile = IDriverPool.getDefaultDevice().generateUiDump(screenName);
            if (uiDumpFile != null) {
                LOGGER.debug("Dump file will be uploaded to amazon S3. File name is : " + uiDumpFile.getName());
                Artifacts.add("Failure UI dump report", uiDumpFile);
            } else {
                LOGGER.debug("Dump file is empty.");
            }
        }
        LOGGER.debug("Screenshot->captureFailure finished.");
        return screenName;
    }

    /**
     * Captures full size screenshot based on auto_screenshot global parameter, creates thumbnail and copies both images to specified screenshots
     * location.
     *
     * @param driver
     *          instance used for capturing.
     * @param comment 
     * 			String comment
     * @param artifact
     * 			boolean artifact
     * @return screenshot name.
     */
    public static BufferedImage captureFullSize(WebDriver driver, String comment, boolean artifact) {
        String screenName;
        BufferedImage screen = null;

        LOGGER.debug("Screenshot->capture starting...");

        try {
            if (!isCaptured(comment)) {
                LOGGER.error("Unable to capture screenshot as driver seems invalid: " + comment);
                return null;
            }

            Timer.start(ACTION_NAME.CAPTURE_SCREENSHOT);
            // Define test screenshot root
            File testScreenRootDir = ReportContext.getTestDir();

            // Capture full page screenshot and resize
            screenName = comment + ".png";
            String screenPath = testScreenRootDir.getAbsolutePath() + "/" + screenName;

            WebDriver augmentedDriver = driver;

            if (!driver.toString().contains("AppiumNativeDriver")) {
                // do not augment for Appium 1.x anymore
                augmentedDriver = new DriverAugmenter().augment(driver);
            }

            screen = takeFullScreenshot(driver, augmentedDriver);

            if (screen == null) {
                //do nothing and return empty
                return null;
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
            File screenshotThumb = new File(thumbScreenPath);
            ImageIO.write(thumbScreen, "PNG", screenshotThumb);

            resizeImg(thumbScreen, Configuration.getInt(Parameter.SMALL_SCREEN_WIDTH),
                    Configuration.getInt(Parameter.SMALL_SCREEN_HEIGHT), thumbScreenPath);

            // Uploading screenshot to Amazon S3
            uploadToAmazonS3(screenshot, screenshotThumb, comment, artifact);

            // add screenshot comment to collector
            ReportContext.addScreenshotComment(screenName, comment);
            return screen;
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
        LOGGER.debug("Screenshot->capture finished.");
        return screen;
    }

    public static BufferedImage captureFullSize(WebDriver driver, String comment) {
        return captureFullSize(driver, comment, false);
    }

    /**
     * Captures screenshot with comment based on auto_screenshot global parameter, creates thumbnail and copies both images to specified screenshots
     * location.
     *
     * @param driver
     *            instance used for capturing.
     * @param comment 
     * 			String comment
   	 * @param artifact
     * 			boolean artifact
     * @return screenshot name.
     */
    public static String capture(WebDriver driver, String comment, boolean artifact) {
        return capture(driver, Configuration.getBoolean(Parameter.AUTO_SCREENSHOT), comment, false, artifact);
    }

    public static String capture(WebDriver driver, String comment) {
        return capture(driver, comment, false);
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
        return capture(driver, isTakeScreenshot, "", false, false);

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
        return capture(driver, isTakeScreenshot, comment, false, false);
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

    private static String capture(WebDriver driver, boolean isTakeScreenshot, String comment, boolean fullSize, boolean artifact) {
        String screenName = "";

        // TODO: AUTO-2883 make full size screenshot generation only when fullSize == true
        // For the rest of cases returned previous implementation

        LOGGER.debug("Screenshot->capture starting...");

        if (isTakeScreenshot) {
            Timer.start(ACTION_NAME.CAPTURE_SCREENSHOT);
            try {
            	if (!isCaptured(comment)) {
            		LOGGER.error("Unable to capture screenshot as driver seems invalid: " + comment);
            		return screenName;
            	}
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
                File screenshotThumb = new File(thumbScreenPath);
                ImageIO.write(thumbScreen, "PNG", screenshotThumb);
                resizeImg(thumbScreen, Configuration.getInt(Parameter.SMALL_SCREEN_WIDTH),
                        Configuration.getInt(Parameter.SMALL_SCREEN_HEIGHT), thumbScreenPath);

                // Uploading screenshot to Amazon S3
                uploadToAmazonS3(screenshot, screenshotThumb, comment, artifact);

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

    private static void uploadToAmazonS3(File screenshot, File screenshotThumb, String comment, boolean artifact) {
        final String correlationId = UUID.randomUUID().toString();
        final String ciTestId = ZafiraEventRegistrar.getThreadCiTestId();
        Optional<CompletableFuture<String>> originalScreenshotFuture = uploadToAmazonS3(screenshot, comment, correlationId, ciTestId, false);
        Optional<CompletableFuture<String>> thumbFuture = uploadToAmazonS3(screenshotThumb, comment, correlationId, ciTestId, true);
        originalScreenshotFuture.ifPresent(of -> thumbFuture.ifPresent(tf -> {
            if(artifact) {
                List<CompletableFuture<String>> urlFutures = Stream.of(of, tf).collect(Collectors.toList());
                Artifacts.add(urlFutures, comment);
            } else {
                AsyncOperation.add(of, tf);
            }
        }));
    }

    /**
     * Upload screenshot file to Amazon S3 using Zafira Client
     * @param screenshot - existing screenshot {@link File}
     */
    private static Optional<CompletableFuture<String>> uploadToAmazonS3(File screenshot, String comment, String correlationId, String ciTestId,
            boolean thumb) {
        final String pathHeader = thumb ? "THUMB_AMAZON_PATH" : "AMAZON_PATH";
        return AmazonS3Client.upload(screenshot,
                () -> ZafiraMessager.custom(MetaInfoLevel.META_INFO, new MetaInfoMessage()
                        .addHeader(pathHeader, null)
                        .addHeader("AMAZON_PATH_CORRELATION_ID", correlationId)),
                url -> ZafiraMessager.custom(MetaInfoLevel.META_INFO, new MetaInfoMessage()
                        .addHeader(pathHeader, url)
                        .addHeader("CI_TEST_ID", ciTestId)
                        .addHeader("AMAZON_PATH_CORRELATION_ID", correlationId)),
                FileUploadType.Type.SCREENSHOTS);
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
            ru.yandex.qatools.ashot.Screenshot screenshot;
            if (Configuration.getPlatform().equals("ANDROID")) {
                String pixelRatio = String.valueOf(((EventFiringWebDriver) augmentedDriver).getCapabilities().getCapability("pixelRatio"));
                if (!pixelRatio.equals("null")) {
                    float dpr = Float.parseFloat(pixelRatio);
                    screenshot = (new AShot()).shootingStrategy(ShootingStrategies
                            .viewportRetina(SpecialKeywords.DEFAULT_SCROLL_TIMEOUT, SpecialKeywords.DEFAULT_BLOCK, SpecialKeywords.DEFAULT_BLOCK,
                                    dpr))
                            .takeScreenshot(augmentedDriver);
                    screenShot = screenshot.getImage();
                } else {
                    screenshot = (new AShot()).shootingStrategy(ShootingStrategies
                            .viewportRetina(SpecialKeywords.DEFAULT_SCROLL_TIMEOUT, SpecialKeywords.DEFAULT_BLOCK, SpecialKeywords.DEFAULT_BLOCK,
                                    SpecialKeywords.DEFAULT_DPR))
                            .takeScreenshot(augmentedDriver);
                    screenShot = screenshot.getImage();
                }
            } else {
                int deviceWidth = augmentedDriver.manage().window().getSize().getWidth();
                String deviceName = String.valueOf(((EventFiringWebDriver) augmentedDriver).getCapabilities().getCapability("deviceName"));
                screenshot = new AShot().shootingStrategy(getScreenshotShuttingStrategy(deviceWidth, deviceName)).takeScreenshot(augmentedDriver);
                screenShot = screenshot.getImage();
            }
        } else {
            // regular web
            ru.yandex.qatools.ashot.Screenshot screenshot;
                screenshot = (new AShot()).shootingStrategy(ShootingStrategies.viewportPasting(SpecialKeywords.DEFAULT_SCROLL_TIMEOUT))
                        .takeScreenshot(augmentedDriver);
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
	public static boolean isCaptured(String message){
		// [VD] do not use below line as it is too common!
		// || message.contains("timeout")
		if (message == null) {
			// unable to detect driver invalid status so return true
			return true;
		}
		// disable screenshot if error message contains any of this info
		boolean disableScreenshot = message.contains("StaleObjectException")
				|| message.contains("StaleElementReferenceException")
				|| message.contains("Session ID is null. Using WebDriver after calling quit")
				|| message.contains("A session is either terminated or not started")
                || message.contains("invalid session id")
				|| message.contains("Session timed out or not found")
				|| message.contains("cannot forward the request unexpected end of stream")
				|| message.contains("was terminated due to") // FORWARDING_TO_NODE_FAILED, CLIENT_STOPPED_SESSION, PROXY_REREGISTRATION, TIMEOUT, BROWSER_TIMEOUT etc
				|| message.contains("InvalidElementStateException") || message.contains("stale element reference")
				|| message.contains("no such element: Unable to locate element")
				|| message.contains("no such window: window was already closed")
				|| message.contains("An element could not be located on the page using the given search parameters")
				|| message.contains("current view have 'secure' flag set")
				|| message.contains("Error communicating with the remote browser. It may have died")
				|| message.contains("unexpected alert open") 
				|| message.contains("chrome not reachable")
				|| message.contains("cannot forward the request Connect to")
				|| message.contains("Could not proxy command to remote server. Original error:") // Error: socket hang up, Error: read ECONNRESET etc				
				|| message.contains("Unable to find elements by Selenium")
				|| message.contains("generateUiDump") //do not generate screenshot if getPageSource is invalid
				|| message.contains("Expected to read a START_MAP but instead have: END") // potential drivers issues fix for moon 
				|| message.contains("Unable to locate element");
		return !disableScreenshot;
	}

    /**
     * Compares two different screenshots
     *
     * @param bufferedImageExpected - old image
     * @param bufferedImageActual   - new image
     * @param comment  - String
     * @param artifact  - boolean
     * @return boolean
     */
    public static boolean isScreenshotDiff(BufferedImage bufferedImageExpected, BufferedImage bufferedImageActual, String comment, boolean artifact) {
        String screenName;
        BufferedImage screen;
        try {
            ImageDiffer imageDiff = new ImageDiffer();
            ImageDiff diff = imageDiff.makeDiff(bufferedImageExpected, bufferedImageActual);
            if (diff.hasDiff()) {
                screen = diff.getMarkedImage();
                Timer.start(ACTION_NAME.CAPTURE_SCREENSHOT);
                // Define test screenshot root
                File testScreenRootDir = ReportContext.getTestDir();

                screenName = comment + ".png";
                String screenPath = testScreenRootDir.getAbsolutePath() + "/" + screenName;

                BufferedImage thumbScreen = screen;

                if (Configuration.getInt(Parameter.BIG_SCREEN_WIDTH) != -1
                        && Configuration.getInt(Parameter.BIG_SCREEN_HEIGHT) != -1) {
                    resizeImg(screen, Configuration.getInt(Parameter.BIG_SCREEN_WIDTH),
                            Configuration.getInt(Parameter.BIG_SCREEN_HEIGHT), screenPath);
                }

                File screenshot = new File(screenPath);
                FileUtils.touch(screenshot);
                ImageIO.write(screen, "PNG", screenshot);

                // Create comparative screenshot thumbnail
                String thumbScreenPath = screenPath.replace(screenName, "/thumbnails/" + screenName);
                File screenshotThumb = new File(thumbScreenPath);
                ImageIO.write(thumbScreen, "PNG", screenshotThumb);
                resizeImg(thumbScreen, Configuration.getInt(Parameter.SMALL_SCREEN_WIDTH),
                        Configuration.getInt(Parameter.SMALL_SCREEN_HEIGHT), thumbScreenPath);

                // Uploading comparative screenshot to Amazon S3
                uploadToAmazonS3(screenshot, screenshotThumb, comment, artifact);
            }
            else {
                LOGGER.info("Unable to create comparative screenshot, there is no difference between images!");
                return false;
            }
        } catch (IOException exception) {
            LOGGER.error("Unable to compare screenshots due to the I/O issues!", exception);
        } catch (WebDriverException exception) {
            LOGGER.error("Unable to compare screenshots due to the WebDriverException!", exception);
        } catch (NullPointerException exception) {
            LOGGER.error("Unable to compare screenshots due to the NullPointerException", exception);
        } catch (Exception exception) {
            LOGGER.error("Unable to compare screenshots!", exception);
        } finally {
            Timer.stop(ACTION_NAME.CAPTURE_SCREENSHOT);
        }
        return true;
    }

    private static ShootingStrategy getScreenshotShuttingStrategy(int deviceWidth, String deviceName) {
        switch (deviceWidth) {
        case SpecialKeywords.DEFAULT_WIDTH:
            if (deviceName.contains("X")) {
                return ShootingStrategies.viewportRetina(SpecialKeywords.DEFAULT_SCROLL_TIMEOUT, SpecialKeywords.IPHONE_X_HEADER,
                        SpecialKeywords.ALTERNATIVE_IOS_FOOTER, SpecialKeywords.IPHONE_X_DPR);
            } else {
                return ShootingStrategies.viewportRetina(SpecialKeywords.DEFAULT_SCROLL_TIMEOUT, SpecialKeywords.DEFAULT_IOS_HEADER,
                        SpecialKeywords.DEFAULT_BLOCK, SpecialKeywords.DEFAULT_DPR);
            }
        case SpecialKeywords.DEFAULT_PLUS_WIDTH:
            if (deviceName.contains("XR")) {
                return ShootingStrategies.viewportRetina(SpecialKeywords.DEFAULT_SCROLL_TIMEOUT, SpecialKeywords.IPHONE_X_HEADER,
                        SpecialKeywords.ALTERNATIVE_IOS_FOOTER, SpecialKeywords.DEFAULT_DPR);
            } else {
                return ShootingStrategies.viewportRetina(SpecialKeywords.DEFAULT_SCROLL_TIMEOUT, SpecialKeywords.IPHONE_PLUS_HEADER,
                        SpecialKeywords.DEFAULT_BLOCK, SpecialKeywords.IPHONE_X_DPR);
            }
        case SpecialKeywords.DEFAULT_IPAD_WIDTH:
            return ShootingStrategies.viewportRetina(SpecialKeywords.DEFAULT_SCROLL_TIMEOUT, SpecialKeywords.IPAD_HEADER,
                    SpecialKeywords.DEFAULT_BLOCK, SpecialKeywords.DEFAULT_DPR);
        case SpecialKeywords.DEFAULT_SE_WIDTH:
            return ShootingStrategies.viewportRetina(SpecialKeywords.DEFAULT_SCROLL_TIMEOUT, SpecialKeywords.DEFAULT_IOS_HEADER,
                    SpecialKeywords.ALTERNATIVE_IOS_FOOTER, SpecialKeywords.DEFAULT_DPR);
        default:
            return ShootingStrategies.viewportRetina(SpecialKeywords.DEFAULT_SCROLL_TIMEOUT, SpecialKeywords.DEFAULT_IOS_HEADER,
                    SpecialKeywords.DEFAULT_BLOCK, SpecialKeywords.DEFAULT_DPR);
        }
    }
}
