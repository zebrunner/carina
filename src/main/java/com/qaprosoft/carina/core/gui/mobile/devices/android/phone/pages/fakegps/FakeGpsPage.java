package com.qaprosoft.carina.core.gui.mobile.devices.android.phone.pages.fakegps;

import com.qaprosoft.carina.core.foundation.utils.android.AndroidUtils;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.gui.mobile.devices.MobileAbstractPage;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.android.AndroidKeyCode;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

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


    @FindBy(xpath = "//android.widget.TextView[contains(@text,'Allow mock locations')]")
    private ExtendedWebElement allowMock;

    protected static final int MINIMAL_TIMEOUT = 1;

    public FakeGpsPage(WebDriver driver) {
        super(driver);
    }


    protected static final Logger LOGGER = Logger.getLogger(FakeGpsPage.class);

    public void clickSetLocation() {
        if (setLocationStart.isElementPresent(1)) {
            LOGGER.info("Start Fake GPS");
            setLocationStart.click();
        } else {
            LOGGER.info("Old app");
            setLocationButton.click();
        }
    }


    public boolean locationSearch(String location) {
        //solveMockSettings();
        //TODO: Check, do we have to set Allow from DevSetting on new version

        if (actionSearch.isElementPresent(1)) {
            actionSearch.click();
            if (inputLocationNew.isElementPresent(DELAY)) {
                inputLocationNew.type(location);
                ((AndroidDriver<AndroidElement>) getDriver()).pressKeyCode(AndroidKeyCode.ENTER);
                ((AndroidDriver<AndroidElement>) getDriver()).pressKeyCode(AndroidKeyCode.KEYCODE_SEARCH);
                pause(3);
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
            return stopFakeGpsButtonNew.clickIfPresent(1);
        } else {
            return stopFakeGpsButton.clickIfPresent(DELAY);
        }
    }

    public boolean isOpenSettingButtonPresent() {
        return openSettingsButtonNew.isElementPresent(1) || openSettingsButton.isElementPresent(1);
    }

    public void solveMockSettings() {

        if (openSettingsButtonNew.isElementPresent(1)) {
            openSettingsButtonNew.click();
            openDevSettings.click();
        } else {
            openSettingsButton.clickIfPresent(DELAY);
        }
        //TODO: Update with correct scroll later if needed
        AndroidUtils.scrollTo(allowMock);
        LOGGER.info("Allow Mock config is present:" + allowMock.isElementPresent(SHORT_TIMEOUT));
        allowMock.clickIfPresent(1);
        getDriver().navigate().back();
    }

    public boolean isOpened(long timeout) {
        return isElementPresent(setLocationStart, timeout) || isElementPresent(setLocationButton, timeout) || isOpenSettingButtonPresent();
    }

    @Override
    public boolean isOpened() {
        return isOpened(EXPLICIT_TIMEOUT / 2);
    }


}
