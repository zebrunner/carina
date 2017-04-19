package com.qaprosoft.carina.core.foundation.webdriver.screenshot;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

public class DebugLevelRule implements IScreenshotRule {

    @Override
    public boolean isTakeScreenshot() {
        return Configuration.get(Parameter.CORE_LOG_LEVEL).equalsIgnoreCase("debug");
    }

}
