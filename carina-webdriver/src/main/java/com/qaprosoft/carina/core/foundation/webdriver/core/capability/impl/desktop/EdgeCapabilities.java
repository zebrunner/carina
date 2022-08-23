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
package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.chromium.ChromiumOptions;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.CapabilityType;

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.AbstractCapabilities;

public class EdgeCapabilities extends AbstractCapabilities<ChromiumOptions<?>> {

    public ChromiumOptions<?> getCapability(String testName) {
        ChromiumOptions<?> capabilities = new ChromiumOptions<>(CapabilityType.BROWSER_NAME, Browser.EDGE.browserName(), "ms:edgeOptions");
        initBaseCapabilities(capabilities, testName);
        addEdgeOptions(capabilities);

        return capabilities;
    }

    private void addEdgeOptions(ChromiumOptions<?> caps) {
        Map<String, Object> prefs = new HashMap<>();
        Map<String, Object> edgeOptions = new HashMap<>();

        boolean needsPrefs = false;

        if (Configuration.getBoolean(Configuration.Parameter.AUTO_DOWNLOAD)) {
            prefs.put("download.prompt_for_download", false);
            if (!"zebrunner".equalsIgnoreCase(R.CONFIG.get("capabilities.provider"))) {
                prefs.put("download.default_directory",
                        ReportContext.getArtifactsFolder().getAbsolutePath());
            }
            needsPrefs = true;
        }

        if (needsPrefs) {
            edgeOptions.put("prefs", prefs);
        }
        caps.setCapability("ms:edgeChrominum", true);
    }

}
