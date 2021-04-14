package com.qaprosoft.carina.core.resources.annotation;

import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface L10N {

    static List<ExtendedWebElement> elements = new ArrayList<>();


}
