package com.qaprosoft.carina.core.gui.mobile.devices.common.pages.settings;

import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.gui.mobile.devices.MobileAbstractPage;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public abstract class DateTimeSettingsPageBase extends MobileAbstractPage {

    protected static final Logger LOGGER = Logger.getLogger(DateTimeSettingsPageBase.class);

    @FindBy(xpath = "//*[@resource-id = 'com.android.settings:id/date_time_settings_fragment' or @resource-id = 'com.android.systemui:id/latestItems']")
    protected ExtendedWebElement title;

    public DateTimeSettingsPageBase(WebDriver driver) {
        super(driver);

    }

    public abstract void openTimeZoneSetting();

    public abstract boolean selectTimeZone(String tz, String timezone);

    public abstract boolean clickNextButton();

    public abstract boolean isOpened(long timeout);

    @Override
    public boolean isOpened() {
        return isOpened(LONG_TIMEOUT);
    }


}