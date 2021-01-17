/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation.utils;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.crypto.CryptoTool;
import com.qaprosoft.carina.core.foundation.exception.InvalidConfigurationException;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

/**
 * R - loads properties from resource files.
 *
 * @author Aliaksei_Khursevich
 *         <a href="mailto:hursevich@gmail.com">Aliaksei_Khursevich</a>
 *
 */
public enum R {
    API("api.properties"),

    CONFIG("config.properties"),

    TESTDATA("testdata.properties"),

    EMAIL("email.properties"),

    REPORT("report.properties"),

    DATABASE("database.properties"),

    ZAFIRA("zafira.properties");

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String OVERRIDE_SIGN = "_";

    private static Pattern CRYPTO_PATTERN = Pattern.compile(SpecialKeywords.CRYPT);

    private String resourceFile;

    // temporary thread/test properties which is cleaned on afterTest phase for current thread. It can override any value from below R enum maps
    private static ThreadLocal<Properties> testProperties = new ThreadLocal<>();

    // permanent global configuration map 
    private static Map<String, Properties> propertiesHolder = new HashMap<>();
    
    // init global configuration map statically
    static {
        reinit();
    }

    public static void reinit() {
        for (R resource : values()) {
            try {
                Properties properties = new Properties();

                URL baseResource = ClassLoader.getSystemResource(resource.resourceFile);
                if (baseResource != null) {
                    properties.load(baseResource.openStream());
                    LOGGER.debug("Base properties loaded: " + resource.resourceFile);
                }

                URL overrideResource;
                String resourceName = OVERRIDE_SIGN + resource.resourceFile;
                while ((overrideResource = ClassLoader.getSystemResource(resourceName)) != null) {
                    properties.load(overrideResource.openStream());
                    LOGGER.debug("Override properties loaded: " + resourceName);
                    resourceName = OVERRIDE_SIGN + resourceName;
                }

                // Overrides properties by systems values
                for (Object key : properties.keySet()) {
                    String systemValue = System.getProperty((String) key);
                    if (!StringUtils.isEmpty(systemValue)) {
                        properties.put(key, systemValue);
                    }
                }

                // init R.CONFIG with default values for required fields
                if (resource.resourceFile.equals("config.properties")) {
                    properties.put(Parameter.ENV_ARG_RESOLVER.getKey(), "com.qaprosoft.carina.core.foundation.utils.DefaultEnvArgResolver");
                    properties.put(Parameter.PROJECT_REPORT_DIRECTORY.getKey(), "./reports");
                    properties.put(Parameter.MAX_LOG_FILE_SIZE.getKey(), "150");
                    properties.put(Parameter.MAX_SCREENSHOOT_HISTORY.getKey(), "0");
                }

                if (resource.resourceFile.contains("config.properties")) {
                    // no need to read env variables using System.getenv()
                    final String prefix = SpecialKeywords.CAPABILITIES + ".";
                    // read all java arguments and redefine capabilities.* items
                    @SuppressWarnings({ "unchecked", "rawtypes" })
                    Map<String, String> javaProperties = new HashMap(System.getProperties());
                    for (Map.Entry<String, String> entry : javaProperties.entrySet()) {
                        String key = entry.getKey();
                        if (key.toLowerCase().startsWith(prefix)) {
                            String value = entry.getValue();
                            if (!StringUtils.isEmpty(value)) {
                                properties.put(key, value);
                            }
                        }
                    }
                }
                propertiesHolder.put(resource.resourceFile, properties);
            } catch (Exception e) {
                throw new InvalidConfigurationException("Invalid config in '" + resource + "': " + e.getMessage());
            }
        }
    }

    R(String resourceKey) {
        this.resourceFile = resourceKey;
    }

    /**
     * Put and update globally value for properties context.
     * 
     * @param key String
     * @param value String
     */
    public void put(String key, String value) {
        put(key, value, false);
    }

    /**
     * Put and update globally or for current test only value for properties context.
     * 
     * @param key String
     * @param value String
     * @param currentTestOnly boolean
     */
    public void put(String key, String value, boolean currentTestOnly) {
        if (currentTestOnly) {
            //declare temporary property key
            LOGGER.warn("Override property for current test '" + key + "=" + value + "'!");
            getTestProperties().put(key, value);
        } else {
            // override globally configuration map property 
            propertiesHolder.get(resourceFile).put(key, value);
        }
    }
    
