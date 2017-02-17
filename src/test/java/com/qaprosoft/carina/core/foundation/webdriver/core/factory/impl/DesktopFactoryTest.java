package com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl;

import static org.mockito.Matchers.anyString;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockObjectFactory;
import org.powermock.reflect.Whitebox;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;

@PrepareForTest({Configuration.class})
public class DesktopFactoryTest {
	
	Object mock = PowerMockito.mock(Object.class);
	
	@ObjectFactory
	public IObjectFactory setObjectFactory() {
		return new PowerMockObjectFactory();
	}
	
	@BeforeTest
    public void initialize() { //This is needed to initialize Mock objects
        MockitoAnnotations.initMocks(this);
    }

	@Test
	public void testCreate(){
		PowerMockito.mockStatic(Configuration.class);
		DesktopFactory mockDesktopFactory = PowerMockito.mock(DesktopFactory.class);
		DesiredCapabilities mockDesiredCapabilities = PowerMockito.mock(DesiredCapabilities.class);
		Device mockDevice = PowerMockito.mock(Device.class);
		RemoteWebDriver mockRemoteWebDriver = PowerMockito.mock(RemoteWebDriver.class);
		Configuration.Parameter parameter = Configuration.Parameter.SELENIUM_HOST;
		
		Whitebox.setInternalState(DesktopFactory.class, mockDesiredCapabilities); //instantiate private field
		//expectNew(DesktopFactory.class).andReturn(mockRemoteWebDriver);
		
		PowerMockito.when(Configuration.get(parameter)).thenReturn("host");
		PowerMockito.when(mockDesktopFactory.getCapabilities(anyString())).thenReturn(mockDesiredCapabilities);
	}
}
