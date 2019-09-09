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
package com.qaprosoft.carina.core.gui.mobile.devices.android.phone.pages.settings;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.utils.android.AndroidUtils;
import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.gui.mobile.devices.MobileAbstractPage;

public class DateTimeSettingsPage extends MobileAbstractPage {

    private static final Logger LOGGER = Logger.getLogger(DateTimeSettingsPage.class);

    @FindBy(xpath = "//android.widget.TextView[@text = 'Date & time']")
    protected ExtendedWebElement dateAndTimeScreenHeaderTitle;

    @FindBy(xpath = "//android.widget.TextView[@text = 'Time zone']")
    protected ExtendedWebElement timeZoneOption;

    @FindBy(xpath = "//android.widget.TextView[@text = 'Region']")
    protected ExtendedWebElement timeZoneRegionOption;

    @FindBy(id = "android:id/search_src_text")
    protected ExtendedWebElement timeZoneRegionSearchInputField;

    @FindBy(xpath = "//*[@resource-id='com.android.settings:id/recycler_view']//android.widget.TextView[contains(@text,'%s')]")
    protected ExtendedWebElement timeZoneRegionSearchResult;

    public DateTimeSettingsPage(WebDriver driver) {
        super(driver);

    }

    @FindBy(xpath = "//android.widget.TextView[@text = 'Select time zone']")
    protected ExtendedWebElement selectTimeZone;

    @FindBy(xpath = "//android.widget.ListView")
    protected ExtendedWebElement scrollableContainer;

    @FindBy(id = "com.android.settings:id/recycler_view")
    protected ExtendedWebElement scrollableContainerInVersion8_1;

    @FindBy(className = "android.widget.ListView")
    protected ExtendedWebElement scrollableContainerByClassName;

    @FindBy(xpath = "//android.widget.TextView[contains(@text,'%s')]")
    protected ExtendedWebElement tzSelectionBase;

    @FindBy(id = "com.android.settings:id/next_button")
    protected ExtendedWebElement nextButton;

    protected static final String SELECT_TIME_ZONE_TEXT = "Select time zone";

    /**
     * openTimeZoneSetting
     */
    public void openTimeZoneSetting() {
        boolean found = selectTimeZone.clickIfPresent(SHORT_TIMEOUT);
        if (!found) {
            boolean scrolled = AndroidUtils.scroll(SELECT_TIME_ZONE_TEXT, scrollableContainerByClassName,
                    AndroidUtils.SelectorType.CLASS_NAME, AndroidUtils.SelectorType.TEXT).isElementPresent();
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
     * @param tzGMT    String
     * @return boolean
     */
    public boolean selectTimeZone(String tz, String timezone, String tzGMT) {
        boolean selected = false;
        String deviceOsFullVersion = IDriverPool.getDefaultDevice().getOsVersion();
        int deviceOsVersion;

        //Adding extra step required to get to TimeZone screen on devices running versions > 8
        if (deviceOsFullVersion.contains(".")) {
            deviceOsVersion = Integer.valueOf(deviceOsFullVersion.split("\\.")[0]);
        } else {
            deviceOsVersion = Integer.valueOf(deviceOsFullVersion);
        }

        //if device OS version >= 9, we have to set Country Region and obtain city from timeZone
        setupTimezoneRegion(timezone, deviceOsVersion);
        tz = tz.split("/")[1].replace("_", " ");

        //locating timeZone by City
        if (!tz.isEmpty() && locateTimeZoneByCity(tz, deviceOsVersion)) {
            tzSelectionBase.format(tz).click();
            selected = true;
        }

        //locating timeZone by GMT
        if (!selected && locateTimeZoneByGMT(tzGMT, deviceOsVersion)) {
            tzSelectionBase.format(tzGMT).click();
            selected = true;
        }

        return selected;
    }

    /**
     * setup timezone region (this method is responsible for setting up timezone region, req. for OS version > 8)
     *
     * @param timezoneRegion         String
     * @param deviceOsVersion  int
     */
    private void setupTimezoneRegion(String timezoneRegion, int deviceOsVersion){
        if (deviceOsVersion >= 9) {
            LOGGER.info("Detected Android version 8 or above, selecting country region for 'Time Zone' option..");
            timeZoneRegionOption.clickIfPresent();
            timeZoneRegionSearchInputField.type(timezoneRegion);
            timeZoneRegionSearchResult.format(timezoneRegion).click();
        }
    }

    /**
     * selectTimezoneByGMT
     *
     * @param tzGMT         String
     * @param deviceOsVersion  int
     * @return boolean
     */
    private boolean locateTimeZoneByGMT(String tzGMT, int deviceOsVersion){
        LOGGER.info("Searching for tz by GTM: " + tzGMT);
        boolean result = false;

        if (deviceOsVersion > 8) {
            try {
                result = AndroidUtils.scroll(tzGMT, scrollableContainerInVersion8_1,
                        AndroidUtils.SelectorType.ID, AndroidUtils.SelectorType.TEXT_CONTAINS).isElementPresent();
            } catch (NoSuchElementException e){
                e.printStackTrace();
                result = false;
            }
        } else {
            try {
                result = AndroidUtils.scroll(tzGMT, scrollableContainerByClassName,
                        AndroidUtils.SelectorType.CLASS_NAME, AndroidUtils.SelectorType.TEXT_CONTAINS).isElementPresent();
            } catch (NoSuchElementException e){
                e.printStackTrace();
                result = false;
            }
        }

        return result;
    }

    /**
     * selectTimezoneByCity
     *
     * @param tz         String
     * @param deviceOsVersion  int
     * @return boolean
     */
    private boolean locateTimeZoneByCity(String tz, int deviceOsVersion){
        LOGGER.info("Searching for tz by City: " + tz);
        boolean result = false;

        if (deviceOsVersion > 8) {
            try {
                result = AndroidUtils.scroll(tz, scrollableContainerInVersion8_1,
                        AndroidUtils.SelectorType.ID, AndroidUtils.SelectorType.TEXT_CONTAINS).isElementPresent();
            } catch (NoSuchElementException e){
                e.printStackTrace();
                result = false;
            }
        } else {
            try {
                result = AndroidUtils.scroll(tz, scrollableContainerByClassName,
                        AndroidUtils.SelectorType.CLASS_NAME, AndroidUtils.SelectorType.TEXT_CONTAINS).isElementPresent();
            } catch (NoSuchElementException e){
                e.printStackTrace();
                result = false;
            }
        }

        return result;
    }


    /**
     * selectTimezoneByText
     *
     * @param timezone         String
     * @param defaultSwipeTime int
     * @return boolean
     */
    private boolean selectTimezoneByText(String timezone, int defaultSwipeTime) {
        boolean scrolled = AndroidUtils.scroll(timezone, scrollableContainerByClassName,
                AndroidUtils.SelectorType.CLASS_NAME, AndroidUtils.SelectorType.TEXT).isElementPresent();
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
        return dateAndTimeScreenHeaderTitle.isElementPresent(timeout);
    }

    @Override
    public boolean isOpened() {
        return isOpened(EXPLICIT_TIMEOUT);
    }

}