    /**
     * Verify if key is declared in data map.
     * 
     * @param key name to verify
     * @return boolean
     */
    public boolean containsKey(String key) {
        return propertiesHolder.get(resourceFile).containsKey(key) || getTestProperties().containsKey(key);
    }

    /**
     * Return value either from system properties or config properties context.
     * System properties have higher priority.
     * Decryption is not performed!
     * 
     * @param key Requested key
     * @return config value
     */
    public String get(String key) {
        String value = getTestProperties().getProperty(key);
        if (value != null) {
            LOGGER.warn("Overridden '" + key + "=" + value + "' property will be used for current test!");
            return value;
        }
        
        value = CONFIG.resourceFile.equals(resourceFile) ? PlaceholderResolver.resolve(propertiesHolder.get(resourceFile), key)
                : propertiesHolder.get(resourceFile).getProperty(key);

        // [VD] Decryption is prohibited here otherwise we have plain sensitive information in logs! 

        // [VD] as designed empty MUST be returned
        return value != null ? value : StringUtils.EMPTY;
    }
    
    /**
     * Return decrypted value either from system properties or config properties context.
     * System properties have higher priority.
     * Decryption is performed if required.
     * 
     * @param key Requested key
     * @return config value
     */
    public String getDecrypted(String key) {
        return decrypt(get(key), CRYPTO_PATTERN);
    }

    /**
     * Return Integer value either from system properties or config properties context.
     * 
     * @param key Requested key
     * @return value Integer
     */
    public int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    /**
     * Return long value either from system properties or config properties context.
     * 
     * @param key Requested key
     * @return value long
     */    
    public long getLong(String key) {
        return Long.parseLong(get(key));
    }

    /**
     * Return Double value either from system properties or config properties context.
     * 
     * @param key Requested key
     * @return value Double
     */    
    public double getDouble(String key) {
        return Double.parseDouble(get(key));
    }

    /**
     * Return boolean value either from system properties or config properties context.
     * 
     * @param key Requested key
     * @return value boolean
     */
    public boolean getBoolean(String key) {
        return Boolean.valueOf(get(key));
    }

    public static String getResourcePath(String resource) {
        String path = StringUtils.removeStart(ClassLoader.getSystemResource(resource).getPath(), "/");
        path = StringUtils.replaceChars(path, "/", "\\");
        path = StringUtils.replaceChars(path, "!", "");
        return path;
    }

	public Properties getProperties() {
		Properties globalProp = propertiesHolder.get(resourceFile);
		// Glodal properties will be updated with test specific properties
		if (!getTestProperties().isEmpty()) {
			Properties testProp = testProperties.get();
			LOGGER.debug(String.format("CurrentTestOnly properties has [%s] entries.", testProp.size()));
			LOGGER.debug(testProp.toString());
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Map<String, String> testCapabilitiesMap = new HashMap(testProp);
			testCapabilitiesMap.keySet().stream().forEach(i -> {
				if (globalProp.containsKey(i)) {
					LOGGER.debug(String.format(
							"Global properties already contains key --- %s --- with value --- %s ---. Global property will be overridden by  --- %s --- from test properties.",
							i, globalProp.get(i), testProp.get(i)));
				} else {
					LOGGER.debug(String.format(
							"Global properties isn't contains key --- %s ---.  Global key --- %s --- will be set to --- %s ---  from test properties.",
							i, i, testProp.get(i)));
				}
				globalProp.setProperty(i, (String) testProp.get(i));
			});
		}
		return globalProp;
	}
    
    public void clearTestProperties() {
        testProperties.remove();
    }
    
    public Properties getTestProperties() {
        if (testProperties.get() == null) {
            // init temporary properties at first call
            Properties properties = new Properties();
            testProperties.set(properties);
        }
        
        return testProperties.get();
    }

    private String decrypt(String content, Pattern pattern) {
        try {
            // keep constructor with parametrized CRYPTO_KEY_PATH to run unit tests successfully!
            CryptoTool cryptoTool = new CryptoTool(Configuration.get(Configuration.Parameter.CRYPTO_KEY_PATH));
            return cryptoTool.decryptByPattern(content, pattern);
        } catch (Exception e) {
            LOGGER.error("Error during decrypting '" + content + "'. Please check error: ", e);
            return content;
        }
    }

}
