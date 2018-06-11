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

import com.qaprosoft.carina.core.foundation.webdriver.Screenshot;

/**
 * ScreenshotEventListener - captures screenshot after essential webdriver event.
 * 
 * @author Alex Khursevich (alex@qaprosoft.com)
 */
public class DriverListener implements IConfigurableEventListener {

    private static final Logger LOGGER = Logger.getLogger(DriverListener.class);

    private final static ThreadLocal<String> currentPositiveMessage = new ThreadLocal<String>();
    private final static ThreadLocal<String> currentNegativeMessage = new ThreadLocal<String>();

    @Override
    public boolean enabled() {
        // custom listener should be enabled forever as UI actions logging moved to this class
        return true;
    }

    @Override
    public void afterAlertAccept(WebDriver driver) {
        captureScreenshot("Alert accepted", driver);
    }

    @Override
    public void afterAlertDismiss(WebDriver driver) {
        captureScreenshot("Alert dismissed", driver);
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
        captureScreenshot("Navigated back", driver);
    }

    @Override
    public void afterNavigateForward(WebDriver driver) {
        captureScreenshot("Navigated forward", driver);
    }

    @Override
    public void afterNavigateRefresh(WebDriver driver) {
        captureScreenshot("Page refreshed", driver);
    }

    @Override
    public void afterNavigateTo(String url, WebDriver driver) {
        String comment = String.format("URL '%s' opened", url);
        captureScreenshot(comment, driver);
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
		if (thr.getMessage() != null) {
			// handle cases which should't be captured
			// TODO: analyze maybe we can easier specify list of issues for
			// capturing here
			if (!thr.getMessage().contains("StaleObjectException")
					&& !thr.getMessage().contains("InvalidElementStateException")
					&& !thr.getMessage().contains("stale element reference")
					&& !thr.getMessage().contains("no such element: Unable to locate element")
					&& !thr.getMessage().contains("An element could not be located on the page using the given search parameters")
					//&& !thr.getMessage().contains("timeout")
					&& !thr.getMessage().contains("chrome not reachable")
					&& !thr.getMessage().contains("Session timed out or not found")) {
				captureScreenshot(thr.getMessage(), driver, null, true);
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
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeSwitchToWindow(String arg0, WebDriver arg1) {
        // TODO Auto-generated method stub

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
        
        /*
         * if (element != null) {
         * ReportContext.saveScreenshot(Screen.getInstance(driver)
         * .capture(ScreenArea.VISIBLE_SCREEN).highlight(element.getLocation()).comment(comment).getImage());
         * } else {
         * ReportContext.saveScreenshot(Screen.getInstance(driver)
         * .capture(ScreenArea.VISIBLE_SCREEN).comment(comment).getImage());
         * }
         */
        resetMessages();

        // examples of new screenshooting approaches
        /*
         * LOGGER.info(ReportContext.saveScreenshot(Screen.getInstance(driver)
         * .capture(ScreenArea.VISIBLE_SCREEN).highlight(element.getLocation()).comment(comment).getImage()));
         * 
         * LOGGER.info(ReportContext.saveScreenshot(Screen.getInstance(driver)
         * .capture(ScreenArea.VISIBLE_SCREEN).comment("Alert accepted").getImage()));
         * 
         * ReportContext.saveScreenshot(Screen.getInstance(driver).capture(ScreenArea.VISIBLE_SCREEN)
         * .highlight(element.getLocation()).comment(comment).getImage());
         * 
         * 
         */
    }

    private void captureScreenshot(String comment, WebDriver driver) {
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

	@Override
	public <X> void afterGetScreenshotAs(OutputType<X> arg0, X arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <X> void beforeGetScreenshotAs(OutputType<X> arg0) {
		// TODO Auto-generated method stub
		
	}

}