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
package com.qaprosoft.carina.core.gui.mobile.devices.android.phone.pages.settings;

import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import com.qaprosoft.carina.core.foundation.utils.mobile.MobileUtils;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.gui.mobile.devices.MobileAbstractPage;

public class DateTimeSettingsPage extends MobileAbstractPage {

    protected static final Logger LOGGER = Logger.getLogger(DateTimeSettingsPage.class);

    @FindBy(xpath = "//*[@resource-id = 'com.android.settings:id/date_time_settings_fragment' or @resource-id = 'com.android.systemui:id/latestItems']")
    protected ExtendedWebElement title;

    public DateTimeSettingsPage(WebDriver driver) {
        super(driver);

    }

    @FindBy(xpath = "//android.widget.TextView[@text = 'Select time zone']")
    protected ExtendedWebElement selectTimeZone;

    @FindBy(xpath = "//android.widget.ListView")
    protected ExtendedWebElement scrollableContainer;

    // @FindBy(xpath =
    // "//android.widget.ListView[@resource-id='com.bamnetworks.mobile.android.gameday.atbat:id/drawer_list_view']//android.widget.TextView[contains(@text,'%s')]")
    @FindBy(xpath = "//android.widget.TextView[contains(@text,'%s')]")
    protected ExtendedWebElement tzSelectionBase;

    @FindBy(id = "com.android.settings:id/next_button")
    protected ExtendedWebElement nextButton;

    protected static final String TIMEZONE_TEXT_BASE = "//android.widget.TextView[contains(@text,'%s')]";

    /**
     * openTimeZoneSetting
     */
    public void openTimeZoneSetting() {
        boolean found = selectTimeZone.clickIfPresent(SHORT_TIMEOUT);
        if (!found) {
            boolean scrolled = MobileUtils.swipe(
                    selectTimeZone,
                    scrollableContainer);
            if (scrolled) {
                found = selectTimeZone.clickIfPresent(SHORT_TIMEOUT);
            } else {
                throw new RuntimeException("Desired Time Zone Menu item not found.. ");
            }
        }
        LOGGER.info("Select Time Zone Menu item was clicked: " + found);
    }

    /**
     * selectTimeZone
     *
     * @param tz       String
     * @param timezone String
     * @return boolean
     */
    public boolean selectTimeZone(String tz, String timezone) {
        int defaultSwipeTime = 15;
        boolean multiTimezoneText = false;
        String baseTimezoneText = timezone;
        boolean selected = false;

        LOGGER.info("Searching for tz: " + tz);
        // TODO: Think how to cover GMT+3:00 instead of GMT+03:00 on some devices.
        if (scrollableContainer.isElementPresent(SHORT_TIMEOUT)) {
            LOGGER.info("Scrollable container present.");
            boolean scrolled = MobileUtils.swipe(
                    tzSelectionBase.format(tz),
                    scrollableContainer, defaultSwipeTime);
            if (!scrolled) {
                LOGGER.info("Probably we have long list. Let's increase swipe attempts.");
                defaultSwipeTime = 30;
                scrolled = MobileUtils.swipe(
                        tzSelectionBase.format(tz),
                        scrollableContainer, defaultSwipeTime);
            }
            if (scrolled) {
                if (timezone.isEmpty()) {
                    LOGGER.info("Select timezone by GMT: " + tz);
                    tzSelectionBase.format(tz).click();
                    selected = true;
                } else {
                    LOGGER.info("Check that timezone by GMT '" + tz + "' is unique.");
                    if (baseTimezoneText.contains(",")) {
                        LOGGER.info("Looks like we have few possible variants for timezone text: " + timezone);
                        multiTimezoneText = true;
                    }
                    if (!multiTimezoneText) {
                        LOGGER.info("Searching for " + timezone);
                        scrolled = MobileUtils.swipe(
                                tzSelectionBase.format(timezone),
                                scrollableContainer, defaultSwipeTime);
                        if (scrolled) {
                            List<ExtendedWebElement> elements = findExtendedWebElements(By.xpath(String.format(TIMEZONE_TEXT_BASE, tz)), 1);
                            LOGGER.info("Found '" + tz + "' " + elements.size() + " times.");

                            LOGGER.info("Select timezone by TimeZone text: " + timezone);
                            tzSelectionBase.format(timezone).click();
                            selected = true;
                        } else {
                            LOGGER.error("Did not find timezone by timezone text: " + timezone);
                            scrolled = MobileUtils.swipe(
                                    tzSelectionBase.format(tz),
                                    scrollableContainer, defaultSwipeTime);
                            if (scrolled) {
                                LOGGER.info("Select timezone by GMT: " + tz);
                                tzSelectionBase.format(tz).click();
                                selected = true;
                            }
                        }
                    } else {
                        String[] listTZ = baseTimezoneText.split(",");
                        for (String oneOfTz : listTZ) {
                            LOGGER.info("Searching for " + oneOfTz);
                            if (selectTimezoneByText(oneOfTz.trim(), 3)) {
                                LOGGER.info("Successful select timezone by TimeZone text: " + oneOfTz);
                                return true;
                            } else {
                                LOGGER.error("TimeZone text '" + oneOfTz + "'  was not found in the list. ");
                            }
                        }
                    }

                }

            } else {
                LOGGER.error("TimeZone '" + tz + "' was not found in the list. Let's try to find by TimeZone text: " + timezone);
                if (baseTimezoneText.contains(",")) {
                    LOGGER.info("Looks like we have few possible variants for timezone text: " + timezone);
                    multiTimezoneText = true;
                }
                if (!multiTimezoneText) {
                    if (selectTimezoneByText(timezone, defaultSwipeTime)) {
                        LOGGER.info("Successful select timezone by TimeZone text: " + timezone);
                        selected = true;
                    } else {
                        LOGGER.error("TimeZone '" + tz + "' and TimeZone text: " + timezone + "  were not found in the list. ");
                    }
                } else {
                    String[] listTZ = baseTimezoneText.split(",");
                    for (String oneOfTz : listTZ) {
                        LOGGER.info("Searching for " + oneOfTz);
                        if (selectTimezoneByText(oneOfTz.trim(), defaultSwipeTime)) {
                            LOGGER.info("Successful select timezone by TimeZone text: " + oneOfTz);
                            return true;
                        } else {
                            LOGGER.error("TimeZone text '" + oneOfTz + "'  was not found in the list. ");
                        }
                    }
                }
            }

        }

        return selected;
    }

    /**
     * selectTimezoneByText
     *
     * @param timezone         String
     * @param defaultSwipeTime int
     * @return boolean
     */
    private boolean selectTimezoneByText(String timezone, int defaultSwipeTime) {
        boolean scrolled = MobileUtils.swipe(tzSelectionBase.format(timezone), scrollableContainer, defaultSwipeTime);
        if (scrolled) {
            LOGGER.info("Select timezone by TimeZone text: " + timezone);
            tzSelectionBase.format(timezone).click();
        }
        return scrolled;
    }

    /**
     * clickNextButton
     *
     * @return boolean
     */
    public boolean clickNextButton() {
        boolean res = nextButton.clickIfPresent(SHORT_TIMEOUT);
        LOGGER.info("Next button was clicked: " + res);
        return res;
    }

    /**
     * isOpened
     *
     * @param timeout long
     * @return boolean
     */
    public boolean isOpened(long timeout) {
        return title.isElementPresent(timeout);
    }

    @Override
    public boolean isOpened() {
        return isOpened(EXPLICIT_TIMEOUT);
    }

}