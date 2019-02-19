package com.qaprosoft.carina.core.foundation.webdriver.locator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactoryFinder;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
@PageFactoryFinder(ExtendedFindBy.FindByBuilder.class)
public @interface ExtendedFindBy {

    String iosPredicate() default "";

    String iosClassChain() default "";

    String androidUIAutomator() default "";

    String accessibilityId() default "";
    
    // Images in automation:
    // https://github.com/appium/appium/blob/master/docs/en/writing-running-appium/image-comparison.md
    // How to adjust parameters: 
    // http://appium.io/docs/en/advanced-concepts/image-elements/
    // Real screen sizes for iOS:
    // https://developer.apple.com/library/archive/documentation/DeviceInformation/Reference/iOSDeviceCompatibility/Displays/Displays.html
    String image() default "";
    
    String text() default "";

    public static class FindByBuilder extends ExtendedFindByBuilder {
        public By buildIt(Object annotation, Field field) {
            ExtendedFindBy findBy = (ExtendedFindBy) annotation;
            By ans = buildByFromShortFindBy(findBy);
            return ans;
        }
    }
}
