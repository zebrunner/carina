package com.qaprosoft.carina.core.foundation.utils.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.reflections.Reflections;

import com.qaprosoft.carina.core.foundation.exception.RequiredCtorNotFoundException;
import com.qaprosoft.carina.core.foundation.utils.factory.DeviceType.Type;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;
import com.qaprosoft.carina.core.gui.AbstractPage;

public class CustomTypePageFactory {

	private static final String VERSION_SPLITTER = "\\.";
	
	private static final String INTEGER_STR = "class java.lang.Integer";
    private static final String INT_STR = "int";
    
    private static final String LONG_OBJ_STR = "class java.lang.Long";
    private static final String LONG_STR = "long";
    
    private static final String DOUBLE_OBJ_STR = "class java.lang.Double";
    private static final String DOUBLE_STR = "double";
	
	private static Reflections reflections;

	static {
		reflections = new Reflections("");
	}

	protected static final Logger LOGGER = Logger
			.getLogger(CustomTypePageFactory.class);

	public static <T extends AbstractPage> T initPage(Class<T> parentClass, Object... parameters) {
		return initPage(DriverPool.getDriver(), parentClass, parameters);
	}

	public static <T extends AbstractPage> T initPage(WebDriver driver,
			Class<T> parentClass, Object... parameters) {

		if (driver == null) {
			LOGGER.error("Page isn't created. There is no any initialized driver for thread: " + Thread.currentThread().getId());
			throw new RuntimeException("Page isn't created. Driver isn't initialized.");
		}
		
		Set<Class<? extends T>> setClasses = reflections
				.getSubTypesOf(parentClass);
		LOGGER.debug("Relatives classes count:" + setClasses.size());
		Class<? extends T> versionClass = null, majorVersionClass = null, deviceClass = null, familyClass = null, requiredClass = null;
		Type screenType = DevicePool.getDevice().getDeviceType();
		
		Device device = DevicePool.getDevice();
		// default version in case if it is desktop driver
		String deviceVersion = "1";
		if (!device.getOsVersion().isEmpty()) {
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
		Constructor<? extends T> ctor;
		try {
			if(versionClass != null){
				LOGGER.debug("Instance by version and platform will be created.");
				requiredClass = versionClass;
			} else if(majorVersionClass != null){
				LOGGER.debug("Instance by major version and platform will be created.");
				requiredClass = majorVersionClass;
			} else if(deviceClass != null){
				LOGGER.debug("Instance by platform will be created.");
				requiredClass = deviceClass;
			} else if(familyClass != null){
				LOGGER.debug("Instance by family will be created.");
				requiredClass = familyClass;
			} else {
			    throw new RuntimeException(
	                    String.format("There is no any class that satisfy to required conditions: [parent class - %s], [device type - %s]", 
	                            parentClass.getName(), screenType));
			}
			// handle cases where we have only WebDriver as ctor parameter
			if (parameters.length == 0) {
				parameters = new Object[] { driver };
			}
			LOGGER.debug("Invoking constructor for " + requiredClass);
			ctor = getConstructorByParams(requiredClass, parameters);
			return ctor.newInstance(parameters);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| SecurityException e) {
			LOGGER.debug("Discovered one of the InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException");
			throw new RuntimeException("Unable to instantiate page!" , e);
		}
	}
	
	/**
     * Get constructor from clazz that satisfy specific range of parameters (using Reflection)
     * @param clazz
     * @param parameters
     * @return constructor
     */
    @SuppressWarnings("unchecked")
    private static <T extends AbstractPage> Constructor<? extends T> getConstructorByParams(Class<T> clazz,
            Object... parameters) {
        LOGGER.debug("Attempt to find costructor that satisfy to following parameters: " + parameters.toString());
        Class<?>[] parametersTypes;
        List<Class<?>> parametersTypesList = new ArrayList<Class<?>>();
        for (Object param : parameters) {
            parametersTypesList.add(param.getClass());
        }
        parametersTypes = parametersTypesList.toArray(new Class<?>[parametersTypesList.size()]);
        Constructor<?> requiredCtor = null;
        Constructor<?>[] ctors = clazz.getDeclaredConstructors();
        LOGGER.debug(String.format("Class %s contains %d ctors ", clazz.toString(), ctors.length));
        for (Constructor<?> constructor : ctors) {
            LOGGER.debug("Constructor: ".concat(constructor.toString()));
        }
        for (Constructor<?> constructor : ctors) {
            Class<?>[] ctorTypes = constructor.getParameterTypes();
            
            // Check if passed parameters quantity satisfy to constructor's parameters size
            if (parametersTypes.length != ctorTypes.length) {
                LOGGER.debug(String.format("Ctors quantity doesn't satisfy to requirements. "
                        + "Expected: %d. Actual: %d", parametersTypes.length, ctorTypes.length));
                continue;
            }
            if (parametersTypes.length == 0) {
                requiredCtor = constructor;
                break;
            }
            int foundParams = 0;
            
            // comparison logic for passed parameters type and ctor' parameters type
            for (Class<?> ctorType : ctorTypes) {
                for (Class<?> paramType : parametersTypes) {
                    if (paramType.isInstance(ctorType) || ctorType.isAssignableFrom(paramType) || comparePrimitives(ctorType, paramType)) {
                        foundParams++;
                        break;
                    }
                }
            }

            if (foundParams == ctorTypes.length) {
                requiredCtor = constructor;;
            }

        }

        if (null == requiredCtor) {
            throw new RequiredCtorNotFoundException();
        }
        
        return (Constructor<? extends T>) requiredCtor;
    }
    
    
    /**
     * Method to compare primitives with corresponding wrappers
     * @param obj1
     * @param obj2
     * @return 
     */
    private static boolean comparePrimitives(Object obj1, Object obj2) {
        
        switch(obj1.toString()) {
            case INT_STR:
            case INTEGER_STR:
                return INTEGER_STR.equalsIgnoreCase(obj2.toString()) || obj2.toString().equalsIgnoreCase(INT_STR);
            case LONG_OBJ_STR:
            case LONG_STR:
                return LONG_OBJ_STR.equalsIgnoreCase(obj2.toString()) || obj2.toString().equalsIgnoreCase(LONG_STR);
            case DOUBLE_OBJ_STR:
            case DOUBLE_STR:
                return DOUBLE_OBJ_STR.equalsIgnoreCase(obj2.toString()) || obj2.toString().equalsIgnoreCase(DOUBLE_STR);
        }
        return false;
    }
}