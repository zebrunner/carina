package com.qaprosoft.carina.core.foundation.webdriver.core.capability;

import org.apache.log4j.Logger;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by yauhenipatotski on 10/26/15.
 */
public class CapabilitiesLoder {

    private static final Logger LOGGER = Logger.getLogger(CapabilitiesLoder.class);

    @SuppressWarnings({"rawtypes", "unchecked"})
    public DesiredCapabilities loadCapabilities(String fileName) {

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream config = loader.getResourceAsStream(fileName);

        LOGGER.info("Loading capabilities:");
        DesiredCapabilities capabilities = new DesiredCapabilities();

        Properties props = new Properties();
        try {
            props.load(config);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, String> capabilitiesMap = new HashMap(props);

        for (Map.Entry<String, String> entry : capabilitiesMap.entrySet()) {

            String valueFromEnv = System.getenv(entry.getKey());
            String value = (valueFromEnv != null) ? valueFromEnv : entry.getValue();

            LOGGER.info(entry.getKey() + ": " + value);
            capabilities.setCapability(entry.getKey(), value);

        }

        return capabilities;
    }
}