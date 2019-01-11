/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.core.foundation.webdriver.core.factory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.events.WebDriverEventListener;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl.DesktopFactory;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl.MobileFactory;
import com.qaprosoft.carina.core.foundation.webdriver.listener.DriverListener;
import com.qaprosoft.zafira.client.ZafiraSingleton;
import com.qaprosoft.zafira.models.dto.TestArtifactType;

/**
 * DriverFactory produces driver instance with desired capabilities according to
 * configuration.
 *
 * @author Alexey Khursevich (hursevich@gmail.com)
 */
public class DriverFactory {

	protected static final Logger LOGGER = Logger.getLogger(DriverFactory.class);
	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss z");
	
	public static WebDriver create(String testName, DesiredCapabilities capabilities, String seleniumHost) {
		LOGGER.debug("DriverFactory start...");
		AbstractFactory factory;

		String driverType = Configuration.getDriverType(capabilities);
		switch (driverType) {
		case SpecialKeywords.DESKTOP:
			factory = new DesktopFactory();
			break;

		case SpecialKeywords.MOBILE:
			factory = new MobileFactory();
			break;

		default:
			throw new RuntimeException("Unsupported driver_type: " + driverType);
		}

		WebDriver driver = factory.create(testName, capabilities, seleniumHost);
		
		TestArtifactType vncArtifact = streamVNC(factory.getVncURL(driver));
		
		driver = factory.registerListeners(driver, getEventListeners(vncArtifact));   

		LOGGER.debug("DriverFactory finish...");

		return driver;
	}

	/**
	 * Create/Remember Zafira artifact that contains link to VNC websocket
	 * 
	 * @param vncURL - websocket URL
	 */
	private static TestArtifactType streamVNC(String vncURL) {
		TestArtifactType vncArtifact = new TestArtifactType();
		try {
			if (!StringUtils.isEmpty(vncURL) && ZafiraSingleton.INSTANCE.isRunning()) {
				String name = String.format("Live video %s", SDF.format(new Date()));
				LOGGER.debug("Init live video artifact name: " + name + "; vnc: " + vncURL);
				vncArtifact.setName(name);
				vncArtifact.setLink(vncURL);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to stream VNC: " + e.getMessage(), e);
		}
		
		return vncArtifact;
	}
	
	/**
	 * Reads 'driver_event_listeners' configuration property and initializes
	 * appropriate array of driver event listeners.
	 * 
	 * @return array of driver listeners
	 */
	private static WebDriverEventListener[] getEventListeners(TestArtifactType vncArtifact) {
		List<WebDriverEventListener> listeners = new ArrayList<>();
		try {
			//explicitely add default carina com.qaprosoft.carina.core.foundation.webdriver.listener.ScreenshotEventListener
			DriverListener driverListener = new DriverListener(vncArtifact);
			listeners.add(driverListener);

			String listenerClasses = Configuration.get(Parameter.DRIVER_EVENT_LISTENERS);
			if (!StringUtils.isEmpty(listenerClasses)) {
				for (String listenerClass : listenerClasses.split(",")) {
					Class<?> clazz = Class.forName(listenerClass);
					if (WebDriverEventListener.class.isAssignableFrom(clazz)) {
						WebDriverEventListener listener = (WebDriverEventListener) clazz.newInstance();
						listeners.add(listener);
						LOGGER.debug("Webdriver event listener registered: " + clazz.getName());
					}
				}
			}
			
		} catch (Exception e) {
			LOGGER.error("Unable to register webdriver event listeners: " + e.getMessage(), e);
		}
		return listeners.toArray(new WebDriverEventListener[listeners.size()]);
	}

}