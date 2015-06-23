package com.qaprosoft.carina.core.foundation.utils.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
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
		Iterator<Class<? extends T>> iterator = setClasses.iterator();

		Type screenType = DevicePool.getDeviceType();
		while (iterator.hasNext()) {
			Class<? extends T> clazz = iterator.next();
			if ((clazz.getAnnotation(DeviceType.class) == null)
					|| (clazz.getAnnotation(DeviceType.class).parentClass() != parentClass)
					|| !clazz.getAnnotation(DeviceType.class).pageType()
							.equals(screenType)) {
				iterator.remove();
			}
		}

		if (setClasses.size() != 1) {
			LOGGER.info("Quantity of classes: " + setClasses.size() + "\n "
					+ setClasses.toString());
			throw new RuntimeException(
					"Quantity of classes: "
							+ setClasses.size()
							+ "! "
							+ "Unable to initialize custom page as There are more than 1 class that could be used as implementation of the page or it absent at all. Please, check config.");
		}

		try {
			return new ArrayList<>(setClasses).get(0)
					.getConstructor(WebDriver.class).newInstance(driver);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}