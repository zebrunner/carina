package com.qaprosoft.carina.core.gui.mobile.devices;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.gui.AbstractPage;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;

public abstract class MobileAbstractPage extends AbstractPage {

    protected static final Logger LOGGER = Logger.getLogger(MobileAbstractPage.class);

    protected static final long DELAY = 10;

    protected static final long SHORT_TIMEOUT = Configuration.getLong(Configuration.Parameter.EXPLICIT_TIMEOUT) / 20;

    protected static final long ONE_SEC_TIMEOUT = 1;

    protected static final long DEFAULT_TRIES = 10;

    public static final long PUSH_NOTIFICATIONS_TIMEOUT = 120;

    public static final int SWIPE_DURATION = 1000;

    public MobileAbstractPage(WebDriver driver) {
        super(driver);
    }

    /**
     * @return true by default. Override it in child classes
     */
    public abstract boolean isOpened();

}
