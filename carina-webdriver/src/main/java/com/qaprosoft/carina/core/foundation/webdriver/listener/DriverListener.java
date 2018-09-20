/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
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
		if (thr.getMessage() != null) {
			if (thr.getStackTrace().toString().contains("com.qaprosoft.carina.core.foundation.webdriver.listener.DriverListener.onException")) {
				LOGGER.error("Do not generate screenshot for invalid driver!");
				//prevent recursive crash for onException
				return;
			}
			
			// handle use-case when application crashed on iOS but tests continue to execute something because doesn't raise valid exception
			// Example:

			// 10:25:20 2018-09-14 10:29:39 DriverListener [TestNG-31] [ERROR]
			// [iPhone_6s] An unknown server-side error occurred while
			// processing the command. Original error: The application under
			// test with bundle id 'Q5AWL8WCY6.iMapMyRun' is not running,
			// possibly crashed (WARNING: The server did not provide any
			// stacktrace information)
			
			if (thr.getMessage().contains("is not running, possibly crashed")) {
				throw new RuntimeException(thr);
			}
			
			String urlPrefix = "";
			try {
				urlPrefix = "url: " + driver.getCurrentUrl() + "\n";
			} catch (Exception e) {
				//do  nothing
			}
			
			// handle cases which should't be captured
			if (!thr.getMessage().contains("StaleObjectException")
					&& !thr.getMessage().contains("was terminated due to FORWARDING_TO_NODE_FAILED")
					&& !thr.getMessage().contains("InvalidElementStateException")
					&& !thr.getMessage().contains("stale element reference")
					&& !thr.getMessage().contains("no such element: Unable to locate element")
					&& !thr.getMessage().contains("An element could not be located on the page using the given search parameters")
					//&& !thr.getMessage().contains("timeout")
					&& !thr.getMessage().contains("chrome not reachable")
					&& !thr.getMessage().contains("cannot forward the request Connect to")
					&& !thr.getMessage().contains("Session ID is null. Using WebDriver after calling quit")
					&& !thr.getMessage().contains("was terminated due to TIMEOUT")
					&& !thr.getMessage().contains("Could not proxy command to remote server. Original error: Error: read ECONNRESET")
					&& !thr.getMessage().contains("Session timed out or not found")) {
				LOGGER.error(thr); // temporary put into the logs whole stacktrace
				captureScreenshot(urlPrefix + thr.getMessage(), driver, null, true);
			}
		}
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

        if (errorMessage) {
            LOGGER.error(comment);
            Screenshot.captureFailure(driver, comment); // in case of failure
        } else {
            LOGGER.info(comment);
            Screenshot.capture(driver, comment);
        }
        
        resetMessages();

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

}