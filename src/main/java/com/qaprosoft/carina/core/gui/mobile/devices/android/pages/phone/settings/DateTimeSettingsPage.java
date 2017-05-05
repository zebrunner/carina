package com.qaprosoft.carina.core.gui.mobile.devices.android.pages.phone.settings;

import com.qaprosoft.carina.core.foundation.utils.factory.DeviceType;
import com.qaprosoft.carina.core.foundation.utils.factory.DeviceType.Type;
import com.qaprosoft.carina.core.foundation.utils.mobile.MobileUtils;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.gui.mobile.devices.common.pages.settings.DateTimeSettingsPageBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import java.util.List;

@DeviceType(pageType = Type.ANDROID_PHONE, parentClass = DateTimeSettingsPageBase.class)
public class DateTimeSettingsPage extends DateTimeSettingsPageBase {

    public DateTimeSettingsPage(WebDriver driver) {
        super(driver);
    }

    protected static final By NOTIFICATION_XPATH = By
            .xpath("//*[@resource-id = 'com.android.systemui:id/"
                    + "notification_stack_scroller']/android.widget.FrameLayout");

    @FindBy(xpath = "//android.widget.TextView[@text = 'Select time zone']")
    protected ExtendedWebElement selectTimeZone;

    @FindBy(xpath = "//android.widget.ListView")
    protected ExtendedWebElement scrollableContainer;

    //@FindBy(xpath = "//android.widget.ListView[@resource-id='com.bamnetworks.mobile.android.gameday.atbat:id/drawer_list_view']//android.widget.TextView[contains(@text,'%s')]")
    @FindBy(xpath = "//android.widget.TextView[contains(@text,'%s')]")
    protected ExtendedWebElement tzSelectionBase;

    @FindBy(id = "com.android.settings:id/next_button")
    protected ExtendedWebElement nextButton;

    protected static final String TIMEZONE_TEXT_BASE = "//android.widget.TextView[contains(@text,'%s')]";

    @Override
    public void openTimeZoneSetting() {
        boolean found = selectTimeZone.clickIfPresent(SHORT_TIMEOUT);
        if (!found) {
            boolean scrolled = MobileUtils.swipeInContainerTillElement(
                    selectTimeZone,
                    scrollableContainer);
            if (scrolled) {
                found = selectTimeZone.clickIfPresent(SHORT_TIMEOUT);
            }
        }
        LOGGER.info("Select Time Zone Menu item was clicked: " + found);
    }

    @Override
    public boolean selectTimeZone(String tz, String timezone) {
        int defaultSwipeTime = 15;
        boolean multiTimezoneText = false;
        String baseTimezoneText = timezone;
        boolean selected = false;

        LOGGER.info("Searching for tz: " + tz);
        //TODO: Think how to cover GMT+3:00 instead of GMT+03:00 on some devices.
        if (scrollableContainer.isElementPresent(SHORT_TIMEOUT)) {
            LOGGER.info("Scrollable container present.");
            boolean scrolled = MobileUtils.swipeInContainerTillElement(
                    format(1, tzSelectionBase, tz),
                    scrollableContainer, defaultSwipeTime);
            if (!scrolled) {
                LOGGER.info("Probably we have long list. Let's increase swipe attempts.");
                defaultSwipeTime = 30;
                scrolled = MobileUtils.swipeInContainerTillElement(
                        format(1, tzSelectionBase, tz),
                        scrollableContainer, defaultSwipeTime);
            }
            if (scrolled) {
                if (timezone.isEmpty()) {
                    LOGGER.info("Select timezone by GMT: " + tz);
                    format(1, tzSelectionBase, tz).click();
                    selected = true;
                } else {
                    LOGGER.info("Check that timezone by GMT '" + tz + "' is unique.");
                    if (baseTimezoneText.contains(",")) {
                        LOGGER.info("Looks like we have few possible variants for timezone text: " + timezone);
                        multiTimezoneText = true;
                    }
                    if (!multiTimezoneText) {
                        LOGGER.info("Searching for " + timezone);
                        scrolled = MobileUtils.swipeInContainerTillElement(
                                format(1, tzSelectionBase, timezone),
                                scrollableContainer, defaultSwipeTime);
                        if (scrolled) {
                            List<ExtendedWebElement> elements = findExtendedWebElements(By.xpath(String.format(TIMEZONE_TEXT_BASE, tz)), 1);
                            LOGGER.info("Found '" + tz + "' " + elements.size() + " times.");

                            LOGGER.info("Select timezone by TimeZone text: " + timezone);
                            format(1, tzSelectionBase, timezone).click();
                            selected = true;
                        } else {
                            LOGGER.error("Did not find timezone by timezone text: " + timezone);
                            scrolled = MobileUtils.swipeInContainerTillElement(
                                    format(1, tzSelectionBase, tz),
                                    scrollableContainer, defaultSwipeTime);
                            if (scrolled) {
                                LOGGER.info("Select timezone by GMT: " + tz);
                                format(1, tzSelectionBase, tz).click();
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

    @Override
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
    @Override
    public boolean isOpened(long timeout) {
        return title.isElementPresent(timeout);
    }

    @Override
    public boolean isOpened() {
        return isOpened(EXPLICIT_TIMEOUT );
    }
}
