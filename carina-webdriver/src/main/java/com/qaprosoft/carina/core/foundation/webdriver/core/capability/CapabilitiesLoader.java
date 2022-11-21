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
package com.qaprosoft.carina.core.foundation.webdriver.core.capability;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.utils.R;

/**
 * Created by yauhenipatotski on 10/26/15.
 */
public class CapabilitiesLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Load capabilities and properties from external file into the global CONFIG context.
     * {@code capabilities.<name>=<value> will be attached to each WebDriver capabilities
     * <name>=<value> will override appropriate configuration parameter by new <value>}
     *  
     * @param fileName
     *            String path to the properties file with custom capabilities and properties
     */
    public void loadCapabilities(String fileName) {
        loadCapabilities(fileName, false);
    }
    
    /**
     * Load capabilities and properties from external file into the global or current test CONFIG context.
     * {@code capabilities.<name>=<value> will be attached to each WebDriver capabilities
     * <name>=<value> will override appropriate configuration parameter by new <value>}
     *  
     * @param fileName
     *            String path to the properties file with custom capabilities and properties
     * @param currentTestOnly boolean
     */
    public void loadCapabilities(String fileName, boolean currentTestOnly) {
        LOGGER.info("Loading capabilities to global context from " + fileName);
        Properties props = loadProperties(fileName);

        @SuppressWarnings({ "rawtypes", "unchecked" })
        Map<String, String> capabilitiesMap = new HashMap(props);
        for (Map.Entry<String, String> entry : capabilitiesMap.entrySet()) {
            String value = entry.getValue();
            String key = entry.getKey();
            LOGGER.info("Set custom property: " + key + "; value: " + value);
            // add each property directly into CONFIG
            R.CONFIG.put(key, value, currentTestOnly);
        }
    }
    
    /**
     * Put Capabilities from Zebrunner Agent.
     * 
     * @param caps Capabilities from Zebrunner Agent launcher
     * 
     * @return capabilities MutableCapabilities
     */
    public MutableCapabilities loadCapabilities(Capabilities caps) {
        for (Map.Entry<String, Object> cap : caps.asMap().entrySet()) {
            String key = cap.getKey();
            // so far only primitive String, integer and boolean are supported from Zebrunner Launcher
            String value = cap.getValue().toString(); 
            LOGGER.info("Set custom property: " + key + "; value: " + value);
            // add each property directly into CONFIG
            R.CONFIG.put(SpecialKeywords.CAPABILITIES + "." + key, value);
        }
        
        return (MutableCapabilities) caps;
    }    
    
    /**
     * Generate MutableCapabilities from external file.
     * Only "capabilities.name=value" will be added to the response.
     * 
     * @param fileName String path to the properties file with custom capabilities
     * 
     * @return capabilities MutableCapabilities
     */
    public MutableCapabilities getCapabilities(String fileName) {
        MutableCapabilities capabilities = new MutableCapabilities();
        
        LOGGER.info("Generating capabilities from " + fileName);
        Properties props = loadProperties(fileName);

        final String prefix = SpecialKeywords.CAPABILITIES + ".";
        
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Map<String, String> capabilitiesMap = new HashMap(props);
        for (Map.Entry<String, String> entry : capabilitiesMap.entrySet()) {
            if (entry.getKey().toLowerCase().startsWith(prefix)) {
                String value = entry.getValue();
                if (!value.isEmpty()) {
                    String cap = entry.getKey().replaceAll(prefix, "");
                    if ("false".equalsIgnoreCase(value)) {
                        LOGGER.debug("Set capabilities value as boolean: false");
                        capabilities.setCapability(cap, false);
                    } else if ("true".equalsIgnoreCase(value)) {
                        LOGGER.debug("Set capabilities value as boolean: true");
                        capabilities.setCapability(cap, true);
                    } else {
                        LOGGER.debug("Set capabilities value as string: " + value);
                        capabilities.setCapability(cap, value);
                    }
                }
            }
        }

        return capabilities;
    }
    
    private Properties loadProperties(String fileName) {
        Properties props = new Properties();
        URL baseResource = ClassLoader.getSystemResource(fileName);
        try {
            if (baseResource != null) {
                props.load(baseResource.openStream());
                LOGGER.info("Custom capabilities properties loaded: " + fileName);
            } else {
                Assert.fail("Unable to find custom capabilities file '" + fileName + "'!");
            }
        } catch (Exception e) {
            Assert.fail("Unable to load custom capabilities from '" + baseResource.getPath() + "'!", e);
        }

        return props;
    }
}