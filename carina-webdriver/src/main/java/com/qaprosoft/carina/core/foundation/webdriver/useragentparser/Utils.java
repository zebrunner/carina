package com.qaprosoft.carina.core.foundation.webdriver.useragentparser;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Utils {

    public static Map<String, String> browserDictionary = new HashMap<>();

    public static String[] splitVersion(String version) {
        return version.split("/") ;
    }

    public static String getProperBrowserKey(String browserName) {
        return browserDictionary.getOrDefault(browserName, browserName);
    }

    public static String buildBrowserVersion(Optional<String[]> browserVersionParts) {
        return browserVersionParts.get()[0] + "." + browserVersionParts.get()[1];
    }

    public static Boolean isRequiredBrowser(String browser, String auCapabilitie) {
        return auCapabilitie.split("/")[0].contains(browser);
    }

}
