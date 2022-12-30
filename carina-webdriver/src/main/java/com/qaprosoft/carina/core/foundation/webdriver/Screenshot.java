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
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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

import com.qaprosoft.carina.core.foundation.webdriver.screenshot.IScreenshotRule;
import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.Configuration.Parameter;
import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.utils.report.ReportContext;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.comparison.DiffMarkupPolicy;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;
import ru.yandex.qatools.ashot.comparison.PointsMarkupPolicy;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

/**
 * Screenshot manager.
 *
 * @author Alex Khursevich
 */
public final class Screenshot {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final List<IScreenshotRule> RULES = Collections.synchronizedList(new ArrayList<>());
    private static final Duration DEFAULT_PAGE_LOAD_TIMEOUT = Duration.ofSeconds(300);
    private static final String ERROR_STACKTRACE = "Error stacktrace: ";
    private static final String ACTUAL_RANGE_OF_SCREENSHOT_RULES_MESSAGE = "Actual range of screenshot rules: {}";
    
    private Screenshot() {
    	//hide default constructor
    }

    /**
     * Adds screenshot rule.
     *
     * @param rule {@link IScreenshotRule}
     * @return list of current rules {@link IScreenshotRule}s after adding
     */
    public static synchronized List<IScreenshotRule> addRule(IScreenshotRule rule) {
        return addScreenshotRule(rule);
    }

    /**
     * Adds screenshot rule.
     *
     * @deprecated for simplifying name of the method, use {@link #addRule(IScreenshotRule)} instead.
     * @param rule {@link IScreenshotRule}.
     * @return list of current rules {@link IScreenshotRule}s after adding
     */
    @Deprecated(forRemoval = true, since = "8.0.5")
    public static synchronized List<IScreenshotRule> addScreenshotRule(IScreenshotRule rule) {
        LOGGER.debug("Following rule will be added: {}", rule.getClass().getName());
        ScreenshotType screenshotType = rule.getEventType();
        Optional<IScreenshotRule> ruleByEventType = getRule(screenshotType);
        if (ruleByEventType.isPresent()) {
            LOGGER.debug("Rule with '{}' event type already exists and will be replaced by '{}'.",
                    screenshotType, rule.getClass().getName());
            ruleByEventType.ifPresent(RULES::remove);
        }
        RULES.add(rule);
        LOGGER.debug(ACTUAL_RANGE_OF_SCREENSHOT_RULES_MESSAGE, RULES);
        return RULES;
    }

    /**
     * Adds screenshot rules.
     *
     * @param rulesList list of {@link IScreenshotRule}s that will be added
     * @return list of current rules {@link IScreenshotRule}s after adding
     */
    public static synchronized List<IScreenshotRule> addRules(List<IScreenshotRule> rulesList) {
        return addScreenshotRules(rulesList);
    }

    /**
     * Adds screenshot rules.
     *
     * @deprecated for simplifying name of the method, use {@link #addRules(List)} instead.
     * @param rulesList list of {@link IScreenshotRule}s that will be added
     * @return list of current rules {@link IScreenshotRule}s after adding
     */
    @Deprecated(forRemoval = true, since = "8.0.5")
    public static synchronized List<IScreenshotRule> addScreenshotRules(List<IScreenshotRule> rulesList) {
        for (IScreenshotRule rule : rulesList) {
            addRule(rule);
        }
        return RULES;
    }

    /**
     * Remove rule if exists
     *
     * @param rule {@link IScreenshotRule} for removing
     * @return list of current {@link IScreenshotRule}s after removing rule
     */
    public static synchronized List<IScreenshotRule> removeRule(IScreenshotRule rule) {
        return removeScreenshotRule(rule);
    }

