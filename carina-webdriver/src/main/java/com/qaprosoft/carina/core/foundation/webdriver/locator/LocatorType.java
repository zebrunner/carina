package com.qaprosoft.carina.core.foundation.webdriver.locator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;

import com.sun.jersey.core.util.Base64;

import io.appium.java_client.AppiumBy;

public enum LocatorType {
    XPATH("By.xpath: ", false) {
        public By buildLocatorFromString(String locator) {
            return By.xpath(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.xpath(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },

    NAME("By.name: ", false) {
        public By buildLocatorFromString(String locator) {
            return By.name(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.name(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },

    /**
     * For IOS the element name.
     * For Android it is the resource identifier. See {@link AppiumBy.ByName}
     */
    APPIUM_BY_NAME("AppiumBy.name: ", true) {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.name(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return AppiumBy.name(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },

    ID("By.id: ", false) {
        public By buildLocatorFromString(String locator) {
            return By.id(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.id(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },

    /**
     * For IOS the element name.
     * For Android it is the resource identifier. See {@link AppiumBy.ById}
     */
    APPIUM_BY_ID("AppiumBy.id: ", true) {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.id(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return AppiumBy.id(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },

    LINKTEXT("By.linkText: ", false) {
        public By buildLocatorFromString(String locator) {
            return By.linkText(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.linkText(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },
    CLASSNAME("By.className: ", false) {
        public By buildLocatorFromString(String locator) {
            return By.className(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.className(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },

    /**
     * For IOS it is the full name of the XCUI element and begins with XCUIElementType.
     * For Android it is the full name of the UIAutomator2 class (e.g.: android.widget.TextView)
     * see {@link AppiumBy.ByClassName}
     */
    APPIUM_BY_CLASSNAME("AppiumBy.className: ", true) {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.className(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return AppiumBy.className(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },

    PARTIAL_LINK_TEXT("By.partialLinkText: ", false) {
        public By buildLocatorFromString(String locator) {
            return By.partialLinkText(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.partialLinkText(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },
    CSS("By.cssSelector: ", false) {
        public By buildLocatorFromString(String locator) {
            return By.cssSelector(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.cssSelector(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },
    TAG_NAME("By.tagName: ", false) {
        public By buildLocatorFromString(String locator) {
            return By.tagName(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.tagName(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },
    ANDROID_UI_AUTOMATOR("AppiumBy.androidUIAutomator: ", false) {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.androidUIAutomator(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return AppiumBy.androidUIAutomator(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },
    IMAGE("AppiumBy.image: ", false) {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.image(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            String formattedLocator = String.format(StringUtils.remove(locator, getStartsWith()), objects);
            Path path = Paths.get(formattedLocator);
            String base64image;
            try {
                base64image = new String(Base64.encode(Files.readAllBytes(path)));
            } catch (IOException e) {
                throw new RuntimeException(
                        "Error while reading image file after formatting. Formatted locator : " + formattedLocator, e);
            }
            return AppiumBy.image(base64image);
        }
    },
    ACCESSIBILITY_ID("AppiumBy.accessibilityId: ", false) {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.accessibilityId(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return AppiumBy.accessibilityId(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },
    IOS_CLASS_CHAIN("AppiumBy.iOSClassChain: ", false) {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.iOSClassChain(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return AppiumBy.iOSClassChain(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    },
    IOS_NS_PREDICATE("AppiumBy.iOSNsPredicate: ", false) {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.iOSNsPredicateString(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return AppiumBy.iOSNsPredicateString(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }
    };

    LocatorType(String startsWith, boolean isAppium) {
        this.startsWith = startsWith;
        this.isAppium = isAppium;
    }

    private final String startsWith;
    private final boolean isAppium;

    public String getStartsWith() {
        return startsWith;
    }

    public boolean isAppium() {
        return this.isAppium;
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
