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

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.MutablePair;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.commons.SpecialKeywords;

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
        MutableCapabilities options = new MutableCapabilities();
        LOGGER.info("Generating capabilities from '{}'", fileName);
        Properties props = loadProperties(fileName);

        Map<String, String> properties = new HashMap(props);
        Map<String, Object> capabilities = properties.entrySet()
                .stream()
                .filter(entry -> entry.getKey().startsWith("capabilities."))
                .filter(entry -> entry.getValue() != null)
                .map(entry -> {
                    MutablePair<String, String> pair = new MutablePair<>();
                    pair.setLeft(entry.getKey().replaceFirst("capabilities.", ""));
                    pair.setRight(entry.getValue());
                    return pair;
                })
                .map(p -> AbstractCapabilities.parseCapabilityType(p.getLeft(), p.getRight()))
                .collect(Collectors.toMap(MutablePair::getLeft, MutablePair::getRight));

        for (Map.Entry<String, Object> entry : capabilities.entrySet()) {
            List<String> names = Arrays.asList(entry.getKey().split("\\."));

            // TODO add support of any nesting. maybe use some algorithm? or there will never be such nesting
            if (names.isEmpty()) {
                // it should never happens
                throw new RuntimeException("Something went wrong when try to create capabilities from configuration.");
            } else if (names.size() == 1) {
                options.setCapability(names.get(0), entry.getValue());
            } else if (names.size() == 2) {
                HashMap<String, Object> nestCapability = new HashMap<>();
                if (options.getCapability(names.get(0)) != null) {
                    // If we already have inner capability, we think that it is HashMap<String, Object> (custom capabilities)
                    nestCapability = (HashMap<String, Object>) options.getCapability(names.get(0));
                }

                nestCapability.put(names.get(1), entry.getValue());
                options.setCapability(names.get(0), nestCapability);
            } else if (names.size() == 3) {
                HashMap<String, Object> nestCapability = new HashMap<>();
                HashMap<String, Object> secondNestCapability = new HashMap<>();

                if (options.getCapability(names.get(0)) != null) {
                    // If we already have inner capability, we think that it is HashMap<String, Object> (custom capabilities)
                    // todo investigate if we have situations when value that already present is not HashMap
                    nestCapability = (HashMap<String, Object>) options.getCapability(names.get(0));
                    if (nestCapability.containsKey(names.get(1))) {
                        secondNestCapability = (HashMap<String, Object>) nestCapability.get(names.get(1));
                    }
                }
                secondNestCapability.put(names.get(2), entry.getValue());
                nestCapability.put(names.get(1), secondNestCapability);
                options.setCapability(names.get(0), nestCapability);
            } else {
                // Let's hope it won't be needed.
                throw new UnsupportedOperationException("At the moment nesting of more than 3 capabilities is not supported. "
                        + "If you come across a situation in which this is necessary, please notify the Carina Support team.");
            }
        }
        return options;
    }
    
    private Properties loadProperties(String fileName) {
        Properties props = new Properties();
        URL baseResource = ClassLoader.getSystemResource(fileName);
        try {
            if (baseResource != null) {
                try (InputStream istream = baseResource.openStream()) {
                    props.load(istream);
                }
                LOGGER.info("Custom capabilities properties loaded: {}", fileName);
            } else {
                Assert.fail("Unable to find custom capabilities file '" + fileName + "'!");
            }
        } catch (Exception e) {
            Assert.fail("Unable to load custom capabilities from '" + baseResource.getPath() + "'!", e);
        }

        return props;
    }
}