    /**
     * Remove rule if exists
     *
     * @deprecated for simplifying name of the method, use {@link #removeRule(IScreenshotRule)} instead.
     * @param rule {@link IScreenshotRule} for removing
     * @return list of current {@link IScreenshotRule}s after removing rule
     */
    @Deprecated(forRemoval = true, since = "8.0.5")
    public static synchronized List<IScreenshotRule> removeScreenshotRule(IScreenshotRule rule) {
        LOGGER.debug("Following rule will be removed if it exists: {}", rule.getClass().getName());
        RULES.remove(rule);
        LOGGER.debug("Actual range of screenshot rules: {}", RULES.toString());
        return RULES;
    }

    /**
     * Get screenshot rule by {@link ScreenshotType}.
     * 
     * @param screenshotType {@link ScreenshotType}.
     * @return {@link Optional} of {@link IScreenshotRule} if exists, {@link Optional#empty()} otherwise.
     */
    public static synchronized Optional<IScreenshotRule> getRule(ScreenshotType screenshotType) {
        return RULES.stream()
                .filter(r -> r.getEventType().equals(screenshotType))
                .findFirst();
    }

    /**
     * Remove screenshot rule by {@link ScreenshotType} if exists.
     *
     * @param screenshotType {@link ScreenshotType}.
     * @return {@link List} of {@link IScreenshotRule} after removing.
     */
    public static synchronized List<IScreenshotRule> removeRule(ScreenshotType screenshotType) {
        LOGGER.debug("Rule with even type '{}' will be removed if it exists.", screenshotType);
        Optional<IScreenshotRule> ruleByEventType = getRule(screenshotType);
        if (ruleByEventType.isPresent()) {
            LOGGER.debug("Detected rule '{}' with event type '{}', it will be removed.", ruleByEventType.get().getClass(), screenshotType);
            RULES.remove(ruleByEventType.get());
        }
        LOGGER.debug("Actual range of screenshot rules: {}", RULES);
        return RULES;
    }

    /**
     * Clear all rules and disable all kind of screenshots, even for failures!
     */
    public static synchronized void clearRules() {
        LOGGER.warn("All screenshot capture rules will be deleted. Automatic capturing disabled even for failures!");
        RULES.clear();
    }

