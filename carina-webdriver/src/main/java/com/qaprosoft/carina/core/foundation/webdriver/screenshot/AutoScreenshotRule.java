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

import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.Configuration.Parameter;

public class AutoScreenshotRule implements IScreenshotRule {
    @Override
    public boolean isTakeScreenshot() {
        // enabled or not screenshot generation for driver action or test failure
        return Configuration.getBoolean(Parameter.AUTO_SCREENSHOT) || Configuration.getBoolean(Parameter.ERROR_SCREENSHOT);
    }

    @Override
    public boolean isAllowFullSize() {
        // enabled or not full size screenshot on failure/driver exception 
        return Configuration.getBoolean(Parameter.ALLOW_FULLSIZE_SCREENSHOT);
    }
}
