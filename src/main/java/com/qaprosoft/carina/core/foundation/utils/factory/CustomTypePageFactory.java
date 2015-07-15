package com.qaprosoft.carina.core.foundation.utils.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
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
		Set<Class<? extends T>> setClassesCopy = new HashSet<>();
		setClassesCopy.addAll(setClasses);
		LOGGER.debug("Relatives classes count:" + setClasses.size());
		Iterator<Class<? extends T>> iterator = setClasses.iterator();

		Type screenType = DevicePool.getDeviceType();
		while (iterator.hasNext()) {
			Class<? extends T> clazz = iterator.next();
			if (clazz.getAnnotation(DeviceType.class) == null) {
				LOGGER.debug("Removing as there is no DeviceType annotation:"
						+ iterator.getClass().getName());
				iterator.remove();
			}
			if (clazz.getAnnotation(DeviceType.class).parentClass() != parentClass) {
				LOGGER.debug("Removing as parentClass is not satisfied:"
						+ iterator.getClass().getName());
				LOGGER.debug("Expected parent class: " + parentClass.getName());
				LOGGER.debug("Actual parent class: "
						+ clazz.getAnnotation(DeviceType.class).parentClass()
								.getName());
				iterator.remove();
			}
			if (!clazz.getAnnotation(DeviceType.class).pageType().getType()
					.equalsIgnoreCase(screenType.getType())) {
				iterator.remove();
				LOGGER.debug("Removing as screenType is not satisifed:"
						+ iterator.getClass().getName());
				LOGGER.debug("Expected screenType [specific type]: " + screenType.getType());
				LOGGER.debug("Actual screenType [specific type]: "
						+ clazz.getAnnotation(DeviceType.class).pageType().getType());
			}
			/*
			 * if ((clazz.getAnnotation(DeviceType.class) == null) ||
			 * (clazz.getAnnotation(DeviceType.class).parentClass() !=
			 * parentClass) || !clazz.getAnnotation(DeviceType.class).pageType()
			 * .equals(screenType)) { iterator.remove();
			 * LOGGER.debug("Removing " + iterator.getClass().getName()); }
			 */
		}
		
		if (setClasses.size() == 0) {
			LOGGER.info("Specific page type wasn't found, therefore, search by family type will be executed. Look for: "
					.concat(screenType.getFamily()));
			
			Iterator<Class<? extends T>> iteratorCopy = setClassesCopy.iterator();

			while (iteratorCopy.hasNext()) {
				Class<? extends T> clazz = iteratorCopy.next();
				if (clazz.getAnnotation(DeviceType.class) == null) {
					LOGGER.debug("Removing as there is no DeviceType annotation:"
							+ iteratorCopy.getClass().getName());
					iteratorCopy.remove();
				}
				if (clazz.getAnnotation(DeviceType.class).parentClass() != parentClass) {
					LOGGER.debug("Removing as parentClass is not satisfied:"
							+ iteratorCopy.getClass().getName());
					LOGGER.debug("Expected parent class: " + parentClass.getName());
					LOGGER.debug("Actual parent class: "
							+ clazz.getAnnotation(DeviceType.class).parentClass()
									.getName());
					iteratorCopy.remove();
				}
				if (!clazz.getAnnotation(DeviceType.class).pageType().getFamily()
						.equalsIgnoreCase(screenType.getFamily())) {
					iteratorCopy.remove();
					LOGGER.debug("Removing as screenType is not satisifed:"
							+ iteratorCopy.getClass().getName());
					LOGGER.debug("Expected screenType family: " + screenType.getFamily());
					LOGGER.debug("Actual screenType family: "
							+ clazz.getAnnotation(DeviceType.class).pageType().getFamily());
				}
			}
		}

		if (setClasses.size() != 1 && setClassesCopy.size() != 1) {
			LOGGER.info("Quantity of classes by specific type: " + setClasses.size() + "\n "
					+ setClasses.toString());
			LOGGER.info("Quantity of classes by family type: " + setClassesCopy.size() + "\n "
					+ setClassesCopy.toString());
			throw new RuntimeException(
					"Quantity of classes by specific type: "
							+ setClasses.size() + "Quantity of classes by family type: " 
							+ setClassesCopy.size() + "! "
							+ "Unable to initialize custom page as There are more than 1 class that could be used as implementation of the page or it absent at all. Please, check config.");
		}

		try {
			if(setClasses.size() == 1) {
			return new ArrayList<>(setClasses).get(0)
					.getConstructor(WebDriver.class).newInstance(driver);
			}else{
				return new ArrayList<>(setClassesCopy).get(0)
						.getConstructor(WebDriver.class).newInstance(driver);
			}
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}