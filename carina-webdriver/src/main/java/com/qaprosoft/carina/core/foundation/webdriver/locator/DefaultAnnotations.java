package com.qaprosoft.carina.core.foundation.webdriver.locator;

import java.lang.reflect.Field;

import org.openqa.selenium.By;
import org.openqa.selenium.support.pagefactory.Annotations;

public class DefaultAnnotations extends Annotations {

    public DefaultAnnotations(Field field) {
        super(field);
    }

    @Override
    public By buildBy() {
        return super.buildBy();
    }
}
