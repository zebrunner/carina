package com.qaprosoft.carina.core.foundation.webdriver.core.capability;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.browsermobproxy.ProxyPool;
import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.proxy.SystemProxy;

import io.appium.java_client.remote.options.SupportsBrowserNameOption;

public abstract class AbstractCapabilities<T extends MutableCapabilities> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String CAPABILITIES_PREFIX = SpecialKeywords.CAPABILITIES + ".";
    private static final List<String> numericCaps = Arrays.asList("idleTimeout", "waitForIdleTimeout");

    /**
     * if customCapabilities equals null, return capabilities from configuration file, otherwise returns
     * customCapabilities
     */
    public abstract T getCapabilities(Capabilities customCapabilities);


    protected void setCapabilities(T options, Capabilities capabilities) {
        if (capabilities == null) {
            return;
        }
        for (String capabilityName : capabilities.getCapabilityNames()) {
            options.setCapability(capabilityName, capabilities.getCapability(capabilityName));
        }
    }

    protected Capabilities getBrowserConfigurationCapabilities() {
        MutableCapabilities capabilities = new MutableCapabilities();

        if (isProxyConfigurationAvailable()) {
            capabilities.setCapability(CapabilityType.PROXY, setupProxy());
        }
        return capabilities;
    }

    /**
     * Read all properties which starts from "capabilities.*" prefix and add return them as Capabilities object
     */
    public static MutableCapabilities getConfigurationCapabilities() {
        MutableCapabilities capabilities = new MutableCapabilities();
        Properties properties = R.CONFIG.getProperties();

        Set<String> capabilitiesNames = R.CONFIG.getProperties()
                .stringPropertyNames()
                .stream()
                .filter((propertyName) -> propertyName.startsWith(CAPABILITIES_PREFIX))
                .map(capability -> capability.replace(CAPABILITIES_PREFIX, ""))
                .collect(Collectors.toSet());

        for (String capabilityName : capabilitiesNames) {
            String value = properties.getProperty(CAPABILITIES_PREFIX + capabilityName);

            if (value.isEmpty()) {
                continue;
            }

            if (numericCaps.contains(capabilityName) && isNumber(value)) {
                LOGGER.debug("Adding {} to capabilities as integer", capabilityName);
                capabilities.setCapability(capabilityName, Integer.parseInt(value));
                continue;
            }

            if ("false".equalsIgnoreCase(value)) {
                capabilities.setCapability(capabilityName, false);
                continue;
            }

            if ("true".equalsIgnoreCase(value)) {
                capabilities.setCapability(capabilityName, true);
                continue;
            }

            capabilities.setCapability(capabilityName, value);
        }

        // browser can be provided not only as capability, but also as configuration parameter
        String browserName = Configuration.getBrowser();
        if (!Objects.equals(browserName, StringUtils.EMPTY)) {
            capabilities.setCapability(SupportsBrowserNameOption.BROWSER_NAME_OPTION, Configuration.getBrowser());
        }
        return capabilities;
    }

    protected static boolean isNumber(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        try {
            Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return false;
        }

        return true;
    }

    private static boolean isProxyConfigurationAvailable() {
        String proxyHost = Configuration.get(Configuration.Parameter.PROXY_HOST);
        String proxyPort = Configuration.get(Configuration.Parameter.PROXY_PORT);
        if (Configuration.get(Configuration.Parameter.BROWSERMOB_PROXY).equals("true")) {
            proxyPort = Integer.toString(ProxyPool.getProxyPortFromThread());
        }
        return proxyHost != null && !proxyHost.isEmpty() && proxyPort != null && !proxyPort.isEmpty();
    }

    private static Proxy setupProxy() {
        ProxyPool.setupBrowserMobProxy();
        SystemProxy.setupProxy();

        String proxyHost = Configuration.get(Configuration.Parameter.PROXY_HOST);
        String proxyPort = Configuration.get(Configuration.Parameter.PROXY_PORT);
        String noProxy = Configuration.get(Configuration.Parameter.NO_PROXY);

        if (Configuration.get(Configuration.Parameter.BROWSERMOB_PROXY).equals("true")) {
            proxyPort = Integer.toString(ProxyPool.getProxyPortFromThread());
        }
        List<String> protocols = Arrays.asList(Configuration.get(Configuration.Parameter.PROXY_PROTOCOLS).split("[\\s,]+"));

        // TODO: test removal comparing with null
        if (proxyHost != null && !proxyHost.isEmpty() && proxyPort != null && !proxyPort.isEmpty()) {

            org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
            String proxyAddress = String.format("%s:%s", proxyHost, proxyPort);

            if (protocols.contains("http")) {
                LOGGER.info(String.format("Http proxy will be set: %s:%s", proxyHost, proxyPort));
                proxy.setHttpProxy(proxyAddress);
            }

            if (protocols.contains("https")) {
                LOGGER.info(String.format("Https proxy will be set: %s:%s", proxyHost, proxyPort));
                proxy.setSslProxy(proxyAddress);
            }

            if (protocols.contains("ftp")) {
                LOGGER.info(String.format("FTP proxy will be set: %s:%s", proxyHost, proxyPort));
                proxy.setFtpProxy(proxyAddress);
            }

            if (protocols.contains("socks")) {
                LOGGER.info(String.format("Socks proxy will be set: %s:%s", proxyHost, proxyPort));
                proxy.setSocksProxy(proxyAddress);
            }

            if (!noProxy.isEmpty()) {
                proxy.setNoProxy(noProxy);
            }

            return proxy;
        }

        return null;
    }
}
