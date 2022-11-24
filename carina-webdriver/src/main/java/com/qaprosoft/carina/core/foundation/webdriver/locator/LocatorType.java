package com.qaprosoft.carina.core.foundation.webdriver.locator;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;

import com.zebrunner.carina.utils.exception.NotImplementedException;

import io.appium.java_client.AppiumBy;

public enum LocatorType {
    XPATH("By.xpath: ") {
        public By buildLocatorFromString(String locator) {
            return By.xpath(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.xpath(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },
    NAME("By.name: ") {
        public By buildLocatorFromString(String locator) {
            return By.name(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.name(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },
    ID("By.id: ") {
        public By buildLocatorFromString(String locator) {
            return By.id(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.id(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },
    LINKTEXT("By.linkText: ") {
        public By buildLocatorFromString(String locator) {
            return By.linkText(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.linkText(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },
    CLASSNAME("By.className: ") {
        public By buildLocatorFromString(String locator) {
            return By.className(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.className(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },
    PARTIAL_LINK_TEXT("By.partialLinkText: ") {
        public By buildLocatorFromString(String locator) {
            return By.partialLinkText(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.partialLinkText(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },
    CSS("By.cssSelector: ") {
        public By buildLocatorFromString(String locator) {
            return By.cssSelector(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.cssSelector(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },
    TAG_NAME("By.tagName: ") {
        public By buildLocatorFromString(String locator) {
            return By.tagName(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.tagName(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },
    ANDROID_UI_AUTOMATOR("AppiumBy.androidUIAutomator: ") {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.androidUIAutomator(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return AppiumBy.androidUIAutomator(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },
    IMAGE("By.Image: ") {
        public By buildLocatorFromString(String locator) {
            // todo add realization
            throw new NotImplementedException();
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            // todo add realization
            throw new NotImplementedException();
        }
    },
    ACCESSIBILITY_ID("AppiumBy.AccessibilityId: ") {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.accessibilityId(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return AppiumBy.accessibilityId(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },
    IOS_CLASS_CHAIN("AppiumBy.iOSClassChain: ") {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.iOSClassChain(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return AppiumBy.iOSClassChain(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },
    IOS_NS_PREDICATE("AppiumBy.iOSNsPredicate: ") {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.iOSNsPredicateString(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return AppiumBy.iOSNsPredicateString(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    };

    LocatorType(String startsWith) {
        this.startsWith = startsWith;
    }

    private final String startsWith;

    public String getStartsWith() {
        return startsWith;
    }

    /**
     * Checks if the locator belongs to the current type
     * 
     * @param locator locator as string. You can get it from {@link By#toString()} or {@link AppiumBy#toString()}
     * @return true if the locator belongs to the current type, false otherwise
     */
    public boolean is(String locator) {
        return locator.startsWith(this.startsWith);
    }

    /**
     * Build locator from string. Before usage, check if locator type is current
     * 
     * @param locator locator as string. You can get it from {@link By#toString()} or {@link AppiumBy#toString()}
     * @return {@link By} or {@link AppiumBy}
     */
    public abstract By buildLocatorFromString(String locator);

    /**
     * Build format locator from string. Before usage, check if locator type is current
     * 
     * @param locator locator as format string
     * @param objects arguments referenced by the format specifiers in the format string
     * @return {@link By} or {@link AppiumBy}
     */
    public abstract By buildLocatorFromString(String locator, Object... objects);
}
