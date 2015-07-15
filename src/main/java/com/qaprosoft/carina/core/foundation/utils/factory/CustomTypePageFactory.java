package com.qaprosoft.carina.core.foundation.utils.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.reflections.Reflections;

import com.qaprosoft.carina.core.foundation.utils.factory.DeviceType.Type;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;
import com.qaprosoft.carina.core.gui.AbstractPage;

public class CustomTypePageFactory {

	private static Reflections reflections;

	static {
		reflections = new Reflections("");
	}

	protected static final Logger LOGGER = Logger
			.getLogger(CustomTypePageFactory.class);

	public static <T extends AbstractPage> T initPage(WebDriver driver,
			Class<T> parentClass) {

		Set<Class<? extends T>> setClasses = reflections
				.getSubTypesOf(parentClass);
		LOGGER.debug("Relatives classes count:" + setClasses.size());
		Class<? extends T> deviceClass = null, familyClass = null;
		Type screenType = DevicePool.getDeviceType();
		for (Class<? extends T> clazz : setClasses) {
			if (clazz.getAnnotation(DeviceType.class) == null || clazz.getAnnotation(DeviceType.class).parentClass() != parentClass) {
				LOGGER.debug("Removing as parentClass is not satisfied or due to absence of @DeviceType annotation:"
						+ clazz.getClass().getName());
				continue;
			}
			if (clazz.getAnnotation(DeviceType.class).pageType()
					.equals(screenType)) {
				LOGGER.debug("Expected screenType: " + screenType);
				LOGGER.debug("Actual screenType: "
						+ clazz.getAnnotation(DeviceType.class).pageType());
				deviceClass = clazz;
				break;
			}
			if (clazz.getAnnotation(DeviceType.class).pageType().getFamily()
					.equals(screenType.getFamily())){
				LOGGER.debug(String.format("Family class '%s' correspond to required page.", screenType.getFamily()));
				familyClass = clazz;
			}
			
		}
		
		try {
			if(deviceClass != null){
				return deviceClass.getConstructor(WebDriver.class).newInstance(driver);
			} 
			if(familyClass != null){
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