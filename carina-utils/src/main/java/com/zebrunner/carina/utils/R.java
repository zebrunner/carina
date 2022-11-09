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
package com.zebrunner.carina.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.SkipException;

import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.utils.exception.InvalidConfigurationException;
import com.zebrunner.carina.crypto.Algorithm;
import com.zebrunner.carina.crypto.CryptoTool;
import com.zebrunner.carina.crypto.CryptoToolBuilder;

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

    ZAFIRA("zafira.properties"),

    AGENT("agent.properties");

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String OVERRIDE_SIGN = "_";

    private String resourceFile;

    // temporary thread/test properties which is cleaned on afterTest phase for current thread. It can override any value from below R enum maps
    private static ThreadLocal<Properties> testProperties = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, String>> PROPERTY_OVERWRITE_NOTIFICATIONS = new ThreadLocal<>();

    private static Map<String, Properties> defaultPropertiesHolder = new HashMap<>();
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

                if (isResourceExists(resource.resourceFile)) {
                    Properties collectedProperties = collect(resource.resourceFile);
                    properties.putAll(collectedProperties);
                    defaultPropertiesHolder.put(resource.resourceFile, collectedProperties);
                }

                URL overrideResource;
                String resourceName = OVERRIDE_SIGN + resource.resourceFile;
                while ((overrideResource = ClassLoader.getSystemResource(resourceName)) != null) {
                    properties.load(overrideResource.openStream());
                    resourceName = OVERRIDE_SIGN + resourceName;
                }

                // Overrides properties by env variables
                for (Object key : properties.keySet()) {
                    String systemValue = System.getenv((String) key);
                    if (!StringUtils.isEmpty(systemValue)) {
                        properties.put(key, systemValue);
                    }
                }
                
                // Overrides properties by systems properties (java arguments)
                for (Object key : properties.keySet()) {
                    String systemValue = System.getProperty((String) key);
                    if (!StringUtils.isEmpty(systemValue)) {
                        properties.put(key, systemValue);
                    }
                }

                // init R.CONFIG with default values for required fields
                if (resource.resourceFile.equals("config.properties")) {
                    if (!CONFIG.isInit(Configuration.Parameter.PROJECT_REPORT_DIRECTORY,properties)) {
                        properties.put(Configuration.Parameter.PROJECT_REPORT_DIRECTORY.getKey(), "./reports");
                    }
                    if (!CONFIG.isInit(Configuration.Parameter.MAX_SCREENSHOOT_HISTORY,properties)) {
                        properties.put(Configuration.Parameter.MAX_SCREENSHOOT_HISTORY.getKey(), "10");
                    }
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
                            if (!StringUtils.isEmpty(value) && !value.equalsIgnoreCase(SpecialKeywords.NULL)) {
                                properties.put(key, value);
                            }
                        }
                    }
                    // delete all empty or null capabilites.* items from properties
                    for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                        String key = (String) entry.getKey();
                        String value = (String) entry.getValue();
                        if (key.toLowerCase().startsWith(prefix)) {
                            if (StringUtils.isBlank(value) || value.equalsIgnoreCase(SpecialKeywords.NULL)) {
                                properties.remove(key, value);
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

    /**
     * Checks for the presence of at least one resource in the classpath
     * 
     * @param resourceName the name of the resource being searched for
     * @return true if at least one resource found, false otherwise
     */
    private static boolean isResourceExists(String resourceName) {
        return ClassLoader.getSystemResource(resourceName) != null;
    }

    /**
     * Collect all properties with the same name into a single Properties object
     * 
     * @param resourceName resource name, for example config.properties
     * @return collected properties
     */
    private static Properties collect(String resourceName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        Properties assembledProperties = new Properties();
            Enumeration<URL> resourceURLs = classLoader.getResources(resourceName);
            while (resourceURLs.hasMoreElements()) {
                Properties tempProperties = new Properties();
                URL url = resourceURLs.nextElement();

                try (InputStream stream = url.openStream()) {
                    tempProperties.load(stream);
                    assembledProperties.putAll(tempProperties);
                }
            }
        return assembledProperties;
    }

    private boolean isInit(Configuration.Parameter parameter, Properties properties){
        String value = (String) properties.get(parameter.getKey());
        return !(value == null || value.length() == 0 || value.equals("NULL"));
    }

    R(String resourceKey) {
        this.resourceFile = resourceKey;
    }

    /**
     * Compares the current value of property with the default value
     * 
     * @param key name of property
     * @return true if current value equals default value, otherwise false
     */
    public boolean isOverwritten(String key) {
        String currentValue = get(key);
        String defaultValue = defaultPropertiesHolder.get(resourceFile).getProperty(key);
        if (defaultValue == null) {
            defaultValue = StringUtils.EMPTY;
        }

        return !currentValue.equals(defaultValue);
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
            // do not warn user about this system property update
            if (!Configuration.Parameter.ERROR_SCREENSHOT.getKey().equals(key)) {
                LOGGER.warn("Override property for current test '" + key + "=" + value + "'!");
            }
            //declare temporary property key
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
            if (PROPERTY_OVERWRITE_NOTIFICATIONS.get() == null) {
                PROPERTY_OVERWRITE_NOTIFICATIONS.set(new HashMap<>());
            }
            // do not warn user about this system property update
            if (!Configuration.Parameter.ERROR_SCREENSHOT.getKey().equals(key) &&
                    !(PROPERTY_OVERWRITE_NOTIFICATIONS.get().containsKey(key) &&
                            value.equals(PROPERTY_OVERWRITE_NOTIFICATIONS.get().get(key)))) {
                LOGGER.warn("Overridden '{}={}' property will be used for current test!", key, value);
                PROPERTY_OVERWRITE_NOTIFICATIONS.get().put(key, value);
            }
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
        return decrypt(get(key), Configuration.get(Configuration.Parameter.CRYPTO_PATTERN));
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
        PROPERTY_OVERWRITE_NOTIFICATIONS.remove();
    }
    
    public Properties getTestProperties() {
        if (testProperties.get() == null) {
            // init temporary properties at first call
            Properties properties = new Properties();
            testProperties.set(properties);
        }
        
        return testProperties.get();
    }

    private String decrypt(String content, String pattern) {
        Matcher cryptoMatcher = Pattern.compile(pattern)
                .matcher(content);

        if (cryptoMatcher.find()) {
            try {
                String cryptoKey = Configuration.get(Configuration.Parameter.CRYPTO_KEY_VALUE);
                if (cryptoKey.isEmpty()) {
                    throw new SkipException("Encrypted data detected, but the crypto key is not found!");
                }

                CryptoTool cryptoTool = CryptoToolBuilder.builder()
                        .chooseAlgorithm(Algorithm.find(Configuration.get(Configuration.Parameter.CRYPTO_ALGORITHM)))
                        .setKey(cryptoKey)
                        .build();

                return cryptoTool.decrypt(content, pattern);
            } catch (Exception e) {
                LOGGER.error("Error during decrypting '" + content + "'. Please check error: ", e);
            }
        }
        return content;
    }

}
