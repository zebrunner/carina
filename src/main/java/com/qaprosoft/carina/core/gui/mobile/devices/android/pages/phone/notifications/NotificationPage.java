package com.qaprosoft.carina.core.gui.mobile.devices.android.pages.phone.notifications;

import java.util.List;

import com.qaprosoft.carina.core.foundation.utils.android.AndroidService;
import com.qaprosoft.carina.core.foundation.utils.mobile.notifications.android.Notification;
import com.qaprosoft.carina.core.gui.mobile.devices.common.pages.notifications.NotificationPageBase;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;

import com.qaprosoft.carina.core.foundation.utils.android.AndroidUtils;
import com.qaprosoft.carina.core.foundation.utils.factory.DeviceType;
import com.qaprosoft.carina.core.foundation.utils.factory.DeviceType.Type;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;

import io.appium.java_client.MobileBy;

@DeviceType(pageType = Type.ANDROID_PHONE, parentClass = NotificationPageBase.class)
public class NotificationPage extends NotificationPageBase {

    public NotificationPage(WebDriver driver) {
        super(driver);

        notificationService = AndroidService.getInstance();
    }

    protected AndroidService notificationService;

    protected static final By NOTIFICATION_XPATH = By
            .xpath("//*[@resource-id = 'com.android.systemui:id/"
                    + "notification_stack_scroller']/android.widget.FrameLayout");

    @FindBy(xpath = "//*[@resource-id = 'com.android.systemui:id/notification_stack_scroller' or @resource-id = 'com.android.systemui:id/latestItems']")
    protected ExtendedWebElement title;

    @FindBy(xpath = "//*[@resource-id = 'com.android.systemui:id/notification_stack_scroller']")
    protected ExtendedWebElement notification_scroller;

    @FindBy(xpath = "//*[@resource-id = 'com.android.systemui:id/"
            + "notification_stack_scroller' or @resource-id = 'com.android.systemui:id/latestItems']/*")
    protected List<ExtendedWebElement> notifications;

    @FindBy(xpath = "//*[@resource-id = 'android:id/status_bar_latest_event_content']/*")
    protected List<ExtendedWebElement> notificationsOtherDevices;

    @FindBy(xpath = "//*[@resource-id='com.android.systemui:id/dismiss_text' " +
            "or @resource-id='com.android.systemui:id/clear_all_button']")
    protected ExtendedWebElement dismissBtn;


    //Found stable solution
    @FindBy(id = "com.android.systemui:id/notification_panel")
    private List<ExtendedWebElement> notificationPanel;

    //settings data
    @FindBy(id = "com.android.systemui:id/clear_all_button")
    private List<ExtendedWebElement> clearAllBtn;

    //last items
    @FindBy(id = "com.android.systemui:id/latestItems")
    private List<ExtendedWebElement> lastItemsContainer;

    //events data
    @FindBy(id = "android:id/status_bar_latest_event_content")
    private List<ExtendedWebElement> lastItemsContent;

    @FindBy(id = "android:id/title")
    private List<ExtendedWebElement> itemTitle;

    String itemTitle_Locator_Text = "android:id/title";

    @FindBys({
            @FindBy(id = "android:id/big_text"),
            @FindBy(id = "android:id/text")
    })
    private List<ExtendedWebElement> itemText;

    String itemText_Phone_Locator_Text = "android:id/text";
    String itemText_Tablet_Locator_Text = "android:id/big_text";
    @FindBy(id = "android:id/time")
    private List<ExtendedWebElement> itemTime;


    /**
     * isNativeNotificationPage
     * @return boolean
     */
    @Override
    public boolean isNativeNotificationPage() {
        boolean bool;
        bool = !notificationPanel.isEmpty();
        return bool;
    }

    /**
     * isClearAllBtnLoaded
     * @return boolean
     */
    @Override
    public boolean isClearAllBtnLoaded() {
        boolean bool;
        bool = !clearAllBtn.isEmpty();
        return bool;
    }

    /**
     * getLastItemsContentSize
     * @return int
     */
    @Override
    public int getLastItemsContentSize() {
        return lastItemsContent.size();
    }


