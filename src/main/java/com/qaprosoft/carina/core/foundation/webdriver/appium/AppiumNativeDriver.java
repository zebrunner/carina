package com.qaprosoft.carina.core.foundation.webdriver.appium;

import com.google.common.collect.ImmutableMap;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.Messager;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.HasTouchScreen;
import org.openqa.selenium.interactions.TouchScreen;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.RemoteTouchScreen;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Deprecated
public class AppiumNativeDriver extends RemoteWebDriver implements HasTouchScreen, Rotatable {

	protected static final Logger LOGGER = Logger.getLogger(AppiumNativeDriver.class);

	protected static final long IMPLICIT_TIMEOUT = Configuration.getLong(Parameter.IMPLICIT_TIMEOUT);

	protected static final long EXPLICIT_TIMEOUT = Configuration.getLong(Parameter.EXPLICIT_TIMEOUT);

	protected static final long RETRY_TIME = Configuration.getLong(Parameter.RETRY_INTERVAL);	
	
	protected static Wait<WebDriver> wait;
	
    private RemoteTouchScreen touch;

/*    // for resolving IllegalArgumentException during using Augmenter for taking screenshots
    public AppiumNativeDriver() {
    }
*/
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
    
    @Override
    public Response execute(String driverCommand, Map<String, ?> parameters) {
      return super.execute(driverCommand, parameters);
    }

    @Override
    protected Response execute(String command) {
      return execute(command, ImmutableMap.<String, Object>of());
    }
    
    
	public ExtendedWebElement findElementByIosUIAutomation(String using) {
		return findElementByIosUIAutomation(using, "extendedWebElement");
	}
	
	public ExtendedWebElement findElementByIosUIAutomation(String using, String name) {
		return findElementByIosUIAutomation(using, name, EXPLICIT_TIMEOUT);
	}		
	
	public ExtendedWebElement findElementByIosUIAutomation(final String using, String name, long timeout)
	{
		ExtendedWebElement element;
		manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		wait = new WebDriverWait(this, timeout, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return !findElements("-ios uiautomation", using).isEmpty();
				}
			});
			
			element = new ExtendedWebElement(findElement("-ios uiautomation", using), name);
			LOGGER.info(Messager.ELEMENT_FOUND.info(name));
		}
		catch (Exception e)
		{
			element = null;
			LOGGER.info(Messager.ELEMENT_NOT_FOUND.error(name));
			manage().timeouts().implicitlyWait(IMPLICIT_TIMEOUT, TimeUnit.SECONDS);
			throw new RuntimeException(e);
		}
		manage().timeouts().implicitlyWait(IMPLICIT_TIMEOUT, TimeUnit.SECONDS);
		return element;
	}	
	

	public List<ExtendedWebElement> findElementsByIosUIAutomation(String using) {
		return findElementsByIosUIAutomation(using, EXPLICIT_TIMEOUT);
	}
	
	public List<ExtendedWebElement> findElementsByIosUIAutomation(final String using, long timeout)
	{
		List<ExtendedWebElement> extendedWebElements = new ArrayList<ExtendedWebElement> ();
		List<WebElement> webElements = new ArrayList<WebElement> ();
		
		manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		wait = new WebDriverWait(this, timeout, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return !findElements("-ios uiautomation", using).isEmpty();
				}
			});
			webElements = findElements("-ios uiautomation", using);
		}
		catch (Exception e)
		{
			//do nothing
		}
		
		for (WebElement element : webElements) {
			String name = "undefined";
			try {
				name = element.getText();
			} catch (Exception e) {/* do nothing*/}
			
			extendedWebElements.add(new ExtendedWebElement(element, name));
		}		
		manage().timeouts().implicitlyWait(IMPLICIT_TIMEOUT, TimeUnit.SECONDS);
		return extendedWebElements;
	}		
	
	
	
	
}
