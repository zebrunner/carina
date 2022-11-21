/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
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
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.imgscalr.Scalr;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.decorators.Decorated;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.utils.report.ReportContext;
import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.screenshot.IScreenshotRule;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.windows.WindowsDriver;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.comparison.DiffMarkupPolicy;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;
import ru.yandex.qatools.ashot.comparison.PointsMarkupPolicy;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

/**
 * Screenshot manager for operation with screenshot capturing, resizing and removing of old screenshot folders.
 *
 * @author Alex Khursevich
 */
public class Screenshot {
    //TODO: deprecate and later move to provate all capture methods allowing to do captureByRule only.
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static List<IScreenshotRule> rules = Collections.synchronizedList(new ArrayList<IScreenshotRule>());
    
    private static final String ERROR_STACKTRACE = "Error stacktrace: ";
    
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
     * Delete rule
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

    /**
     * Clear all rules and disable all kind of screenshots even for failures!
     */
    public static void clearRules() {
        LOGGER.warn("All screenshot capture rules will be deleted. Automatic capturing disabled even for failures!");
        rules.clear();
    }

    /**
     * Captures visible screenshot explicitly by any rule, creates thumbnail and copies both images to specified screenshots
     * location.
     *
     * @param driver
     *            instance used for capturing.
     * @param comment String
     * @return screenshot name.
     */
    public static String captureByRule(WebDriver driver, String comment) {
        return captureByRule(driver, comment, false);
    }
    
    /**
     * Captures screenshot explicitly by any rule, creates thumbnail and copies both images to specified screenshots
     * location.
     *
     * @param driver
     *            instance used for capturing.
     * @param comment String
     * @param isFullSize boolean
     * @return screenshot name.
     */
    public static String captureByRule(WebDriver driver, String comment, boolean isFullSize) {
        boolean isTakeScreenshotRules = false;
        for (IScreenshotRule iScreenshotRule : rules) {
            isTakeScreenshotRules = iScreenshotRule.isTakeScreenshot();
            if (isTakeScreenshotRules) {
                isFullSize = isFullSize && iScreenshotRule.isAllowFullSize(); 
                break;
            }
        }
        return capture(driver, isTakeScreenshotRules, comment, isFullSize);
    }

    /**
     * Captures visible screenshot explicitly ignoring any rules, creates thumbnail and copies both images to specified screenshots
     * location.
     *
     * @param driver
     *            instance used for capturing.
     * @param comment String
     * @return screenshot name.
     */
    public static String capture(WebDriver driver, String comment) {
        return capture(driver, comment, false);
    }
    
