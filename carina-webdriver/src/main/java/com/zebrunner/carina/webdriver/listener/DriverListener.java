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
package com.zebrunner.carina.webdriver.listener;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.decorators.Decorated;
import org.openqa.selenium.support.events.WebDriverListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zebrunner.agent.core.registrar.Artifact;
import com.zebrunner.carina.utils.FileManager;
import com.zebrunner.carina.utils.report.ReportContext;
import com.zebrunner.carina.webdriver.IDriverPool;
import com.zebrunner.carina.webdriver.Screenshot;
import com.zebrunner.carina.webdriver.ScreenshotType;

/**
 * Default driver listener<br>
 * If you want to create your own driver event listener, it must necessarily implement the
 * {@link WebDriverListener} interface and it must have a default constructor,
 * or if you need a driver in methods where the driver is not passed as a parameter,
 * then add a constructor that takes a single parameter of the {@link WebDriver} type.<br>
 * DriverListener - captures screenshot after essential webdriver event.
 * IMPORTANT! Please avoid any driver calls with extra listeners (recursive exception generation)
 * 
 * @author Alex Khursevich (alex@qaprosoft.com)
 */
public class DriverListener implements WebDriverListener, IDriverPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final ThreadLocal<String> CURRENT_POSITIVE_MESSAGE = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_NEGATIVE_MESSAGE = new ThreadLocal<>();

    private WebDriver driver = null;

    // FIXME refactor - it is not a good idea to set driver using constructor
    public DriverListener(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public void afterAccept(Alert alert) {
        onAfterAction("Alert accepted", this.driver);
    }

    @Override
    public void afterDismiss(Alert alert) {
        onAfterAction("Alert dismissed", this.driver);

    }

    @Override
    public void afterSendKeys(WebElement element, CharSequence... keysToSend) {
        captureScreenshot(String.format("Text '%s' typed", charArrayToString(keysToSend)), this.driver, element, false);
    }

    @Override
    public void afterClick(WebElement element) {
        String comment = "Element clicked";
        captureScreenshot(comment, this.driver, element, false);
    }

    @Override
    public void afterBack(WebDriver.Navigation navigation) {
        onAfterAction("Navigated back", this.driver);
    }

    @Override
    public void afterForward(WebDriver.Navigation navigation) {
        onAfterAction("Navigated forward", this.driver);

    }

    @Override
    public void afterRefresh(WebDriver.Navigation navigation) {
        onAfterAction("Page refreshed", this.driver);
    }

    @Override
    public void afterTo(WebDriver.Navigation navigation, String url) {
        String comment = String.format("URL '%s' opened", url);
        onAfterAction(comment, this.driver);

    }

    // TODO investigate this method is too complex, and it contails old logic. Can we simplify it?
    @Override
    public void onError(Object target, Method method, Object[] args, InvocationTargetException e) {

        // [VD] make below code as much safety as possible otherwise potential recursive failure could occur with driver related issue.
        // most suspicious are capture screenshots, generating dumps etc
        if (e == null
                || e.getMessage() == null
                || e.getMessage().contains("Method has not yet been implemented")
                || e.getMessage().contains("Expected to read a START_MAP but instead have: END. Last 0 characters read")
                || e.getMessage().contains("Unable to determine type from: <. Last 1 characters read")
                || e.getMessage().contains("script timeout")
                || e.getMessage().contains("javascript error: Cannot read property 'outerHTML' of null")
                || e.getMessage().contains("javascript error: Cannot read property 'scrollHeight' of null")
                || e.getMessage().contains("Method is not implemented")
                || e.getMessage().contains("An element could not be located on the page using the given search parameters")
                || e.getMessage().contains("no such element: Unable to locate element")
                || e.getMessage().contains("Failed to execute command screen image")
                // carina has a lot of extra verifications to solve all stale reference issue and finally perform an action so ignore such exception
                // in listener!
                || e.getMessage().contains("StaleElementReferenceException")
                || e.getMessage().contains("stale_element_reference.html")) {
            // do nothing
            return;
        }

        // handle use-case when application crashed on iOS but tests continue to execute something because doesn't raise valid exception
        // Example:

        // 10:25:20 2018-09-14 10:29:39 DriverListener [TestNG-31] [ERROR]
        // [iPhone_6s] An unknown server-side error occurred while
        // processing the command. Original error: The application under
        // test with bundle id 'Q5AWL8WCY6' is not running,
        // possibly crashed (WARNING: The server did not provide any
        // stacktrace information)

        // TODO: investigate if we run @AfterMethod etc system events after this crash
        if (e.getMessage().contains("is not running, possibly crashed")) {
            throw new RuntimeException(e);
        }

        // hopefully castDriver below resolve root cause of the recursive onException calls but keep below to ensure
        if (e.getStackTrace() != null
                && (Arrays.toString(e.getStackTrace())
                        .contains("com.qaprosoft.carina.core.foundation.webdriver.listener.DriverListener.onException")
                        || Arrays.toString(e.getStackTrace()).contains("Unable to capture screenshot due to the WebDriverException"))) {
            LOGGER.error("Do not generate screenshot for invalid driver!");
            // prevent recursive crash for onException
            return;
        }

        LOGGER.debug("DriverListener->onException starting..." + e.getMessage());
        //this.driver = castDriver(this.driver);

        try {
            // 1. if you see mess with afterTest carina actions and Timer startup failure you should follow steps #2+ to determine root cause.
            // Driver initialization 'default' FAILED! Retry 1 of 1 time - Operation already started: mobile_driverdefault
            // 2. carefully track all preliminary exception for the same thread to detect 1st problematic exception
            // 3. 99% those root exception means that we should prohibit screenshot generation for such use-case
            // 4. if 3rd one is true just update Screenshot.isCaptured() adding part of the exception to the list
            // handle cases which should't be captured
            if (Screenshot.isCaptured(e.getMessage())) {
                captureScreenshot(e.getMessage(), driver, null, true);
            }
        } catch (Exception err) {
            if (!err.getMessage().isEmpty()
                    && (err.getMessage().contains("Method has not yet been implemented")
                            || (err.getMessage().contains("Method is not implemented")))) {
                LOGGER.debug("Unrecognized exception detected in DriverListener->onException!", err);
            } else {
                LOGGER.error("Unrecognized exception detected in DriverListener->onException!", err);
            }
        } catch (Throwable err) {
            LOGGER.error("Take a look to the logs above for current thread and add exception into the exclusion for Screenshot.isCaptured().", err);
        }

        LOGGER.debug("DriverListener->onException finished.");
    }

    /**
     * Converts char sequence to string.
     * 
     * @param csa - char sequence array
     * @return string representation
     */
    private String charArrayToString(CharSequence[] csa) {
        String s = StringUtils.EMPTY;
        if (csa != null) {
            StringBuilder sb = new StringBuilder();
            for (CharSequence cs : csa) {
                sb.append(String.valueOf(cs));
            }
            s = sb.toString();
        }
        return s;
    }

    private void captureScreenshot(String comment, WebDriver driver, WebElement element, boolean errorMessage) {
        if (getMessage(errorMessage) != null) {
            comment = getMessage(errorMessage);
        }

        try {
            if (errorMessage) {
                LOGGER.error(comment);
                // in case of failure try full size if allowed
                // do not generate UI dump if no screenshot
                Screenshot.capture(driver, ScreenshotType.UNSUCCESSFUL_DRIVER_ACTION)
                        .ifPresent(s -> generateDump(driver, s));
            } else {
                LOGGER.info(comment);
                Screenshot.capture(driver, ScreenshotType.SUCCESSFUL_DRIVER_ACTION);
            }
        } catch (Exception e) {
            LOGGER.debug("Unrecognized failure detected in DriverListener->captureScreenshot!", e);
        } finally {
            resetMessages();
        }
    }

    private void generateDump(WebDriver driver, String screenName) {
        // XML layout extraction
        File uiDumpFile = getDevice(driver).generateUiDump(screenName);
        if (uiDumpFile != null) {
            // use the same naming but with zip extension. Put into the test artifacts folder
            String dumpArtifact = ReportContext.getArtifactsFolder().getAbsolutePath() + "/" + screenName.replace(".png", ".zip");
            LOGGER.debug("UI Dump artifact: " + dumpArtifact);

            // build path to screenshot using name
            File screenFile = new File(ReportContext.getTestDir().getAbsolutePath() + "/" + screenName);

            // archive page source dump and screenshot both together
            FileManager.zipFiles(dumpArtifact, uiDumpFile, screenFile);

            Artifact.attachToTest("UI Dump artifact", new File(dumpArtifact));
        } else {
            LOGGER.debug("Dump file is empty.");
        }
    }

    private void onAfterAction(String comment, WebDriver driver) {
        captureScreenshot(comment, driver, null, false);
    }

    public static String getMessage(boolean errorMessage) {
        if (errorMessage) {
            return CURRENT_NEGATIVE_MESSAGE.get();
        } else {
            return CURRENT_POSITIVE_MESSAGE.get();
        }
    }

    public static void setMessages(String positiveMessage, String negativeMessage) {
        CURRENT_POSITIVE_MESSAGE.set(positiveMessage);
        CURRENT_NEGATIVE_MESSAGE.set(negativeMessage);
    }

    private void resetMessages() {
        CURRENT_POSITIVE_MESSAGE.remove();
        CURRENT_NEGATIVE_MESSAGE.remove();
    }

    /**
     * Cast Carina driver to WebDriver removing all extra listeners (try to avoid direct operations via WebDriver as it doesn't support logging etc)
     *
     * @param drv WebDriver
     *
     * @return WebDriver
     */
//    private WebDriver castDriver(WebDriver drv) {
//        if (drv instanceof Decorated) {
//            drv = ((Decorated<WebDriver>) drv).getOriginal();
//        }
//        return drv;
//    }

    /**
     * Clean driver from Decorator and cast driver to
     *
     * @param <T> extends WebDriver
     * @param driver WebDriver
     * @param clazz  class to which to cast
     * @return driver without listeners
     */
    public static <T extends WebDriver> T castDriver(WebDriver driver, Class<T> clazz) {
        T castDriver = null;
        if (driver instanceof Decorated) {
            driver = ((Decorated<WebDriver>) driver).getOriginal();
        }
        castDriver = clazz.cast(driver);
        return castDriver;
    }
}
