/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation.webdriver.listener;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.FileManager;
import com.qaprosoft.carina.core.foundation.webdriver.CarinaDriver;
import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.Screenshot;
import com.zebrunner.agent.core.registrar.Artifact;

/**
 * ScreenshotEventListener - captures screenshot after essential webdriver event.
 * IMPORTANT! Please avoid any driver calls with extra listeners (recursive exception generation)
 *
 * @author Alex Khursevich (alex@qaprosoft.com)
 */
public class DriverListener implements WebDriverEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final static ThreadLocal<String> currentPositiveMessage = new ThreadLocal<String>();
    private final static ThreadLocal<String> currentNegativeMessage = new ThreadLocal<String>();

    @Override
    public void afterAlertAccept(WebDriver driver) {
        onAfterAction("Alert accepted", driver);
    }

    @Override
    public void afterAlertDismiss(WebDriver driver) {
        onAfterAction("Alert dismissed", driver);
    }

    @Override
    public void afterChangeValueOf(WebElement element, WebDriver driver, CharSequence[] value) {
        String comment = String.format("Text '%s' typed", charArrayToString(value));
        captureScreenshot(comment, driver, element, false);
    }

    @Override
    public void afterClickOn(WebElement element, WebDriver driver) {
        String comment = "Element clicked";
        captureScreenshot(comment, driver, element, false);
    }

    @Override
    public void afterFindBy(By by, WebElement element, WebDriver driver) {
        // Do nothing
    }

    @Override
    public void afterNavigateBack(WebDriver driver) {
        onAfterAction("Navigated back", driver);
    }

    @Override
    public void afterNavigateForward(WebDriver driver) {
        onAfterAction("Navigated forward", driver);
    }

    @Override
    public void afterNavigateRefresh(WebDriver driver) {
        onAfterAction("Page refreshed", driver);
    }

    @Override
    public void afterNavigateTo(String url, WebDriver driver) {
        String comment = String.format("URL '%s' opened", url);
        onAfterAction(comment, driver);
    }

    @Override
    public void afterScript(String script, WebDriver driver) {
        // Do nothing
    }

    @Override
    public void beforeAlertAccept(WebDriver driver) {
        // Do nothing
    }

    @Override
    public void beforeAlertDismiss(WebDriver driver) {
        // Do nothing
    }

    @Override
    public void beforeChangeValueOf(WebElement element, WebDriver driver, CharSequence[] value) {
        // Do nothing
    }

    @Override
    public void beforeClickOn(WebElement element, WebDriver driver) {
        // Do nothing
    }

    @Override
    public void beforeFindBy(By by, WebElement element, WebDriver driver) {
        // Do nothing
    }

    @Override
    public void beforeNavigateBack(WebDriver driver) {
        // Do nothing
    }

    @Override
    public void beforeNavigateForward(WebDriver driver) {
        // Do nothing
    }

    @Override
    public void beforeNavigateRefresh(WebDriver driver) {
        // Do nothing
    }

    @Override
    public void beforeNavigateTo(String script, WebDriver driver) {
        // Do nothing
    }

    @Override
    public void beforeScript(String script, WebDriver driver) {
        // Do nothing
    }

    @Override
    public void onException(Throwable thr, WebDriver driver) {
        if (thr == null || thr.getMessage() == null) {
            // do nothing
            return;
        }
        CarinaDriver carinaDriver = null;
        try {
            driver = castDriver(driver);
            carinaDriver = IDriverPool.getCarinaDriver(((RemoteWebDriver) driver).getSessionId());
        } catch (Exception ex){
            ex.printStackTrace();
        }
        String message = thr.getMessage();

        // handle use-case when application crashed on iOS but tests continue to execute something because doesn't raise valid exception
        // Example:

        // 10:25:20 2018-09-14 10:29:39 DriverListener [TestNG-31] [ERROR]
        // [iPhone_6s] An unknown server-side error occurred while
        // processing the command. Original error: The application under
        // test with bundle id 'Q5AWL8WCY6' is not running,
        // possibly crashed (WARNING: The server did not provide any
        // stacktrace information)

        // TODO: investigate if we run @AfterMethod etc system events after this crash
        if (message.contains("is not running, possibly crashed")) {
            throw new RuntimeException(thr);
        }

        if (message.contains("Session ID is null. Using WebDriver after calling quit")
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
                || message.contains("no such window: window was already closed")
                || message.contains("Error communicating with the remote browser. It may have died")
                || message.contains("chrome not reachable")
                || message.contains("cannot forward the request Connect to")
                || message.contains("Could not proxy command to remote server. Original error:") // Error: socket hang up, Error: read ECONNRESET etc
                || message.contains("Could not proxy command to the remote server. Original error:") // Different messages on some Appium versions
                || message.contains("Driver connection refused")) {
            // mark driver as not alive anymore!
            LOGGER.error("Mark current driver as died!");

            carinaDriver.setAlive(false);
            return;
        }

        // [VD] make below code as much safety as possible otherwise potential recursive failure could occur with driver related issue.
        // most suspicious are capture screenshots, generating dumps etc
        if (message.contains("Method has not yet been implemented")
                || message.contains("Expected to read a START_MAP but instead have: END. Last 0 characters read")
                || message.contains("Unable to determine type from: <. Last 1 characters read")
                || message.contains("script timeout")
                || message.contains("javascript error: Cannot read property 'outerHTML' of null")
                || message.contains("javascript error: Cannot read property 'scrollHeight' of null")
                || message.contains("Method is not implemented")
                || message.contains("An element could not be located on the page using the given search parameters")
                || message.contains("no such element: Unable to locate element")
                // carina has a lot of extra verifications to solve all stale reference issue and finally perform an action so ignore such exception in listener!
                || message.contains("StaleElementReferenceException")
                || message.contains("stale_element_reference.html")) {
            // do nothing
            return;
        }

        // hopefully castDriver below resolve root cause of the recursive onException calls but keep below to ensure
        if (thr.getStackTrace() != null
                && (Arrays.toString(thr.getStackTrace())
                        .contains("com.qaprosoft.carina.core.foundation.webdriver.listener.DriverListener.onException")
                        || Arrays.toString(thr.getStackTrace()).contains("Unable to capture screenshot due to the WebDriverException"))) {
            LOGGER.warn("Do not generate recursive screenshots!");
            // prevent recursive crash for onException
            return;
        }

        LOGGER.debug("DriverListener->onException starting..." + message);
        driver = castDriver(driver);

        try {
            // 1. if you see mess with afterTest carina actions and Timer startup failure you should follow steps #2+ to determine root cause.
            // Driver initialization 'default' FAILED! Retry 1 of 1 time - Operation already started: mobile_driverdefault
            // 2. carefully track all preliminary exception for the same thread to detect 1st problematic exception
            // 3. 99% those root exception means that we should prohibit screenshot generation for such use-case
            // 4. if 3rd one is true just update Screenshot.isCaptured() adding part of the exception to the list
            // handle cases which should't be captured
            if (carinaDriver.isAlive() && Screenshot.isCaptured(message)) {
                captureScreenshot(message, driver, null, true);
            }
        } catch (Exception e) {
            if (!e.getMessage().isEmpty()
                    && (e.getMessage().contains("Method has not yet been implemented") || (e.getMessage().contains("Method is not implemented")))) {
                LOGGER.debug("Unrecognized exception detected in DriverListener->onException! " + e.getMessage(), e);
            } else {
                LOGGER.error("Unrecognized exception detected in DriverListener->onException! " + e.getMessage(), e);
            }
        } catch (Throwable e) {
            LOGGER.error("Take a look to the logs above for current thread and exclude particular exception for Screenshot.isCaptured(). "
                    + e.getMessage(), e);
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

    @Override
    public void afterSwitchToWindow(String arg0, WebDriver driver) {
        // do nothing

    }

    @Override
    public void beforeSwitchToWindow(String arg0, WebDriver driver) {
        // Do nothing
    }

    @Override
    public <X> void afterGetScreenshotAs(OutputType<X> arg0, X arg1) {
        // do nothing
    }

    @Override
    public <X> void beforeGetScreenshotAs(OutputType<X> arg0) {
        // Do nothing
    }

    @Override
    public void afterGetText(WebElement element, WebDriver driver, String arg2) {
        // do nothing
    }

    @Override
    public void beforeGetText(WebElement element, WebDriver driver) {
        // do nothing
    }

    private void captureScreenshot(String comment, WebDriver driver, WebElement element, boolean errorMessage) {
        driver = castDriver(driver);
        if (getMessage(errorMessage) != null) {
            comment = getMessage(errorMessage);
        }

        try {
            if (errorMessage) {
                LOGGER.error(comment);
                if (Screenshot.isEnabled()) {
                    String screenName = Screenshot.capture(driver, comment, true); // in case of failure
                    // do not generate UI dump if no screenshot
                    if (!screenName.isEmpty()) {
                        generateDump(screenName);
                    }
                }
            } else {
                LOGGER.info(comment);
                Screenshot.captureByRule(driver, comment);
            }
        } catch (Exception e) {
            LOGGER.debug("Unrecognized failure detected in DriverListener->captureScreenshot: " + e.getMessage(), e);
        } finally {
            resetMessages();
        }
    }

    private void generateDump(String screenName) {
        // XML layout extraction
        File uiDumpFile = IDriverPool.getDefaultDevice().generateUiDump(screenName);
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
            return currentNegativeMessage.get();
        } else {
            return currentPositiveMessage.get();
        }
    }

    public static void setMessages(String positiveMessage, String negativeMessage) {
        currentPositiveMessage.set(positiveMessage);
        currentNegativeMessage.set(negativeMessage);
    }

    private void resetMessages() {
        currentPositiveMessage.remove();
        currentNegativeMessage.remove();
    }

    /**
     * Cast Carina driver to WebDriver removing all extra listeners (try to avoid direct operations via WebDriver as it doesn't support logging etc)
     *
     * @param drv WebDriver
     *
     * @return WebDriver
     */
    private WebDriver castDriver(WebDriver drv) {
        if (drv instanceof EventFiringWebDriver) {
            drv = ((EventFiringWebDriver) drv).getWrappedDriver();
        } else if (drv instanceof Proxy){
            InvocationHandler h = Proxy.getInvocationHandler((Proxy) drv);
            Field[] fields = h.getClass().getDeclaredFields();
            fields[0].setAccessible(true);
            EventFiringWebDriver eventFiringWebDriver;
            try {
                eventFiringWebDriver = ((EventFiringWebDriver) fields[0].get(h));
                drv = eventFiringWebDriver.getWrappedDriver();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return drv;
    }

}