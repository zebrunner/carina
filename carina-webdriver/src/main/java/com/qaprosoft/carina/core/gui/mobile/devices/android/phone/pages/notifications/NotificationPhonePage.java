package com.qaprosoft.carina.core.gui.mobile.devices.android.phone.pages.notifications;

import com.qaprosoft.carina.core.foundation.utils.factory.DeviceType;
import com.qaprosoft.carina.core.foundation.utils.factory.DeviceType.Type;
import org.openqa.selenium.WebDriver;

@SuppressWarnings("squid:MaximumInheritanceDepth")
@DeviceType(pageType = Type.ANDROID_PHONE, parentClass = NotificationPage.class)
public class NotificationPhonePage extends NotificationPage{
    public NotificationPhonePage(WebDriver driver){
        super(driver);
    }
}
