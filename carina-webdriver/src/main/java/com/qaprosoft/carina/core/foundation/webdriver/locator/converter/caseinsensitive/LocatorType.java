package com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive;

public enum LocatorType {
    XPATH("By.xpath: "), NAME("By.name: "), ID("By.id: "), LINKTEXT("By.linkText: ");

    private LocatorType(String startsWith) {
        this.startsWith = startsWith;
    }

    private String startsWith;

    public String getStartsWith() {
        return startsWith;
    }
}
