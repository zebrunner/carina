package com.qaprosoft.carina.core.foundation.webdriver.useragentparser.browsers;

import com.qaprosoft.carina.core.foundation.webdriver.useragentparser.Browser;

public class Safari extends Browser {

    public Safari(String browser) {
        super(browser);
    }

    @Override public String getUserAgentKey() {
        return getBrowserName();
    }

}
