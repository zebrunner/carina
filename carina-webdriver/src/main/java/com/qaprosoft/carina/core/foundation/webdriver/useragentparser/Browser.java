package com.qaprosoft.carina.core.foundation.webdriver.useragentparser;

public abstract class Browser implements IParsable {

    private String browserName;

    public Browser(String browserName) {
        this.browserName = browserName;
    }

    public String getBrowserName() {
        return this.browserName;
    }

    public abstract String getUserAgentKey();

}
