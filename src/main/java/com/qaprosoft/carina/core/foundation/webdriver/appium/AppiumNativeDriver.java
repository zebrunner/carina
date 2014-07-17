package com.qaprosoft.carina.core.foundation.webdriver.appium;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Rotatable;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.HasTouchScreen;
import org.openqa.selenium.interactions.TouchScreen;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.RemoteTouchScreen;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.google.common.collect.ImmutableMap;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;

public class AppiumNativeDriver extends RemoteWebDriver implements HasTouchScreen, Rotatable {

    private RemoteTouchScreen touch;

    // for resolving IllegalArgumentException during using Augmenter for taking screenshots
    public AppiumNativeDriver() {
    }

    public AppiumNativeDriver(URL remoteAddress, Capabilities desiredCapabilities) {
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
    
	public ExtendedWebElement findElementByIosUIAutomation(String using) {
		return new ExtendedWebElement(findElement("-ios uiautomation", using), "extendedWebElement");
	}	

	public List<ExtendedWebElement> findElementsByIosUIAutomation(String using) {
		List<WebElement> webElements = findElements("-ios uiautomation", using);
		List<ExtendedWebElement> webExtendedElements = Collections.<ExtendedWebElement>emptyList();
		
		for (WebElement element : webElements) {
			webExtendedElements.add(new ExtendedWebElement(element, "extendedWebElement"));
		}
		return webExtendedElements;
	}    
	
}
