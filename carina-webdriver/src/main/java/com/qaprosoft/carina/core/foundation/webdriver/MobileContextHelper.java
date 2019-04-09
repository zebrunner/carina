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

    private static boolean isModifiedContext = false;

    private static String storedContext;

    public static void changeToWebViewContext(WebDriver driver) {
        AppiumDriver appiumDriver = getDriverSafe(driver);
        Set contextNames = appiumDriver.getContextHandles();
        if (contextNames.stream().anyMatch(context -> ((String) context).contains("WEBVIEW"))) {
            isModifiedContext = true;
            appiumDriver.context(contextNames.toArray()[1].toString());
        } else {
            contextNames.forEach(t -> LOGGER.info(t));
            throw new NotFoundException("The webView context is not found");
        }
    }

    public static void changeToNativeAppContext(WebDriver driver) {
        isModifiedContext = false;
        getDriverSafe(driver).context("NATIVE_APP");
    }

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
                e.printStackTrace();
            }
            locatorField.setAccessible(true);
            WebDriver appiumDriver = null;
            try {
                appiumDriver = (WebDriver) locatorField.get(innerProxy);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (appiumDriver instanceof AppiumDriver) {
                return (AppiumDriver) driver;
            }
        }
        throw new ClassCastException("Appium Driver can not be casted from the actual driver.");
    }

    public static void backUpContext(WebDriver driver) {
        storedContext = getDriverSafe(driver).getContext();
    }

    public static void restoreContext(WebDriver driver) {
        getDriverSafe(driver).context(storedContext);
    }

    public static boolean isInWebViewContext() {
        return isModifiedContext;
    }
}
