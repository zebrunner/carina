package com.qaprosoft.carina.core.foundation.utils.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.reflections.Reflections;

import com.qaprosoft.carina.core.foundation.utils.factory.DeviceType.Type;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;
import com.qaprosoft.carina.core.gui.AbstractPage;

public class CustomTypePageFactory {

	private static final String VERSION_SPLITTER = "\\.";
	
	private static Reflections reflections;

	static {
		reflections = new Reflections("");
	}

	protected static final Logger LOGGER = Logger
			.getLogger(CustomTypePageFactory.class);

	public static <T extends AbstractPage> T initPage(Class<T> parentClass){
		return initPage(DriverPool.getDriverByThread(), parentClass);
	}
	
	public static <T extends AbstractPage> T initPage(WebDriver driver,
			Class<T> parentClass) {

		Set<Class<? extends T>> setClasses = reflections
				.getSubTypesOf(parentClass);
		LOGGER.debug("Relatives classes count:" + setClasses.size());
		Class<? extends T> versionClass = null, majorVersionClass = null, deviceClass = null, familyClass = null;
		Type screenType = DevicePool.getDeviceType();
		Device device = DevicePool.getDevice();
		// default version in case if it is desktop driver
		String deviceVersion = "1";
		if (null != device) {
			deviceVersion = device.getOsVersion();
		}
		String majorVersionNumber = deviceVersion.split(VERSION_SPLITTER)[0];
		LOGGER.debug("Major version of device OS: " + majorVersionNumber);
		for (Class<? extends T> clazz : setClasses) {
			if (clazz.getAnnotation(DeviceType.class) == null || clazz.getAnnotation(DeviceType.class).parentClass() != parentClass) {
				LOGGER.debug("Removing as parentClass is not satisfied or due to absence of @DeviceType annotation:"
						+ clazz.getClass().getName());
				continue;
			}
			DeviceType dt = clazz.getAnnotation(DeviceType.class);
			
			if (dt.pageType().equals(screenType)) {
				LOGGER.debug("Expected screenType: " + screenType);
				LOGGER.debug("Actual screenType: " + dt.pageType());
				if(Arrays.asList(dt.version()).contains(deviceVersion)) {
					LOGGER.debug("Expected version: " + deviceVersion);
					LOGGER.debug("Actual versions: " + dt.version());
					versionClass = clazz;
					break;
				}
				
				for (String version : dt.version()) {
					if(version.split(VERSION_SPLITTER)[0].equals(majorVersionNumber)) {
						majorVersionClass = clazz;
						LOGGER.debug("Class was chosen by major version number of device");
						break;
					}
				}
				
				deviceClass = clazz;
				continue;
			}
			if (dt.pageType().getFamily().equals(screenType.getFamily())){
				LOGGER.debug(String.format("Family class '%s' correspond to required page.", screenType.getFamily()));
				familyClass = clazz;
			}
			
		}
		
		try {
			if(versionClass != null){
				LOGGER.info("Instance by version and platform will be created.");
				return versionClass.getConstructor(WebDriver.class).newInstance(driver);
			}
			if(majorVersionClass != null){
				LOGGER.info("Instance by major version and platform will be created.");
				return majorVersionClass.getConstructor(WebDriver.class).newInstance(driver);
			}
			if(deviceClass != null){
				LOGGER.info("Instance by platform will be created.");
				return deviceClass.getConstructor(WebDriver.class).newInstance(driver);
			} 
			if(familyClass != null){
				LOGGER.info("Instance by family will be created.");
				return familyClass.getConstructor(WebDriver.class).newInstance(driver);
			}
			throw new RuntimeException(
					String.format("There is no any class that satisfy to required conditions: [parent class - %s], [device type - %s]", parentClass.getName(), screenType));
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}