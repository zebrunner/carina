package com.qaprosoft.carina.core.gui.mobile.devices.common.pages.tzchanger;

import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.gui.mobile.devices.MobileAbstractPage;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

public abstract class TZChangerPageBase extends MobileAbstractPage {

    protected static final Logger LOGGER = Logger.getLogger(TZChangerPageBase.class);

    @FindBy(id = "com.futurek.android.tzc:id/txt_selected")
    protected ExtendedWebElement title;

    public TZChangerPageBase(WebDriver driver) {
        super(driver);

    }


    public abstract boolean selectTimeZone(String timezone);

    public abstract boolean isOpened(long timeout);

    @Override
    public boolean isOpened() {
        return isOpened(LONG_TIMEOUT);
    }


}