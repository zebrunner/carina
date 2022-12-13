package com.qaprosoft.carina.core.foundation.webdriver.screenshot;

import com.qaprosoft.carina.core.foundation.webdriver.ScreenshotType;
import com.zebrunner.carina.utils.Configuration;

public class DefaultUnSuccessfulDriverActionScreenshotRule implements IScreenshotRule {

    @Override
    public ScreenshotType getEventType() {
        return ScreenshotType.UNSUCCESSFUL_DRIVER_ACTION;
    }

    @Override
    public boolean isTakeScreenshot() {
        return true;
    }

    @Override
    public boolean isAllowFullSize() {
        // enabled or not full size screenshot on failure/driver exception
        return Configuration.getBoolean(Configuration.Parameter.ALLOW_FULLSIZE_SCREENSHOT);
    }
}
