/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation.webdriver.core.capability;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.utils.R;

/**
 * Created by yauhenipatotski on 10/26/15.
 */
public class CapabilitiesLoader {

    private static final Logger LOGGER = Logger.getLogger(CapabilitiesLoader.class);

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void loadCapabilities(String fileName) {
        // TODO: investigate howto allow access to this static method only from internal carina packages

        LOGGER.info("Loading capabilities:");
        Properties props = new Properties();
        URL baseResource = ClassLoader.getSystemResource(fileName);
        try {
            if (baseResource != null) {
                props.load(baseResource.openStream());
                LOGGER.info("Custom capabilities properties loaded: " + fileName);
            } else {
                throw new RuntimeException("Unable to find custom capabilities file '" + fileName + "'!");
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to load custom capabilities from '" + baseResource.getPath() + "'!", e);
        }

        Map<String, String> capabilitiesMap = new HashMap(props);
        for (Map.Entry<String, String> entry : capabilitiesMap.entrySet()) {
            // TODO: investigate effects of removing env args monitoring for extra capabilities declaration
            // String valueFromEnv = null;
            // valueFromEnv = System.getProperty(entry.getKey());
            // String value = (valueFromEnv != null) ? valueFromEnv : entry.getValue();

            String value = entry.getValue();
            String key = entry.getKey();
            LOGGER.info("Set custom property: " + key + "; value: " + value);
            // add each property directly into CONFIG
            R.CONFIG.put(key, value);
        }

    }
}