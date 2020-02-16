package com.qaprosoft.carina.core.foundation.webdriver.useragentparser.browsers;

import com.qaprosoft.carina.core.foundation.webdriver.useragentparser.Browser;

public class Firefox extends Browser {

    public Firefox(String browser) {
        super(browser);
    }

    @Override public String getUserAgentKey() {
        return getBrowserName();
    }

}
