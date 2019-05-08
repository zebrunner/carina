/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

public class MobileCapabilies extends AbstractCapabilities {

    @Override
    public DesiredCapabilities getCapability(String testName) {
        DesiredCapabilities capabilities = new DesiredCapabilities();

        // add capabilities based on dynamic _config.properties variables
        capabilities = initCapabilities(capabilities);

        if (R.CONFIG.getBoolean(SpecialKeywords.FULL_RESET_BEFORE_SUITE) && !R.CONFIG.get(SpecialKeywords.FULL_RESET_BEFORE_SUITE).isEmpty()) {
            LOGGER.info("Will be execute 'fullResetBeforeSuite'!");
            executeFullResetBeforeSuite(capabilities);
        }

        return capabilities;
    }

    private void executeFullResetBeforeSuite(DesiredCapabilities caps) {
        if (!caps.getCapability("udid").toString().isEmpty()) {
            String udid = caps.getCapability("udid").toString();
            if (IDriverPool.resetDeviceStatus.isEmpty() || !IDriverPool.resetDeviceStatus.containsKey(udid)) {
                IDriverPool.resetDeviceStatus.put(udid, false);
                caps.setCapability("fullReset", true);
                LOGGER.info("Application will be reset.");
            }
        }
    }
}
