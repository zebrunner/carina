package com.qaprosoft.carina.core.foundation.webdriver;

import io.appium.java_client.AppiumDriver;
import org.apache.log4j.Logger;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Set;

public class MobileContextHelper {

    protected static final Logger LOGGER = Logger.getLogger(DriverHelper.class);

    private static AppiumDriver getDriverSafe(WebDriver driver) {
        if (driver instanceof EventFiringWebDriver) {
            driver = ((EventFiringWebDriver) driver).getWrappedDriver();
            if (driver instanceof AppiumDriver) {
                return (AppiumDriver) driver;
            }
        }
        if (driver instanceof Proxy) {
            InvocationHandler innerProxy = Proxy.getInvocationHandler(driver);
            Field locatorField = null;
            try {
                locatorField = innerProxy.getClass().getDeclaredField("arg$2");
            } catch (NoSuchFieldException e) {
                LOGGER.error(e.getMessage());
            }
            if (locatorField!=null) {
                locatorField.setAccessible(true);
                WebDriver appiumDriver = null;
                try {
                    appiumDriver = (WebDriver) locatorField.get(innerProxy);
                } catch (IllegalAccessException e) {
                    LOGGER.error(e.getMessage());
                }
                if (appiumDriver instanceof AppiumDriver) {
                    return (AppiumDriver) appiumDriver;
                }
            }
        }
        throw new ClassCastException("Appium Driver can not be casted from the actual driver.");
    }

    public void changeToWebViewContext(WebDriver driver) {
        AppiumDriver appiumDriver = getDriverSafe(driver);
        Set<String> contextNames = appiumDriver.getContextHandles();
        if (contextNames.stream().anyMatch(context -> context.contains("WEBVIEW"))) {
            appiumDriver.context(contextNames.toArray()[1].toString());
        } else {
            for (String context : contextNames) {
                LOGGER.info(context);
            }
            throw new NotFoundException("The webView context is not found");
        }
    }

    public void changeToNativeAppContext(WebDriver driver) {
        getDriverSafe(driver).context("NATIVE_APP");
    }

    public String getContext(WebDriver driver) {
        return getDriverSafe(driver).getContext();
    }

    public void setContext(String context, WebDriver driver) {
        getDriverSafe(driver).context(context);
    }

    public boolean isInWebViewContext(WebDriver driver) {
        return getContext(driver).contains("WEBVIEW");
    }
}
