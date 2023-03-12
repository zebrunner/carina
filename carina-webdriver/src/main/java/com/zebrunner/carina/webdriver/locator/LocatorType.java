package com.zebrunner.carina.webdriver.locator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;

import com.sun.jersey.core.util.Base64;

import io.appium.java_client.AppiumBy;

public enum LocatorType {

    BY_XPATH("By.xpath: ", true) {
        public By buildLocatorFromString(String locator) {
            return By.xpath(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.xpath(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }

        public By buildLocatorWithIndex(String locator, int index) {
            // xpath index starts from 1
            return By.xpath(String.format("(%s)[%s]", StringUtils.remove(locator, getStartsWith()), index + 1));
        }
    },

    BY_NAME("By.name: ", false) {
        public By buildLocatorFromString(String locator) {
            return By.name(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.name(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }

        public By buildLocatorWithIndex(String locator, int index) {
            throw new UnsupportedOperationException("Building locator 'By.name' with index is not supported");
        }
    },

    APPIUM_BY_NAME("AppiumBy.name: ", false) {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.name(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return AppiumBy.name(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }

        public By buildLocatorWithIndex(String locator, int index) {
            throw new UnsupportedOperationException("Building locator 'AppiumBy.name' with index is not supported");
        }
    },

    BY_ID("By.id: ", false) {

        public By buildLocatorFromString(String locator) {
            return By.id(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.id(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }

        public By buildLocatorWithIndex(String locator, int index) {
            throw new UnsupportedOperationException("Building locator 'By.id' with index is not supported");
        }
    },

    APPIUM_BY_ID("AppiumBy.id: ", false) {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.id(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return AppiumBy.id(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }

        public By buildLocatorWithIndex(String locator, int index) {
            throw new UnsupportedOperationException("Building locator 'AppiumBy.id' with index is not supported");
        }
    },

    BY_LINKTEXT("By.linkText: ", false) {
        public By buildLocatorFromString(String locator) {
            return By.linkText(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.linkText(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }

        public By buildLocatorWithIndex(String locator, int index) {
            throw new UnsupportedOperationException("Building locator 'By.linkText' with index is not supported");
        }
    },

    BY_CLASSNAME("By.className: ", false) {
        public By buildLocatorFromString(String locator) {
            return By.className(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.className(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }

        public By buildLocatorWithIndex(String locator, int index) {
            throw new UnsupportedOperationException("Building locator 'By.className' with index is not supported");
        }
    },

    APPIUM_BY_CLASSNAME("AppiumBy.className: ", false) {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.className(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return AppiumBy.className(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }

        public By buildLocatorWithIndex(String locator, int index) {
            throw new UnsupportedOperationException("Building locator 'AppiumBy.className' with index is not supported");
        }
    },

    BY_PARTIAL_LINK_TEXT("By.partialLinkText: ", false) {
        public By buildLocatorFromString(String locator) {
            return By.partialLinkText(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.partialLinkText(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }

        public By buildLocatorWithIndex(String locator, int index) {
            throw new UnsupportedOperationException("Building locator 'By.partialLinkText' with index is not supported");
        }
    },

    BY_CSS("By.cssSelector: ", false) {
        public By buildLocatorFromString(String locator) {
            return By.cssSelector(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.cssSelector(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }

        public By buildLocatorWithIndex(String locator, int index) {
            // index starts from 1
            throw new UnsupportedOperationException("Building locator 'By.cssSelector' with index is not supported");
        }
    },

    BY_TAG_NAME("By.tagName: ", false) {
        public By buildLocatorFromString(String locator) {
            return By.tagName(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return By.tagName(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }

        public By buildLocatorWithIndex(String locator, int index) {
            throw new UnsupportedOperationException("Building locator 'By.tagName' with index is not supported");
        }
    },

    APPIUM_BY_ANDROID_UI_AUTOMATOR("AppiumBy.androidUIAutomator: ", false) {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.androidUIAutomator(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return AppiumBy.androidUIAutomator(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }

        public By buildLocatorWithIndex(String locator, int index) {
            throw new UnsupportedOperationException("Building locator 'AppiumBy.androidUIAutomator' with index is not supported");
        }
    },

    APPIUM_BY_IMAGE("AppiumBy.image: ", false) {
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

        public By buildLocatorWithIndex(String locator, int index) {
            throw new UnsupportedOperationException("Building locator 'AppiumBy.image' with index is not supported");
        }
    },

    APPIUM_BY_ACCESSIBILITY_ID("AppiumBy.accessibilityId: ", false) {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.accessibilityId(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return AppiumBy.accessibilityId(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }

        public By buildLocatorWithIndex(String locator, int index) {
            throw new UnsupportedOperationException("Building locator 'AppiumBy.accessibilityId' with index is not supported");
        }
    },

    APPIUM_BY_IOS_CLASS_CHAIN("AppiumBy.iOSClassChain: ", false) {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.iOSClassChain(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return AppiumBy.iOSClassChain(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }

        public By buildLocatorWithIndex(String locator, int index) {
            throw new UnsupportedOperationException("Building locator 'AppiumBy.iOSClassChain' with index is not supported");
        }
    },

    APPIUM_BY_IOS_NS_PREDICATE("AppiumBy.iOSNsPredicate: ", false) {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.iOSNsPredicateString(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return AppiumBy.iOSNsPredicateString(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }

        public By buildLocatorWithIndex(String locator, int index) {
            throw new UnsupportedOperationException("Building locator 'AppiumBy.iOSNsPredicate' with index is not supported");
        }
    },
    
    APPIUM_BY_CUSTOM("AppiumBy.custom: ", false) {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.custom(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return AppiumBy.custom(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }

        public By buildLocatorWithIndex(String locator, int index) {
            throw new UnsupportedOperationException("Building locator 'AppiumBy.custom' with index is not supported");
        }
    },
    
    APPIUM_BY_ANDROID_VIEW_TAG("AppiumBy.androidViewTag: ", false) {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.androidViewTag(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return AppiumBy.androidViewTag(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }

        public By buildLocatorWithIndex(String locator, int index) {
            throw new UnsupportedOperationException("Building locator 'AppiumBy.androidViewTag' with index is not supported");
        }
    },
    
    APPIUM_BY_ANDROID_VIEW_MATCHER("AppiumBy.androidViewMatcher: ", false) {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.androidViewMatcher(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return AppiumBy.androidViewMatcher(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }

        public By buildLocatorWithIndex(String locator, int index) {
            throw new UnsupportedOperationException("Building locator 'AppiumBy.androidViewMatcher' with index is not supported");
        }
    },
    
    APPIUM_BY_ANDROID_DATA_MATCHER("AppiumBy.androidDataMatcher: ", false) {
        public By buildLocatorFromString(String locator) {
            return AppiumBy.androidDataMatcher(StringUtils.remove(locator, getStartsWith()));
        }

        public By buildLocatorFromString(String locator, Object... objects) {
            return AppiumBy.androidDataMatcher(String.format(StringUtils.remove(locator, getStartsWith()), objects));
        }

        public By buildLocatorWithIndex(String locator, int index) {
            throw new UnsupportedOperationException("Building locator 'AppiumBy.androidDataMatcher' with index is not supported");
        }
    };

    LocatorType(String startsWith, boolean isIndexSupport) {
        this.startsWith = startsWith;
        this.isIndexSupport = isIndexSupport;
    }

    private final String startsWith;
    private final boolean isIndexSupport;

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

    /**
     * Build locator with index. Before usage, check if locator type is current
     * 
     * @param locator locator as string.
     * @param index index of an element. Starts from 0 (first element will have 0 index)
     * @return {@link By} or {@link AppiumBy}
     */
    public abstract By buildLocatorWithIndex(String locator, int index);

    /**
     * Is support generation of locator with index of element in list
     * 
     * @return true if support, false otherwise
     */
    public boolean isIndexSupport() {
        return this.isIndexSupport;
    }
}
