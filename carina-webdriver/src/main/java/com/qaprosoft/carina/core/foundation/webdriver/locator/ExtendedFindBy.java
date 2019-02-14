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
