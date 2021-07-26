package com.qaprosoft.carina.core.foundation.webdriver.locator.internal;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LocatingListsElementHandler implements InvocationHandler {
    private final WebElement element;
    private final ElementLocator locator;

    public LocatingListsElementHandler(WebElement element, ElementLocator locator) {
        this.element = element;
        this.locator = locator;
    }
    public Object invoke(Object object, Method method, Object[] objects) throws Throwable {

        if ("toString".equals(method.getName())) {
            return "Proxy element for: " + element.toString();
        }

        if ("getWrappedElement".equals(method.getName())) {
            return element;
        }

        try {
            return method.invoke(element, objects);
        } catch (InvocationTargetException e) {
            // Unwrap the underlying exception
            throw e.getCause();
        }
    }
}
