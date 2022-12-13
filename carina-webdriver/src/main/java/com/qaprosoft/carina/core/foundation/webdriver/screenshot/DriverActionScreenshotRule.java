package com.qaprosoft.carina.core.foundation.webdriver.screenshot;

import com.qaprosoft.carina.core.foundation.webdriver.ScreenshotType;

public class DriverActionScreenshotRule implements IScreenshotRule {

    @Override
    public ScreenshotType getEventType() {
        return ScreenshotType.SUCCESSFUL_DRIVER_ACTION;
    }

    @Override
    public boolean isTakeScreenshot() {
        return false;
    }

    @Override
    public boolean isAllowFullSize() {
        return false;
    }
}