    /**
     * Captures screenshot explicitly ignoring any rules, creates thumbnail and copies both images to specified screenshots
     * location.
     *
     * @param driver
     *            instance used for capturing.
     * @param comment String
     * @param isFullSize boolean
     * @return screenshot name.
     */
    public static String capture(WebDriver driver, String comment, boolean isFullSize) {
        return capture(driver, true, comment, isFullSize);
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

        if (isTakeScreenshot) {
            LOGGER.debug("Screenshot->capture starting...");
            
            driver = castDriver(driver); // remove all DriverListener casting to WebDriver
            try {
            	if (!isCaptured(comment)) {
                    // [VD] do not write something to log as this original exception is used as original exception for failure
            		//LOGGER.debug("Unable to capture screenshot as driver seems invalid: " + comment);
            		return screenName;
            	}
                // Define test screenshot root
                File testScreenRootDir = ReportContext.getTestDir();

                // Capture full page screenshot and resize
                screenName = System.currentTimeMillis() + ".png";
                String screenPath = testScreenRootDir.getAbsolutePath() + "/" + screenName;

                //hotfix to converting proxy into the valid driver
                if (driver instanceof Proxy) {
    				try {
        				InvocationHandler innerProxy = Proxy.getInvocationHandler((Proxy) driver);
        				// "arg$2" is by default RemoteWebDriver;
        				// "arg$1" is EventFiringWebDriver
        				// wrap into try/catch to make sure we don't affect test execution
        				Field locatorField = innerProxy.getClass().getDeclaredField("arg$2");
        				locatorField.setAccessible(true);

        				driver = (WebDriver) locatorField.get(innerProxy);
    				} catch (Exception e) {
    					//do nothing and receive augmenting warning in the logs
    				}
                }

                BufferedImage screen;

                // Create screenshot
                if (fullSize) {
                    screen = takeFullScreenshot(driver);
                } else {
                    screen = takeVisibleScreenshot(driver);
                }

                if (screen == null) {
                	//do nothing and return empty
                	return "";
                }

                if (Configuration.getInt(Parameter.BIG_SCREEN_WIDTH) != -1
                        && Configuration.getInt(Parameter.BIG_SCREEN_HEIGHT) != -1) {
                    resizeImg(screen, Configuration.getInt(Parameter.BIG_SCREEN_WIDTH),
                            Configuration.getInt(Parameter.BIG_SCREEN_HEIGHT), screenPath);
                }

                File screenshot = new File(screenPath);

                ImageIO.write(screen, "PNG", screenshot);

                if (!comment.isEmpty()) {
                    LOGGER.info(comment);
                    // add screenshot comment to collector
                    ReportContext.addScreenshotComment(screenName, comment);
                }
                //upload screenshot to Zebrunner Reporting
                com.zebrunner.agent.core.registrar.Screenshot.upload(Files.readAllBytes(screenshot.toPath()), Instant.now().toEpochMilli());
            } catch (NoSuchWindowException e) {
                LOGGER.warn("Unable to capture screenshot due to NoSuchWindowException!");
                LOGGER.debug(ERROR_STACKTRACE, e);
            } catch (IOException e) {
                LOGGER.warn("Unable to capture screenshot due to the I/O issues!");
                LOGGER.debug(ERROR_STACKTRACE, e);
            } catch (WebDriverException e) {
                LOGGER.warn("Unable to capture screenshot due to the WebDriverException!");
                LOGGER.debug(ERROR_STACKTRACE, e);
            } catch (Exception e) {
                LOGGER.warn("Unable to capture screenshot due to the Exception!");
                LOGGER.debug(ERROR_STACKTRACE, e);
            } finally {
                LOGGER.debug("Screenshot->capture finished.");
            }
        }
        
        return screenName;
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
     * Makes full size screenshot using javascript (May not work properly with
     * popups and active js-elements on the page)
     *
     * @param driver web driver without listeners (original)
     * @exception Exception can be caused by read() or getScreenshotAs() methods
     *
     * @return a screenshot if it was produced successfully, or null otherwise
     */
    private static BufferedImage takeFullScreenshot(WebDriver driver) {
        BufferedImage screenShot = null;
        // default timeout for full size screenshot is EXPLICIT_TIMEOUT /2
        long timeout = Configuration.getInt(Parameter.EXPLICIT_TIMEOUT) / 2;
        setPageLoadTimeout(driver, timeout);

        Wait<WebDriver> wait = new FluentWait<>(driver)
                .pollingEvery(Duration.ofSeconds(timeout))
                .withTimeout(Duration.ofSeconds(timeout));

        try {
            LOGGER.debug("starting full size screenshot capturing...");
            if (driver.getClass().toString().contains("windows")) {
                File capturedScreenshot = wait.until(drv -> ((WindowsDriver) drv)
                        .getScreenshotAs(OutputType.FILE));
                screenShot = ImageIO.read(capturedScreenshot);
                // Appium driver and it's descendant classes contains 'java_client' in classname
            } else if (driver.getClass().toString().contains("java_client")) {
                // Mobile Native app
                File capturedScreenshot = wait.until(drv -> ((AppiumDriver) drv)
                        .getScreenshotAs(OutputType.FILE));
                screenShot = ImageIO.read(capturedScreenshot);
            } else if (Configuration.getDriverType().equals(SpecialKeywords.MOBILE)) {

                if (Configuration.getPlatform().equals("ANDROID")) {
                    String pixelRatio = String.valueOf(((HasCapabilities) driver).getCapabilities().getCapability("pixelRatio"));
                    float dpr = !pixelRatio.equals("null") ? Float.parseFloat(pixelRatio) : SpecialKeywords.DEFAULT_DPR;
                    screenShot = wait.until(drv -> (new AShot()).shootingStrategy(ShootingStrategies
                            .viewportRetina(SpecialKeywords.DEFAULT_SCROLL_TIMEOUT, SpecialKeywords.DEFAULT_BLOCK, SpecialKeywords.DEFAULT_BLOCK,
                                    dpr))
                            .takeScreenshot(drv))
                            .getImage();
                } else {
                    int deviceWidth = driver.manage().window().getSize().getWidth();
                    String deviceName = String.valueOf(((HasCapabilities) driver)
                            .getCapabilities()
                            .getCapability(MobileCapabilityType.DEVICE_NAME));

                    screenShot = wait.until(drv -> new AShot()
                            .shootingStrategy(getScreenshotShuttingStrategy(deviceWidth, deviceName))
                            .takeScreenshot(driver))
                            .getImage();
                }
            } else {
                // regular web
                screenShot = wait.until(drv -> new AShot()
                        .shootingStrategy(ShootingStrategies.viewportPasting(SpecialKeywords.DEFAULT_SCROLL_TIMEOUT))
                        .takeScreenshot(drv))
                        .getImage();
            }
        } catch (TimeoutException e) {
            LOGGER.warn("Unable to capture screenshot during {} sec!", timeout);
        } catch (Exception e) {
            // for undefined failure keep full stacktrace to handle later correctly!
            LOGGER.error("Undefined error on capture full screenshot detected!", e);
        } finally {
            // restore default pageLoadTimeout driver timeout
            setPageLoadTimeout(driver, getPageLoadTimeout());
            LOGGER.debug("finished full size screenshot call.");
        }
        return screenShot;
    }

    /**
     * Take screenshot of visible part of the page
     *
     * @param driver web driver without listeners (original)
     * @exception Exception can be caused by read() or getScreenshotAs() methods
     *
     * @return a screenshot if it was produced successfully, or null otherwise
     */
    private static BufferedImage takeVisibleScreenshot(WebDriver driver) throws Exception {
        BufferedImage screenShot = null;
        // default timeout for driver quit 1/3 of explicit
        long timeout = Configuration.getInt(Parameter.EXPLICIT_TIMEOUT) / 3;
        setPageLoadTimeout(driver, timeout);
        try {
            Wait<WebDriver> wait = new FluentWait<>(driver)
                    .pollingEvery(Duration.ofSeconds(timeout))
                    .withTimeout(Duration.ofSeconds(timeout));

            LOGGER.debug("starting screenshot capturing...");
            File capturedScreenshot = wait.until(drv -> ((TakesScreenshot) drv)
                    .getScreenshotAs(OutputType.FILE));
            screenShot = ImageIO.read(capturedScreenshot);
        } catch (TimeoutException e) {
            LOGGER.warn("Unable to capture screenshot during {} sec!", timeout);
        } catch (Exception e) {
            LOGGER.error("Undefined error on capture screenshot detected: {}", e.getMessage());
        } finally {
            // restore default pageLoadTimeout driver timeout
            setPageLoadTimeout(driver, getPageLoadTimeout());
            LOGGER.debug("finished screenshot call.");
        }
        return screenShot;
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
		boolean isContains = message.contains("StaleObjectException")
		        || message.contains("NoSuchSessionException")
				|| message.contains("StaleElementReferenceException")
				|| message.contains("stale_element_reference.html")
				|| message.contains("Error executing JavaScript")
				|| message.contains("Session ID is null. Using WebDriver after calling quit")
				|| message.contains("A session is either terminated or not started")
                || message.contains("invalid session id")
                || message.contains("Session does not exist")
                || message.contains("not found in active sessions")
				|| message.contains("Session timed out or not found")
				|| message.contains("Unable to determine type from: <. Last 1 characters read")
				|| message.contains("not available and is not among the last 1000 terminated sessions")				
				|| message.contains("cannot forward the request")
                || message.contains("connect ECONNREFUSED")
				|| message.contains("was terminated due to") // FORWARDING_TO_NODE_FAILED, CLIENT_STOPPED_SESSION, PROXY_REREGISTRATION, TIMEOUT, BROWSER_TIMEOUT etc
				|| message.contains("InvalidElementStateException")
				|| message.contains("no such element: Unable to locate element")
				|| message.contains("https://www.seleniumhq.org/exceptions/no_such_element.html") // use-case for Safari driver
				|| message.contains("no such window: window was already closed")
				|| message.contains("Method is not implemented") //to often exception for mobile native app testing
				// [VD] exclude below condition otherwise we overload appium when fluent wait looking for device and doing screenshot in a loop 
				|| message.contains("An element could not be located on the page using the given search parameters")
				|| message.contains("current view have 'secure' flag set")
				|| message.contains("Error communicating with the remote browser. It may have died")
				|| message.contains("unexpected alert open") 
				|| message.contains("chrome not reachable")
				|| message.contains("cannot forward the request Connect to")
				|| message.contains("Could not proxy command to remote server. Original error:") // Error: socket hang up, Error: read ECONNRESET etc				
				|| message.contains("Could not proxy command to the remote server. Original error:") // Different messages on some Appium versions
				|| message.contains("Unable to find elements by Selenium")
				|| message.contains("generateUiDump") //do not generate screenshot if getPageSource is invalid
				|| message.contains("Expected to read a START_MAP but instead have: END") // potential drivers issues fix for moon
				|| message.contains("An unknown error has occurred") //
				|| message.contains("Unable to find element with")
				|| message.contains("Unable to locate element")
				|| message.contains("Illegal base64 character 2e")
				|| message.contains("javascript error: Cannot read property 'outerHTML' of null")
				|| message.contains("Driver connection refused")
                || message.contains("tab crashed")				
				// carina based errors which means that driver is not ready for screenshoting
				|| message.contains("Unable to open url during");

		if (!isContains) {
		    // for released builds put below message to debug  
		    LOGGER.debug("isCaptured->message: '" + message + "'");
		    // for snapshot builds use info to get more useful information
		    //LOGGER.info("isCaptured->message: '" + message + "'");
		}
		return !isContains;
	}

    public static boolean compare(BufferedImage bufferedImageExpected, BufferedImage bufferedImageActual, String comment, boolean artifact) {
        return compare(bufferedImageExpected, bufferedImageActual, comment, artifact, new PointsMarkupPolicy());
    }

    /**
     * Compares two different screenshots
     *
     * @param bufferedImageExpected - old image
     * @param bufferedImageActual   - new image
     * @param comment               - String
     * @param artifact              - boolean
     * @param markupPolicy          - DiffMarkupPolicy
     * @return boolean
     */
    public static boolean compare(BufferedImage bufferedImageExpected, BufferedImage bufferedImageActual, String comment, boolean artifact, DiffMarkupPolicy markupPolicy) {
        String screenName;
        BufferedImage screen;
        try {
            ImageDiffer imageDiffer = new ImageDiffer();
            imageDiffer.withDiffMarkupPolicy(markupPolicy);
            ImageDiff diff = imageDiffer.makeDiff(bufferedImageExpected, bufferedImageActual);
            if (diff.hasDiff()) {
                screen = diff.getMarkedImage();
                // Define test screenshot root
                File testScreenRootDir = ReportContext.getTestDir();

                screenName = comment + ".png";
                String screenPath = testScreenRootDir.getAbsolutePath() + "/" + screenName;

                if (Configuration.getInt(Parameter.BIG_SCREEN_WIDTH) != -1
                        && Configuration.getInt(Parameter.BIG_SCREEN_HEIGHT) != -1) {
                    resizeImg(screen, Configuration.getInt(Parameter.BIG_SCREEN_WIDTH),
                            Configuration.getInt(Parameter.BIG_SCREEN_HEIGHT), screenPath);
                }

                File screenshot = new File(screenPath);
                FileUtils.touch(screenshot);
                ImageIO.write(screen, "PNG", screenshot);

                // Uploading comparative screenshot to Amazon S3
                if (artifact){
                    com.zebrunner.agent.core.registrar.Artifact.attachToTest(comment + ".png", screenshot);
                } else {
                    com.zebrunner.agent.core.registrar.Screenshot.upload(Files.readAllBytes(screenshot.toPath()), Instant.now().toEpochMilli());
                }
            }
            else {
                LOGGER.info("Unable to create comparative screenshot, there is no difference between images!");
                return false;
            }
        } catch (IOException e) {
            LOGGER.warn("Unable to compare screenshots due to the I/O issues!");
            LOGGER.debug(ERROR_STACKTRACE, e);
        } catch (WebDriverException e) {
            LOGGER.warn("Unable to compare screenshots due to the WebDriverException!");
            LOGGER.debug(ERROR_STACKTRACE, e);
        } catch (NullPointerException e) {
            LOGGER.warn("Unable to compare screenshots due to the NullPointerException!");
            LOGGER.debug(ERROR_STACKTRACE, e);
        } catch (Exception e) {
            LOGGER.warn("Unable to compare screenshots!");
            LOGGER.debug(ERROR_STACKTRACE, e);
        } finally {
            // do nothing
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

    /**
     * Cast Carina driver to WebDriver removing all extra listeners (use it in problematic places where you handle all exceptions)
     *
     * @param drv WebDriver
     *
     * @return WebDriver
     */
    private static WebDriver castDriver(WebDriver drv) {
        if (drv instanceof Decorated<?>) {
            drv = ((Decorated<WebDriver>) drv).getOriginal();
        }
        return drv;
    }

    private static void setPageLoadTimeout(WebDriver drv, long timeout) {
        try {
            drv.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(timeout));
        } catch (UnsupportedCommandException e) {
            //TODO: review upcoming appium 2.0 changes
            LOGGER.debug("Appium: Not implemented yet for pageLoad timeout!");
        }
        
    }

    private static long getPageLoadTimeout() {
        long timeout = 300;
        // #1705: limit pageLoadTimeout driver timeout by idleTimeout
//      if (!R.CONFIG.get("capabilities.idleTimeout").isEmpty()) {
//          long idleTimeout = R.CONFIG.getLong("capabilities.idleTimeout");
//          if (idleTimeout < timeout) {
//              timeout = idleTimeout;
//          }
//      }
        return timeout;
    }
}
