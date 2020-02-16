package com.qaprosoft.carina.core.foundation.webdriver.useragentparser.browsers;

import com.qaprosoft.carina.core.foundation.webdriver.useragentparser.Browser;

public class IExplorer extends Browser {

    private static final String USER_AGENT_KEY = "MSIE";

    public IExplorer(String browser) {
        super(browser);
    }

    @Override public String getUserAgentKey() {
        return USER_AGENT_KEY;
    }

    public String getBrowserVersion(String browserName, String userAgent) {
        String[] version = userAgent.split(" ");
        for (int i = 1; i < version.length-1; i++) {
            if (version[i].equalsIgnoreCase(getUserAgentKey())) {
                return version[i+1].replaceAll("[^\\d.]", "");
            }
        }
        return null;
    }

}
