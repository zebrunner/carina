package com.qaprosoft.carina.core.gui.mobile.devices.common.pages.notifications;

import java.util.List;

import com.qaprosoft.carina.core.gui.mobile.devices.MobileAbstractPage;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;

public abstract class NotificationPageBase extends MobileAbstractPage {

    protected static final Logger LOGGER = Logger.getLogger(NotificationPageBase.class);

    @FindBy(xpath = "//*[@resource-id = 'com.android.systemui:id/notification_stack_scroller' or @resource-id = 'com.android.systemui:id/latestItems']")
    protected ExtendedWebElement title;

    public NotificationPageBase(WebDriver driver) {
        super(driver);

    }

    public abstract boolean isNativeNotificationPage();

    public abstract boolean isClearAllBtnLoaded();

    public abstract int getLastItemsContentSize();

    public abstract String getItemTitle(int num);

    public abstract String getItemText(int num);

    public abstract void tapClearAllBtn();

    /**
     * clearNotifications
     */
    public abstract void clearNotifications();


    /**
     * cleanNotificationByService
     */
    public abstract void cleanNotificationByService();

    /**
     * getAllAvailableNotifications
     *
     * @return List of Notification
     */
    public abstract List<com.qaprosoft.carina.core.foundation.utils.mobile.notifications.android.Notification> getAllAvailableNotifications();

    /**
     * collapseStatusBar
     */
    public abstract void collapseStatusBar();

    /**
     * isStatusBarExpanded
     *
     * @return boolean
     */
    public abstract boolean isStatusBarExpanded();

    public abstract boolean isOpened(long timeout);

    @Override
    public boolean isOpened() {
        return isOpened(LONG_TIMEOUT);
    }


}