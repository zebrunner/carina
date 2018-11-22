package com.qaprosoft.carina.core.foundation.webdriver;

import static org.mockito.Mockito.mock;

import org.mockito.Mock;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeSuite;

import com.qaprosoft.carina.core.foundation.utils.R;

public class BeforeSuiteTest implements IDriverPool {
	
	@Mock
    private WebDriver mockDriverSuite;
	
	 @BeforeSuite(alwaysRun = true)
	    public void beforeSuite() {
//	        R.CONFIG.put("thread_count", "1");
//	        R.CONFIG.put("data_provider_thread_count", "1");

	        this.mockDriverSuite = mock(WebDriver.class);
	    }

}
