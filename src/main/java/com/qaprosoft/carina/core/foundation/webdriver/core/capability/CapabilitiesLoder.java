package com.qaprosoft.carina.core.foundation.webdriver.core.capability;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;

/**
 * Created by yauhenipatotski on 10/26/15.
 */
public class CapabilitiesLoder {

    private static final Logger LOGGER = Logger.getLogger(CapabilitiesLoder.class);

    @SuppressWarnings({"rawtypes", "unchecked"})
    public DesiredCapabilities loadCapabilities(String fileName) {

        LOGGER.info("Loading capabilities:");
        Properties props = new Properties();
        URL baseResource = ClassLoader.getSystemResource(fileName);
		try {
			if(baseResource != null)
			{
				props.load(baseResource.openStream());
				LOGGER.info("Custom capabilities properties loaded: " + fileName);
			} else {
				throw new RuntimeException("Unable to find custom capabilities file '" + fileName + "'!");	
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to load custom capabilities from '" + baseResource.getPath() + "'!", e);
		}
		
        DesiredCapabilities capabilities = new DesiredCapabilities();

        Map<String, String> capabilitiesMap = new HashMap(props);
        for (Map.Entry<String, String> entry : capabilitiesMap.entrySet()) {

            String valueFromEnv = null;
            if (!entry.getKey().equalsIgnoreCase("os")) {
            	valueFromEnv = System.getenv(entry.getKey());
            } else {
            	LOGGER.warn("'os' capability can't be loaded from environment as it is default system variable!");
            }
            String value = (valueFromEnv != null) ? valueFromEnv : entry.getValue();

            if (entry.getKey().startsWith(SpecialKeywords.CORE)) {
            	String key = entry.getKey().replaceAll(SpecialKeywords.CORE + ".", "");
            	LOGGER.info("Set custom core property: " + key + "; value: " + value);
            	R.CONFIG.put(key, value);
            } else {
            	LOGGER.info("Set custom driver capability: " + entry.getKey() + "; value: " + value);
            	capabilities.setCapability(entry.getKey(), value);
            }
        }

        return capabilities;
    }
}