    /**
     * getItemTitle
     * @param num notification number
     * @return String
     */
    @Override
    public String getItemTitle(int num) {
        try {
            return lastItemsContent.get(num).findExtendedWebElement(By.id(itemTitle_Locator_Text)).getText();
        } catch (Exception e) {
            LOGGER.info("Can't get notification title. Exception: " + e);
            return "";
        }
    }

    /**
     * getItemText
     * @param num notification number
     * @return String
     */
    @Override
    public String getItemText(int num) {
        //TODO: Check on Tablet
        //if (isPhone())
        try {
            LOGGER.info("Visible text:" + lastItemsContent.get(num).findExtendedWebElements(MobileBy.className("android.widget.TextView")).size());
            if (true)
                return lastItemsContent.get(num).findExtendedWebElements(MobileBy.className("android.widget.TextView")).get(2).getText();
            else {
                if (lastItemsContent.get(num).findExtendedWebElement(MobileBy.id(itemText_Tablet_Locator_Text)).isElementNotPresent(1)) {
                    return lastItemsContent.get(num).findExtendedWebElement(MobileBy.id(itemText_Phone_Locator_Text)).getText();
                } else {
                    return lastItemsContent.get(num).findExtendedWebElement(MobileBy.id(itemText_Tablet_Locator_Text)).getText();
                }
            }
        } catch (Exception e) {
            LOGGER.info("Can't get notification text. Exception: " + e);
            return "";
        }
    }

    @Override
    public void tapClearAllBtn() {
        clearAllBtn.get(0).click();
    }

    /*public MessagesPage tapLastItemsContent(int num) {
        tapElement(lastItemsContainer.get(num));
        return new MessagesPage(driver);
    }

    public MessagesPage tapItemTitle(int num) {
        tapElement(lastItemsContent.get(num));
        return new MessagesPage(driver);
    }*/

    /**
     * clearNotifications
     */
    @Override
    public void clearNotifications() {
        if (!isOpened(1)) {
            notificationService.expandStatusBar();
        }
        if (dismissBtn.isElementPresent(SHORT_TIMEOUT)) {
            LOGGER.info("Dismiss all notifications btn is present.");
            dismissBtn.click();
        } else {
            LOGGER.info("Dismiss all notifications btn isn't present. Attempt to clear all notifications manually");
            LOGGER.debug("Notifications page source: ".concat(getDriver().getPageSource()));
            int x1, x2, y1, y2;
            Point point;
            Dimension dim;
            List<ExtendedWebElement> notificationList;
            if (notifications.size() > 0) {
                notificationList = notifications;
            } else {
                notificationList = notificationsOtherDevices;
            }
            LOGGER.info("Visible Notifications size:" + notificationList.size());
            for (ExtendedWebElement notification : notificationList) {
                point = notification.getElement().getLocation();
                dim = notification.getElement().getSize();
                x1 = point.x + dim.width / 6;
                x2 = point.x + dim.width * 5 / 6;
                y1 = y2 = point.y + dim.height / 2;
                AndroidUtils
                        .swipeCoord(x1, y1, x2, y2, SWIPE_DURATION);
            }
        }
    }


    /**
     * cleanNotificationByService
     */
    @Override
    public void cleanNotificationByService() {
        notificationService.clearNotifications();
    }


    /**
     * getAllAvailableNotifications
     *
     * @return List of Notification
     */
    @Override
    public List<Notification> getAllAvailableNotifications() {
        LOGGER.info("Android device");
        List<Notification> list = notificationService.getNotifications();
        return list;
    }

    /**
     * collapseStatusBar
     */
    @Override
    public void collapseStatusBar() {
        LOGGER.info("Android device");
        notificationService.collapseStatusBar();
    }

    /**
     * isStatusBarExpanded
     *
     * @return boolean
     */
    @Override
    public boolean isStatusBarExpanded() {
        LOGGER.info("Android device");
        notificationService.expandStatusBar();
        return isOpened(DELAY);
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
        return isOpened(LONG_TIMEOUT);
    }
}
