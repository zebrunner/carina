package com.qaprosoft.carina.core.foundation.webdriver.locator;

public enum LocatorType {
    XPATH("By.xpath: "),
    NAME("By.name: "),
    ID("By.id: "),
    LINKTEXT("By.linkText: "),
    CLASSNAME("By.className: "),
    PARTIAL_LINK_TEXT("By.partialLinkText: "),
    CSS("By.cssSelector: "),
    TAG_NAME("By.tagName: "),
    ANDROID_UI_AUTOMATOR("By.AndroidUIAutomator: "),
    IMAGE("By.Image: "),
    ACCESSIBILITY_ID("By.AccessibilityId: ");

    private LocatorType(String startsWith) {
        this.startsWith = startsWith;
    }

    private final String startsWith;

    public String getStartsWith() {
        return startsWith;
    }
}
