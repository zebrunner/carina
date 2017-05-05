package com.qaprosoft.carina.core.gui.mobile.devices.android.pages.phone.tzchanger;


import com.qaprosoft.carina.core.foundation.utils.factory.DeviceType;
import com.qaprosoft.carina.core.foundation.utils.factory.DeviceType.Type;
import com.qaprosoft.carina.core.foundation.utils.mobile.MobileUtils;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.gui.mobile.devices.common.pages.tzchanger.TZChangerPageBase;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

@DeviceType(pageType = Type.ANDROID_PHONE, parentClass = TZChangerPageBase.class)
public class TZChangerPage extends TZChangerPageBase {

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


    @Override
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
    @Override
    public boolean isOpened(long timeout) {
        return title.isElementPresent(timeout);
    }

    @Override
    public boolean isOpened() {
        return isOpened(EXPLICIT_TIMEOUT );
    }
}
