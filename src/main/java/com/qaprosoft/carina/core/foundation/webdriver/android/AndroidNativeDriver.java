package com.qaprosoft.carina.core.foundation.webdriver.android;

import java.net.URL;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Rotatable;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.interactions.HasTouchScreen;
import org.openqa.selenium.interactions.TouchScreen;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.RemoteTouchScreen;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.google.common.collect.ImmutableMap;

public class AndroidNativeDriver extends RemoteWebDriver implements HasTouchScreen, Rotatable {

    private RemoteTouchScreen touch;

    // for resolving IllegalArgumentException during using Augmenter for taking screenshots
    public AndroidNativeDriver() {
    }

    public AndroidNativeDriver(URL remoteAddress, Capabilities desiredCapabilities) {
        super(remoteAddress, desiredCapabilities);
        touch = new RemoteTouchScreen(getExecuteMethod());
    }

    @Override
    public TouchScreen getTouch() {
        return touch;
    }

    @Override
    public void rotate(ScreenOrientation orientation) {
        execute(DriverCommand.SET_SCREEN_ORIENTATION, ImmutableMap.of("orientation", orientation));
    }

    @Override
    public ScreenOrientation getOrientation() {
        return ScreenOrientation.valueOf(
                (String) execute(DriverCommand.GET_SCREEN_ORIENTATION).getValue());
    }
}
