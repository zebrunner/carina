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
package com.qaprosoft.carina.core.gui.mobile.devices.android.phone.pages.fakegps;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import com.qaprosoft.carina.core.foundation.utils.android.AndroidUtils;
import com.qaprosoft.carina.core.foundation.utils.common.CommonUtils;
import com.qaprosoft.carina.core.foundation.utils.mobile.MobileUtils;
import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.gui.mobile.devices.MobileAbstractPage;

import io.appium.java_client.android.AndroidKeyCode;

/**
 * Fake GPS Page
 */

public class FakeGpsPage extends MobileAbstractPage {

    @FindBy(id = "com.lexa.fakegps:id/buttonStart")
    private ExtendedWebElement setLocationButton;

    @FindBy(id = "com.lexa.fakegps:id/action_start")
    private ExtendedWebElement setLocationStart;

    @FindBy(id = "com.lexa.fakegps:id/action_search")
    private ExtendedWebElement actionSearch;

    @FindBy(id = "com.lexa.fakegps:id/menu_search")
    private ExtendedWebElement locationSearch;

    @FindBy(id = "android:id/button1")
    private ExtendedWebElement messagesOkBtn;

    @FindBy(id = "android:id/alertTitle")
    private ExtendedWebElement alertTitle;

    @FindBy(id = "com.lexa.fakegps:id/buttonStop")
    private ExtendedWebElement stopFakeGpsButton;

    @FindBy(id = "com.lexa.fakegps:id/action_stop")
    private ExtendedWebElement stopFakeGpsButtonNew;

    @FindBy(id = "com.lexa.fakegps:id/button")
    private ExtendedWebElement openSettingsButton;

    @FindBy(xpath = "//*[@content-desc='More options']")
    private ExtendedWebElement openSettingsButtonNew;

    @FindBy(xpath = "//android.widget.TextView[@text='Settings']")
    private ExtendedWebElement openDevSettings;

    @FindBy(xpath = "//android.widget.FrameLayout[@resource-id='android:id/custom']/android.widget.EditText")
    private ExtendedWebElement inputLocation;

    @FindBy(id = "android:id/search_src_text")
    private ExtendedWebElement inputLocationNew;

    // @FindBy(xpath = "//android.widget.TextView[contains(@text,'Allow mock locations')]")
    @FindBy(xpath = "//android.widget.TextView[contains(@text,'ock location')]")
    private ExtendedWebElement allowMock;

    @FindBy(xpath = "//android.widget.TextView[contains(@text,'ock location')]")
    private ExtendedWebElement allowMock7;

    @FindBy(xpath = "//*[contains(@resource-id,':id/list')]")
    private ExtendedWebElement devSettingsContainer;

    @FindBy(xpath = "//android.widget.TextView[contains(@text,'com.lexa.fakegps')]")
    private ExtendedWebElement fakeGpsPackage;

    protected static final int MINIMAL_TIMEOUT = 1;

    public FakeGpsPage(WebDriver driver) {
        super(driver);
    }

    protected static final Logger LOGGER = Logger.getLogger(FakeGpsPage.class);

    public void clickSetLocation() {
        if (setLocationStart.isElementPresent(DELAY)) {
            LOGGER.info("Start Fake GPS");
            setLocationStart.click();
        } else {
            LOGGER.info("Old app");
            setLocationButton.click();
        }
    }

    public boolean locationSearch(String location) {
        solveMockSettings();

        if (actionSearch.isElementPresent(DELAY)) {
            actionSearch.click();
            if (inputLocationNew.isElementPresent(DELAY)) {
                inputLocationNew.type(location);
                AndroidUtils.pressKeyCode(AndroidKeyCode.ENTER);
                AndroidUtils.pressKeyCode(AndroidKeyCode.KEYCODE_SEARCH);
                CommonUtils.pause(3);
                return true;
            }
        } else {
            LOGGER.info("Old app");
            locationSearch.click();
            if (inputLocation.isElementPresent(DELAY)) {
                inputLocation.type(location);
                messagesOkBtn.click();
                return true;
            }
        }
        return false;
    }

    public boolean clickStopFakeGps() {
        if (stopFakeGpsButtonNew.isElementPresent(DELAY)) {
            return stopFakeGpsButtonNew.clickIfPresent(MINIMAL_TIMEOUT);
        } else {
            return stopFakeGpsButton.clickIfPresent(DELAY);
        }
    }

    public boolean isOpenSettingButtonPresent() {
        return openSettingsButton.isElementPresent(MINIMAL_TIMEOUT);
    }

    public void solveMockSettings() {

        if (openSettingsButton.isElementPresent(MINIMAL_TIMEOUT)) {
            openSettingsButton.clickIfPresent(DELAY);

            String currentAndroidVersion = IDriverPool.getDefaultDevice().getOsVersion();
            LOGGER.info("currentAndroidVersion=" + currentAndroidVersion);
            if (currentAndroidVersion.contains("7.")) {
                MobileUtils.swipe(allowMock7, devSettingsContainer);
                allowMock7.clickIfPresent(MINIMAL_TIMEOUT);
                fakeGpsPackage.clickIfPresent(DELAY);
            } else {
                MobileUtils.swipe(allowMock, devSettingsContainer);
                LOGGER.info("Allow Mock config is present:" + allowMock.isElementPresent(SHORT_TIMEOUT));
                allowMock.clickIfPresent(MINIMAL_TIMEOUT);
                fakeGpsPackage.clickIfPresent(DELAY / 3);
            }

            getDriver().navigate().back();
        }
    }

    public boolean isOpened(long timeout) {
        return setLocationStart.isElementPresent(timeout) || setLocationButton.isElementPresent(timeout) || isOpenSettingButtonPresent();
    }

    @Override
    public boolean isOpened() {
        return isOpened(EXPLICIT_TIMEOUT / 2);
    }

}
