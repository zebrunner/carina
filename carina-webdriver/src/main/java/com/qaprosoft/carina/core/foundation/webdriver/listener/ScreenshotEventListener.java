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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.screenshot.ICapturable.ScreenArea;
import com.qaprosoft.carina.core.foundation.webdriver.screenshot.Screen;

/**
 * ScreenshotEventListener - captures screenshot after essential webdriver event.
 * 
 * @author Alex Khursevich (alex@qaprosoft.com)
 */
public class ScreenshotEventListener implements IConfigurableEventListener {
    private static final Logger LOGGER = Logger.getLogger(ScreenshotEventListener.class);

    @Override
    public boolean enabled() {
        return Configuration.getBoolean(Parameter.AUTO_SCREENSHOT);
    }

    @Override
    public void afterAlertAccept(WebDriver driver) {
        LOGGER.info(ReportContext.saveScreenshot(Screen.getInstance(driver)
                .capture(ScreenArea.VISIBLE_SCREEN).comment("Alert accepted").getImage()));
    }

    @Override
    public void afterAlertDismiss(WebDriver driver) {
        LOGGER.info(ReportContext.saveScreenshot(Screen.getInstance(driver)
                .capture(ScreenArea.VISIBLE_SCREEN).comment("Alert dismissed").getImage()));
    }

    @Override
    public void afterChangeValueOf(WebElement element, WebDriver driver, CharSequence[] value) {
        String comment = String.format("Text '%s' typed", charArrayToString(value));
        LOGGER.info(ReportContext.saveScreenshot(Screen.getInstance(driver)
                .capture(ScreenArea.VISIBLE_SCREEN).highlight(element.getLocation()).comment(comment).getImage()));
    }

    @Override
    public void afterClickOn(WebElement element, WebDriver driver) {
        // Do nothing
    }

    @Override
    public void afterFindBy(By by, WebElement element, WebDriver driver) {
        // Do nothing
    }

    @Override
    public void afterNavigateBack(WebDriver driver) {
        LOGGER.info(ReportContext.saveScreenshot(Screen.getInstance(driver)
                .capture(ScreenArea.VISIBLE_SCREEN).comment("Navigated back").getImage()));
    }

    @Override
    public void afterNavigateForward(WebDriver driver) {
        LOGGER.info(ReportContext.saveScreenshot(Screen.getInstance(driver)
                .capture(ScreenArea.VISIBLE_SCREEN).comment("Navigated forward").getImage()));
    }

    @Override
    public void afterNavigateRefresh(WebDriver driver) {
        LOGGER.info(ReportContext.saveScreenshot(Screen.getInstance(driver)
                .capture(ScreenArea.VISIBLE_SCREEN).comment("Page refreshed").getImage()));
    }

    @Override
    public void afterNavigateTo(String url, WebDriver driver) {
        String comment = String.format("URL '%s' opened", url);
        LOGGER.info(ReportContext.saveScreenshot(Screen.getInstance(driver)
                .capture(ScreenArea.VISIBLE_SCREEN).comment(comment).getImage()));
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
        LOGGER.info(ReportContext.saveScreenshot(Screen.getInstance(driver)
                .capture(ScreenArea.VISIBLE_SCREEN).highlight(element.getLocation()).comment("Element clicked").getImage()));
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
    public void onException(Throwable t, WebDriver driver) {
        // Do nothing
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
}