    /**
     * Captures visible screenshot explicitly by any rule, creates thumbnail and copies both images to specified screenshots
     * location.
     *
     * @param driver
     *            instance used for capturing.
     * @param comment String
     * @return screenshot name.
     * @deprecated use {@link #capture(WebDriver, ScreenshotType, String)} instead
     */
    @Deprecated(forRemoval = true, since = "8.0.5")
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
     * @deprecated use {@link #capture(WebDriver, ScreenshotType, String)} instead
     */
    @Deprecated(forRemoval = true, since = "8.0.5")
    public static String captureByRule(WebDriver driver, String comment, boolean isFullSize) {
        boolean isTakeScreenshotRules = false;
        for (IScreenshotRule iScreenshotRule : RULES) {
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
     * @param driver instance used for capturing.
     * @param comment String
     * @return screenshot name.
     * @deprecated use {@link #capture(WebDriver, IScreenshotRule, String)} instead, or {@link #capture(WebDriver, ScreenshotType, String)}
     *             with {@link ScreenshotType#EXPLICIT_VISIBLE}
     */
    @Deprecated(forRemoval = true, since = "8.0.5")
    public static String capture(WebDriver driver, String comment) {
        return capture(driver, comment, false);
    }

    /**
     * Captures application screenshot, creates thumbnail and copies both images to specified screenshots location.
     *
     * @param driver {@link WebDriver}
     * @param comment screenshot comment
     * @param isFullSize
     *            Boolean
     * @return screenshot name.
     * @deprecated use {@link #capture(WebDriver, IScreenshotRule, String)} instead, or {@link #capture(WebDriver, ScreenshotType, String)}
     *             with {@link ScreenshotType#EXPLICIT_VISIBLE} or {@link ScreenshotType#EXPLICIT_FULL_SIZE}
     */
    @Deprecated(forRemoval = true, since = "8.0.5")
    public static String capture(WebDriver driver, String comment, boolean isFullSize) {
        return capture(driver, true, comment, isFullSize);
    }

    /**
     * Capture screenshot by rule.
     * Search rule in pool by specified {@link ScreenshotType}, and if there is one, a screenshot is taken on it.
     * 
     * @param driver {@link WebDriver}.
     * @param screenshotType {@link ScreenshotType}.
     * @return {@link Optional} with name of screenshot file (file name with extension) if captured and successfully saved, {@link Optional#empty()}
     *         otherwise.
     */
    public static Optional<String> capture(WebDriver driver, ScreenshotType screenshotType) {
        return capture(driver, screenshotType, "");
    }

    /**
     * Capture screenshot by rule.
     * Search rule in pool by specified {@link ScreenshotType}, and if there is one, a screenshot is taken on it.
     *
     * @param driver {@link WebDriver}.
     * @param screenshotType {@link ScreenshotType}.
     * @param comment screenshot comment
     * @return {@link Optional} with name of screenshot file (file name with extension) if captured and successfully saved, {@link Optional#empty()}
     *         otherwise.
     */
    public static Optional<String> capture(WebDriver driver, ScreenshotType screenshotType, String comment) {
        IScreenshotRule rule = null;
        synchronized (RULES) {
            rule = RULES.stream()
                    .filter(r -> screenshotType.equals(r.getEventType()))
                    .findFirst()
                    .orElse(null);
        }
        return rule != null ? capture(driver, rule, comment) : Optional.empty();
    }

    /**
     * Capture screenshot by concrete rule (ignoring rules in pool).
     *
     * @param driver {@link WebDriver}
     * @param comment screenshot comment
     * @return {@link Optional} with name of screenshot file (file name with extension) if captured and successfully saved, {@link Optional#empty()}
     *         otherwise.
     */
    public static Optional<String> capture(WebDriver driver, IScreenshotRule rule, String comment) {
        Objects.requireNonNull(rule, "screenshot rule param must not be null");
        Objects.requireNonNull(rule, "comment to screenshot must not be null");

        if (!rule.isTakeScreenshot()) {
            return Optional.empty();
        }

        if (rule.isEnableValidation() && !isRuleValid(rule)) {
            return Optional.empty();
        }

        if (!isCaptured(comment)) {
            // [VD] do not write something to log as this original exception is used as original exception for failure
            // invalid driver for screenshot capture
            return Optional.empty();
        }

        WebDriver originalDriver = castDriver(driver);

        BufferedImage screenshot = null;
        LOGGER.debug("Screenshot->capture starting...");
        try {
            setPageLoadTimeout(originalDriver, rule.getTimeout());
            Wait<WebDriver> wait = new FluentWait<>(driver)
                    .pollingEvery(rule.getTimeout())
                    .withTimeout(rule.getTimeout());
            if (rule.isAllowFullSize()) {
                LOGGER.debug("starting full size screenshot capturing...");
                screenshot = takeFullScreenshot(driver, wait);
            } else {
                LOGGER.debug("starting visible screenshot capturing...");
                screenshot = takeVisibleScreenshot(wait);
            }
        } catch (TimeoutException e) {
            LOGGER.warn("Unable to capture screenshot during {} sec!", rule.getTimeout().toSeconds());
        } catch (Exception e) {
            // for undefined failure keep full stacktrace to handle later correctly!
            LOGGER.error("Undefined error on capture screenshot detected!", e);
        } finally {
            setPageLoadTimeout(originalDriver, getDefaultPageLoadTimeout());
            LOGGER.debug("finished screenshot call.");
        }

        if (screenshot == null) {
            return Optional.empty();
        }

        String screenshotFileName = null;
        try {
            Pair<Integer, Integer> dimensions = rule.getImageResizeDimensions();
            screenshot = resizeImg(screenshot, dimensions.getLeft(), dimensions.getRight());

            String fileName = rule.getFilename() + "." + rule.getFileExtension();
            Path file = Path.of(rule.getSaveFolder().toString(), fileName)
                    .toAbsolutePath();

            ImageIO.write(screenshot, rule.getFileExtension(), file.toFile());

            screenshotFileName = fileName;

            if (!comment.isEmpty()) {
                LOGGER.info(comment);
                // add screenshot comment to collector
                ReportContext.addScreenshotComment(screenshotFileName, comment);
            }
            // upload screenshot to Zebrunner Reporting
            com.zebrunner.agent.core.registrar.Screenshot.upload(Files.readAllBytes(file), Instant.now().toEpochMilli());
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
        return Optional.ofNullable(screenshotFileName);
    }

    /**
     * Check is rule valid
     * 
     * @param rule {@link IScreenshotRule}
     * @return true if screenshot rule valid, false otherwise
     */
    private static boolean isRuleValid(IScreenshotRule rule) {
        boolean isValid = true;
        if (rule.getFilename() == null || rule.getFilename().isEmpty()) {
            LOGGER.error("Screenshot rule '{}' filename must not be null or empty.", rule.getClass());
            isValid = false;
        }
        if (StringUtils.isBlank(rule.getFileExtension())) {
            LOGGER.error("Screenshot rule '{}' file extension must not be null or empty.", rule.getClass());
            isValid = false;
        }

        if (rule.getTimeout() == null || rule.getTimeout().toSeconds() < 0) {
            LOGGER.error("Screenshot rule '{}' timeout must not be null or less than 0 seconds.", rule.getClass());
            isValid = false;
        }

        if (rule.getEventType() == null) {
            LOGGER.error("Screenshot rule '{}' event type must not be null.", rule.getClass());
            isValid = false;
        }

        if (rule.getImageResizeDimensions() == null ||
                rule.getImageResizeDimensions().getLeft() == null ||
                rule.getImageResizeDimensions().getRight() == null) {
            LOGGER.error("Screenshot rule '{}' event type must not be null.", rule.getClass());
        }

        if (rule.getSaveFolder() == null || !Files.isDirectory(rule.getSaveFolder())) {
            LOGGER.error("Screenshot rule '{}' save folder is null or is not a folder", rule.getClass());
        }

        return isValid;
    }

    /**
     * Captures screenshot explicitly ignoring any rules, creates thumbnail and copies both images to specified screenshots
     * location.
     *
     * @param driver instance used for capturing.
     * @param comment String
     * @param fullSize boolean
     * @return screenshot name.
     * @deprecated use {@link #capture(WebDriver, ScreenshotType, String)} or {@link #capture(WebDriver, IScreenshotRule, String)}
     */
    @Deprecated(forRemoval = true, since = "8.0.5")
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
                    // LOGGER.debug("Unable to capture screenshot as driver seems invalid: " + comment);
                    return screenName;
                }
                // Define test screenshot root
                File testScreenRootDir = ReportContext.getTestDir();

                // Capture full page screenshot and resize
                screenName = System.currentTimeMillis() + ".png";
                String screenPath = testScreenRootDir.getAbsolutePath() + "/" + screenName;

                // hotfix to converting proxy into the valid driver
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
                        // do nothing and receive augmenting warning in the logs
                    }
                }

                int divider = fullSize ? 2 : 3;
                Duration timeout = Duration.ofSeconds(Configuration.getInt(Configuration.Parameter.EXPLICIT_TIMEOUT) / divider);
                BufferedImage screen = null;
                try {
                    setPageLoadTimeout(driver, timeout);
                    Wait<WebDriver> wait = new FluentWait<>(driver)
                            .pollingEvery(timeout)
                            .withTimeout(timeout);
                    // Create screenshot
                    if (fullSize) {
                        screen = takeFullScreenshot(driver, wait);
                    } else {
                        screen = takeVisibleScreenshot(wait);
                    }
                } catch (TimeoutException e) {
                    LOGGER.warn("Unable to capture screenshot during {} sec!", timeout.toSeconds());
                } catch (Exception e) {
                    // for undefined failure keep full stacktrace to handle later correctly!
                    LOGGER.error("Undefined error on capture screenshot detected!", e);
                } finally {
                    setPageLoadTimeout(driver, getDefaultPageLoadTimeout());
                    LOGGER.debug("finished screenshot call.");
                }

                if (screen == null) {
                    // do nothing and return empty
                    return "";
                }

                if (Configuration.getInt(Parameter.BIG_SCREEN_WIDTH) != -1
                        && Configuration.getInt(Parameter.BIG_SCREEN_HEIGHT) != -1) {
                    screen = resizeImg(screen, Configuration.getInt(Parameter.BIG_SCREEN_WIDTH),
                            Configuration.getInt(Parameter.BIG_SCREEN_HEIGHT));
                }

                File screenshot = new File(screenPath);

                ImageIO.write(screen, "PNG", screenshot);

                if (!comment.isEmpty()) {
                    LOGGER.info(comment);
                    // add screenshot comment to collector
                    ReportContext.addScreenshotComment(screenName, comment);
                }
                // upload screenshot to Zebrunner Reporting
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
     * @param bufferedImage image to resize.
     * @param width the target width that you wish the image to have.
     * @param height the target height that you wish the image to have.
     */
    private static BufferedImage resizeImg(BufferedImage bufferedImage, Integer width, Integer height) {
        Objects.requireNonNull(bufferedImage, "bufferedImage parameter must not be null");
        if (width == null || width < 0 || height == null || height < 0) {
            return bufferedImage;
        }

        BufferedImage resizedImage = null;
        try {
            resizedImage = Scalr.resize(bufferedImage, Scalr.Method.BALANCED, Scalr.Mode.FIT_TO_WIDTH, width, height,
                    Scalr.OP_ANTIALIAS);
            if (resizedImage.getHeight() > height) {
                resizedImage = Scalr.crop(resizedImage, resizedImage.getWidth(), height);
            }
        } catch (Exception e) {
            LOGGER.error("Image scaling problem!", e);
            resizedImage = bufferedImage;
        }
        return resizedImage;
    }

    /**
     * Makes full size screenshot using javascript (May not work properly with
     * popups and active js-elements on the page)
     *
     * @param driver web driver without listeners (original)
     * @exception IOException see {@link ImageIO#read(File)}
     *
     * @return a {@link BufferedImage} of screenshot if it was produced successfully, or null otherwise
     */
    private static BufferedImage takeFullScreenshot(WebDriver driver, Wait<WebDriver> wait) throws IOException {
        BufferedImage screenshot;

        if (driver instanceof AppiumDriver) {
            File capturedScreenshot = wait.until(drv -> ((TakesScreenshot) drv)
                    .getScreenshotAs(OutputType.FILE));
            screenshot = ImageIO.read(capturedScreenshot);
        } else {
            final AShot ashot;
            // if for mobile we use RemoteWebDriver
            if (Configuration.getDriverType().equals(SpecialKeywords.MOBILE)) {
                if (Configuration.getPlatform().equals("ANDROID")) {
                    String pixelRatio = String.valueOf(((HasCapabilities) driver).getCapabilities().getCapability("pixelRatio"));
                    float dpr = !pixelRatio.equals("null") ? Float.parseFloat(pixelRatio) : SpecialKeywords.DEFAULT_DPR;
                    ashot = new AShot().shootingStrategy(ShootingStrategies
                            .viewportRetina(SpecialKeywords.DEFAULT_SCROLL_TIMEOUT, SpecialKeywords.DEFAULT_BLOCK, SpecialKeywords.DEFAULT_BLOCK,
                                    dpr));
                } else {
                    int deviceWidth = driver.manage().window().getSize().getWidth();
                    String deviceName = String.valueOf(((HasCapabilities) driver).getCapabilities()
                            .getCapability(MobileCapabilityType.DEVICE_NAME));
                    ashot = new AShot().shootingStrategy(getScreenshotShuttingStrategy(deviceWidth, deviceName));
                }
            } else {
                // regular web
                ashot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(SpecialKeywords.DEFAULT_SCROLL_TIMEOUT));
            }
            screenshot = wait.until(
                    ashot::takeScreenshot)
                    .getImage();
        }
        return screenshot;
    }

    /**
     * Take screenshot of visible part of the page.
     *
     * @exception IOException see {@link ImageIO#read(File)}
     *
     * @return a {@link BufferedImage} of screenshot if it was produced successfully, or null otherwise
     */
    private static BufferedImage takeVisibleScreenshot(Wait<WebDriver> wait) throws IOException {
            File capturedScreenshot = wait.until(drv -> ((TakesScreenshot) drv)
                    .getScreenshotAs(OutputType.FILE));
            return ImageIO.read(capturedScreenshot);
    }

    /**
     * Analyze if screenshot can be captured using the most common reason when
     * driver is died etc.
     *
     * @param message error message (stacktrace).
     * @return true if screenshot already captured (or cannot be captured), false otherwise
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
     * Compares two different screenshots.
     * Creates an image with marked differences if the images differ.
     *
     * @param bufferedImageExpected old image
     * @param bufferedImageActual new image
     * @param fileNameDiffImage name of the new image with marked differences
     * @param artifact true if attach to test run as artifact, false if upload as screenshot
     * @param markupPolicy {@link DiffMarkupPolicy}
     * @return boolean
     */
    public static boolean compare(BufferedImage bufferedImageExpected, BufferedImage bufferedImageActual,
            String fileNameDiffImage, boolean artifact, DiffMarkupPolicy markupPolicy) {
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

                screenName = fileNameDiffImage + ".png";
                String screenPath = testScreenRootDir.getAbsolutePath() + "/" + screenName;

                if (Configuration.getInt(Parameter.BIG_SCREEN_WIDTH) != -1
                        && Configuration.getInt(Parameter.BIG_SCREEN_HEIGHT) != -1) {
                    screen = resizeImg(screen, Configuration.getInt(Parameter.BIG_SCREEN_WIDTH),
                            Configuration.getInt(Parameter.BIG_SCREEN_HEIGHT));
                }

                File screenshot = new File(screenPath);
                FileUtils.touch(screenshot);
                ImageIO.write(screen, "PNG", screenshot);

                // Uploading comparative screenshot to Amazon S3
                if (artifact){
                    com.zebrunner.agent.core.registrar.Artifact.attachToTest(fileNameDiffImage + ".png", screenshot);
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
     * Remove all driver extra listeners (use it in problematic places where you handle all exceptions)
     *
     * @param driver {@link WebDriver}
     *
     * @return {@link WebDriver} without driver listeners
     */
    private static WebDriver castDriver(WebDriver driver) {
        if (driver instanceof Decorated<?>) {
            return (WebDriver) ((Decorated<?>) driver).getOriginal();
        }
        return driver;
    }

    private static void setPageLoadTimeout(WebDriver drv, Duration timeout) {
        try {
            drv.manage().timeouts().pageLoadTimeout(timeout);
        } catch (UnsupportedCommandException e) {
            //TODO: review upcoming appium 2.0 changes
            LOGGER.debug("Appium: Not implemented yet for pageLoad timeout!");
        }
    }

    private static Duration getDefaultPageLoadTimeout() {
        return DEFAULT_PAGE_LOAD_TIMEOUT;
        // #1705: limit pageLoadTimeout driver timeout by idleTimeout
//      if (!R.CONFIG.get("capabilities.idleTimeout").isEmpty()) {
//          long idleTimeout = R.CONFIG.getLong("capabilities.idleTimeout");
//          if (idleTimeout < timeout) {
//              timeout = idleTimeout;
//          }
//      }
    }
}
