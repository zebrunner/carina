/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
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
package com.qaprosoft.carina.core.foundation.webdriver.screenshot;

import com.qaprosoft.carina.core.foundation.webdriver.ScreenshotType;
import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.Configuration.Parameter;

/**
 * Screenshot rule for capturing screenshots if debug log level enabled.
 * Not used by default in Carina Framework
 */
public class DebugLevelScreenshotRule implements IScreenshotRule {

    @Override
    public ScreenshotType getEventType() {
        return ScreenshotType.SUCCESSFUL_DRIVER_ACTION;
    }

    @Override
    public boolean isTakeScreenshot() {
        return Configuration.get(Parameter.CORE_LOG_LEVEL).equalsIgnoreCase("debug");
    }
    
    @Override
    public boolean isAllowFullSize() {
        return Configuration.get(Parameter.CORE_LOG_LEVEL).equalsIgnoreCase("debug");
    }

}
