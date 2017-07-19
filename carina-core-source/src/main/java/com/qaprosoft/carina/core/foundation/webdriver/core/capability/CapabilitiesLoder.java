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
            if (!entry.getKey().startsWith(SpecialKeywords.CORE)) {
                String valueFromEnv = null;
                valueFromEnv = System.getProperty(entry.getKey());
                String value = (valueFromEnv != null) ? valueFromEnv : entry.getValue();

            	LOGGER.info("Set custom driver capability: " + entry.getKey() + "; value: " + value);
            	capabilities.setCapability(entry.getKey(), value);
            	//add each custom capability to properties generating new key-value pair to be able to change some env specific data later
            	R.CONFIG.put(entry.getKey(), value);
            }
        }

        return capabilities;
    }
}