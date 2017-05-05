package com.qaprosoft.carina.core.gui.mobile.devices.android.phone.pages.tzchanger;

import com.qaprosoft.carina.core.foundation.utils.mobile.MobileUtils;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.gui.mobile.devices.MobileAbstractPage;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

public class TZChangerPage extends MobileAbstractPage {

    protected static final Logger LOGGER = Logger.getLogger(TZChangerPage.class);

    @FindBy(id = "com.futurek.android.tzc:id/txt_selected")
    protected ExtendedWebElement title;

    public TZChangerPage(WebDriver driver) {
        super(driver);

    }

    @FindBy(xpath = "//android.widget.ListView")
    protected ExtendedWebElement scrollableContainer;

    @FindBy(xpath = "//android.widget.TextView[@resource-id='android:id/text1' and contains(@text,'%s')]")
    protected ExtendedWebElement tzSelectionBase;

    @FindBy(id = "com.android.settings:id/next_button")
    protected ExtendedWebElement nextButton;

    protected static final String TIMEZONE_TEXT_BASE = "//android.widget.TextView[contains(@text,'%s')]";


    /**
     * selectTimeZone
     * @param timezone String format should be "Europe/London"
     * @return boolean
     */
    public boolean selectTimeZone(String timezone) {
        int defaultSwipeTime = 30;

        String baseTimezoneText = timezone;
        boolean selected = false;
        String tz = "";

        if (timezone.contains("/")) {
            tz = timezone.split("/")[0];
        } else {
            LOGGER.error("Incorrect timezone format: " + timezone);
            return false;
        }

        LOGGER.info("Searching for tz: " + tz);
        if (scrollableContainer.isElementPresent(SHORT_TIMEOUT)) {
            LOGGER.info("Scrollable container present.");
            boolean scrolled = MobileUtils.swipeInContainerTillElement(
                    format(1, tzSelectionBase, tz),
                    scrollableContainer, defaultSwipeTime);
            if (!scrolled) {
                LOGGER.info("Probably we have long list. Let's increase swipe attempts.");
                defaultSwipeTime = 50;
                scrolled = MobileUtils.swipeInContainerTillElement(
                        format(1, tzSelectionBase, tz),
                        scrollableContainer, defaultSwipeTime);
            }
            if (scrolled) {

                LOGGER.info("Select timezone folder: " + tz);
                format(1, tzSelectionBase, tz).click();


                LOGGER.info("Searching for " + timezone);
                scrolled = MobileUtils.swipeInContainerTillElement(
                        format(1, tzSelectionBase, timezone),
                        scrollableContainer, defaultSwipeTime);
                if (scrolled) {

                    LOGGER.info("Select timezone by TimeZone text: " + timezone);
                    format(1, tzSelectionBase, timezone).click();
                    selected = true;
                } else {
                    LOGGER.error("Did not find timezone by timezone text: " + timezone);
                    defaultSwipeTime = 30;
                    scrolled = MobileUtils.swipeInContainerTillElement(
                            format(1, tzSelectionBase, timezone),
                            scrollableContainer, defaultSwipeTime);
                    if (scrolled) {
                        LOGGER.info("Select timezone: " + timezone);
                        format(1, tzSelectionBase, timezone).click();
                        selected = true;
                    }
                }
            } else {
                LOGGER.error("Didn't find timezone: " + timezone);
            }

        }


        return selected;
    }

    /**
     * selectTimezoneByText in TZ changer
     * @param timezone String
     * @param defaultSwipeTime int
     * @return boolean
     */
    private boolean selectTimezoneByText(String timezone, int defaultSwipeTime) {
        boolean scrolled = MobileUtils.swipeInContainerTillElement(
                format(1, tzSelectionBase, timezone),
                scrollableContainer, defaultSwipeTime);
        if (scrolled) {
            LOGGER.info("Select timezone by TimeZone text: " + timezone);
            format(1, tzSelectionBase, timezone).click();
        }
        return scrolled;
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
        return isOpened(EXPLICIT_TIMEOUT );
    }


}