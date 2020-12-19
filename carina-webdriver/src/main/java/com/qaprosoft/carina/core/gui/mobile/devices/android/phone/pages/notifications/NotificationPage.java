/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.gui.mobile.devices.android.phone.pages.notifications;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.utils.android.AndroidService;
import com.qaprosoft.carina.core.foundation.utils.android.IAndroidUtils;
import com.qaprosoft.carina.core.foundation.utils.factory.DeviceType;
import com.qaprosoft.carina.core.foundation.utils.mobile.notifications.android.Notification;
import com.qaprosoft.carina.core.foundation.webdriver.DriverHelper;
import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.gui.mobile.devices.MobileAbstractPage;

import io.appium.java_client.MobileBy;

public class NotificationPage extends MobileAbstractPage implements IAndroidUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public NotificationPage(WebDriver driver) {
        super(driver);
        notificationService = AndroidService.getInstance();
    }

    private AndroidService notificationService;

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

    @FindBy(xpath = "//*[@resource-id='com.android.systemui:id/clear_all' " +
            "or @resource-id='com.android.systemui:id/clear_all_button' " +
            "or @resource-id='com.android.systemui:id/dismiss_text']")
    protected ExtendedWebElement dismissBtn;

    // Found stable solution
    @FindBy(id = "com.android.systemui:id/notification_panel")
    private List<ExtendedWebElement> notificationPanel;

    // settings data
    @FindBy(id = "com.android.systemui:id/clear_all_button")
    private List<ExtendedWebElement> clearAllBtn;

    // last items
    @FindBy(id = "com.android.systemui:id/latestItems")
    private List<ExtendedWebElement> lastItemsContainer;

    // events data
    @FindBy(id = "android:id/status_bar_latest_event_content")
    private List<ExtendedWebElement> lastItemsContent;

    @FindBy(id = "android:id/title")
    private List<ExtendedWebElement> itemTitle;

    String itemTitle_Locator_Text = "android:id/title";

    @FindAll({
            @FindBy(id = "android:id/big_text"),
            @FindBy(id = "android:id/text")
    })
    private List<ExtendedWebElement> itemTextList;

    @FindBy(xpath = "//*[@text='%s']")
    private ExtendedWebElement textItem;

    String itemText_Phone_Locator_Text = "android:id/text";
    String itemText_Tablet_Locator_Text = "android:id/big_text";
    @FindBy(id = "android:id/time")
    private List<ExtendedWebElement> itemTime;

    /**
     * isNativeNotificationPage
     *
     * @return boolean
     */
    public boolean isNativeNotificationPage() {
        boolean bool;
        bool = !notificationPanel.isEmpty();
        return bool;
    }

    /**
     * isClearAllBtnLoaded
     *
     * @return boolean
     */
    public boolean isClearAllBtnLoaded() {
        boolean bool;
        bool = !clearAllBtn.isEmpty();
        return bool;
    }

    /**
     * getLastItemsContentSize
     *
     * @return int
     */
    public int getLastItemsContentSize() {
        return lastItemsContent.size();
    }

    /**
     * getItemTitle
     *
     * @param num notification number
     * @return String
     */
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
     *
     * @param num notification number
     * @return String
     */
    public String getItemText(int num) {
        try {
            LOGGER.info("Visible text:" + lastItemsContent.get(num).findExtendedWebElements(MobileBy.className("android.widget.TextView")).size());
            if (IDriverPool.getDefaultDevice().getDeviceType() == DeviceType.Type.ANDROID_TABLET) {
                try {
                    if (lastItemsContent.get(num).findExtendedWebElement(MobileBy.id(itemText_Tablet_Locator_Text)).isElementNotPresent(1)) {
                        return lastItemsContent.get(num).findExtendedWebElement(MobileBy.id(itemText_Phone_Locator_Text)).getText();
                    } else {
                        return lastItemsContent.get(num).findExtendedWebElement(MobileBy.id(itemText_Tablet_Locator_Text)).getText();
                    }
                } catch (Exception err) {
                    LOGGER.error("Issue for getting notifications on Tablet.", err);
                    return lastItemsContent.get(num).findExtendedWebElements(MobileBy.className("android.widget.TextView")).get(2).getText();
                }
            } else {
                return lastItemsContent.get(num).findExtendedWebElements(MobileBy.className("android.widget.TextView")).get(2).getText();
            }
        } catch (Exception e) {
            LOGGER.info("Can't get notification text. Exception: ", e);
            return "";
        }
    }

    public void tapClearAllBtn() {
        clearAllBtn.get(0).click();
    }

    /*
     * public MessagesPage tapLastItemsContent(int num) {
     * tapElement(lastItemsContainer.get(num));
     * return new MessagesPage(driver);
     * }
     * 
     * public MessagesPage tapItemTitle(int num) {
     * tapElement(lastItemsContent.get(num));
     * return new MessagesPage(driver);
     * }
     */

    /**
     * clearNotifications
     */
    public void clearNotifications() {
        if (!isOpened(1)) {
            notificationService.expandStatusBar();
        }

        try{
            swipe(dismissBtn, notification_scroller);
            if(dismissBtn.getAttribute("enabled").equals("true")) {
                LOGGER.info("Clicking 'Dismiss All Notifications' button...");
                dismissBtn.click();
            } else {
                LOGGER.info("'Dismiss All Notifications' Button is present but disabled, meaning any alerts displayed are not closable. Collapsing tray...");
                pressBack();
            }
        } catch (AssertionError e){
            LOGGER.info("Device tray closed by swiping which means no notifications were present. Proceeding with test.");
        }
    }

    /**
     * cleanNotificationByService
     */
    public void cleanNotificationByService() {
        notificationService.clearNotifications();
    }

    /**
     * getAllAvailableNotifications
     *
     * @return List of Notification
     */
    public List<Notification> getAllAvailableNotifications() {
        List<Notification> list = notificationService.getNotifications();
        return list;
    }

    /**
     * collapseStatusBar
     */
    public void collapseStatusBar() {
        notificationService.collapseStatusBar();
    }

    /**
     * isStatusBarExpanded
     *
     * @return boolean
     */
    public boolean isStatusBarExpanded() {
        notificationService.expandStatusBar();
        return isOpened(DELAY);
    }

    public void clickAlert(String alertText){
        isStatusBarExpanded();
        textItem.format(alertText).click();
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
        return isOpened(DriverHelper.EXPLICIT_TIMEOUT);
    }

}