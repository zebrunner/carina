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
package com.qaprosoft.carina.core.foundation.webdriver.listener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.qaprosoft.carina.core.foundation.webdriver.Screenshot;
import com.qaprosoft.zafira.client.ZafiraSingleton;
import com.qaprosoft.zafira.models.dto.TestArtifactType;

/**
 * ScreenshotEventListener - captures screenshot after essential webdriver event.
 * 
 * @author Alex Khursevich (alex@qaprosoft.com)
 */
public class DriverListener implements WebDriverEventListener {
	// 1. register live vnc url in DriverFactory (method streamVNC should return valid TestArtifactType
	// 2. DriverFactory->getEventListeners(TestArtifactType vncArtifact)
	// 3. declare vncArtifact using constructor in DriverListener
	// 4. onBefore any action try to register vncArtifact in Zafira. Detailed use-cases find in onBeforeAction method 
	protected TestArtifactType vncArtifact;
	
	public DriverListener(TestArtifactType vncArtifact) {
		this.vncArtifact = vncArtifact;
	}
	
    private static final Logger LOGGER = Logger.getLogger(DriverListener.class);

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
    	onBeforeAction();
    }

    @Override
    public void beforeAlertDismiss(WebDriver driver) {
    	onBeforeAction();
    }

    @Override
    public void beforeChangeValueOf(WebElement element, WebDriver driver, CharSequence[] value) {
    	onBeforeAction();
    }

    @Override
    public void beforeClickOn(WebElement element, WebDriver driver) {
    	onBeforeAction();
    }

    @Override
    public void beforeFindBy(By by, WebElement element, WebDriver driver) {
    	onBeforeAction();
    }

    @Override
    public void beforeNavigateBack(WebDriver driver) {
    	onBeforeAction();
    }

    @Override
    public void beforeNavigateForward(WebDriver driver) {
    	onBeforeAction();
    }

    @Override
    public void beforeNavigateRefresh(WebDriver driver) {
    	onBeforeAction();
    }

    @Override
    public void beforeNavigateTo(String script, WebDriver driver) {
    	onBeforeAction();
    }

    @Override
    public void beforeScript(String script, WebDriver driver) {
    	onBeforeAction();
    }

    @Override
    public void onException(Throwable thr, WebDriver driver) {
        LOGGER.debug("DriverListener->onException starting...");
        // [VD] make below code as much safety as possible otherwise potential recursive failure could occur with driver related issue.
        // most suspicious are capture screenshots, generating dumps etc
        if (thr.getMessage() == null)
            return;

        if (thr.getStackTrace().toString().contains("com.qaprosoft.carina.core.foundation.webdriver.listener.DriverListener.onException")) {
            LOGGER.error("Do not generate screenshot for invalid driver!");
            // prevent recursive crash for onException
            return;
        }

        if (thr.getMessage().contains("Method has not yet been implemented")) {
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
        if (thr.getMessage().contains("is not running, possibly crashed")) {
            throw new RuntimeException(thr);
        }

        String urlPrefix = "";
        try {
            urlPrefix = "url: " + driver.getCurrentUrl() + "\n";
            // handle cases which should't be captured
            if (Screenshot.isCaptured(thr.getMessage())) {
                captureScreenshot(urlPrefix + thr.getMessage(), driver, null, true);
            }
        } catch (Exception e) {
            LOGGER.debug("Unrecognized failure detected in DriverListener->onException: " + e.getMessage(), e);
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
    public void afterSwitchToWindow(String arg0, WebDriver arg1) {
        // do nothing

    }

    @Override
    public void beforeSwitchToWindow(String arg0, WebDriver arg1) {
    	onBeforeAction();
    }

    private void captureScreenshot(String comment, WebDriver driver, WebElement element, boolean errorMessage) {
        if (getMessage(errorMessage) != null) {
            comment = getMessage(errorMessage);
        }

        LOGGER.debug("DriverListener->captureScreenshot starting...");
        try {
            if (errorMessage) {
                LOGGER.error(comment);
                Screenshot.captureFailure(driver, comment); // in case of failure
            } else {
                LOGGER.info(comment);
                Screenshot.capture(driver, comment);
            }
        } catch (Exception e) {
            LOGGER.debug("Unrecognized failure detected in DriverListener->captureScreenshot: " + e.getMessage(), e);
        } finally {
            resetMessages();
        }
        LOGGER.debug("DriverListener->captureScreenshot finished...");
    }

    private void onAfterAction(String comment, WebDriver driver) {
        captureScreenshot(comment, driver, null, false);
    }
    

	private void onBeforeAction() {
		// 4a. if "tzid" not exist inside vncArtifact and exists in Reporter -> register new vncArtifact in Zafira.
		// 4b. if "tzid" already exists in current artifact but in Reporter there is another value. Then this is use case for class/suite mode when we share the same
		// driver across different tests

		ITestResult res = Reporter.getCurrentTestResult();
		if (res != null && res.getAttribute("ztid") != null) {
			Long ztid = (Long) res.getAttribute("ztid");
			if (ztid != vncArtifact.getTestId() && vncArtifact != null && ! StringUtils.isBlank(vncArtifact.getName())) {
				vncArtifact.setTestId(ztid);
				LOGGER.debug("Registered live video artifact " + vncArtifact.getName() + " into zafira");
				ZafiraSingleton.INSTANCE.getClient().addTestArtifact(vncArtifact);
			}

		}
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

	@Override
	public <X> void afterGetScreenshotAs(OutputType<X> arg0, X arg1) {
		// do nothing
		
	}

	@Override
	public <X> void beforeGetScreenshotAs(OutputType<X> arg0) {
		onBeforeAction();
	}

	@Override
	public void afterGetText(WebElement arg0, WebDriver arg1, String arg2) {
		// do nothing		
	}

	@Override
	public void beforeGetText(WebElement arg0, WebDriver arg1) {
		// do nothing		
	}

}