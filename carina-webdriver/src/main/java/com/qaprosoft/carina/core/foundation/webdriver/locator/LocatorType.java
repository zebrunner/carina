package com.qaprosoft.carina.core.foundation.webdriver.locator;

public enum LocatorType {
    XPATH("By.xpath: "),
    NAME("By.name: "),
    ID("By.id: "),
    LINKTEXT("By.linkText: "),
    CLASSNAME("By.className: ");

    private LocatorType(String startsWith) {
        this.startsWith = startsWith;
    }

    private String startsWith;

    public String getStartsWith() {
        return startsWith;
    }
}
