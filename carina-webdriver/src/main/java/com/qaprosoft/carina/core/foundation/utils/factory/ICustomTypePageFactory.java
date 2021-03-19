package com.qaprosoft.carina.core.foundation.utils.factory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.WebDriver;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.exception.RequiredCtorNotFoundException;
import com.qaprosoft.carina.core.foundation.utils.factory.DeviceType.Type;
import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.gui.AbstractPage;

public interface ICustomTypePageFactory extends IDriverPool {
    static final Logger PAGEFACTORY_LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    String VERSION_SPLITTER = "\\.";

    String INTEGER_STR = "class java.lang.Integer";
    String INT_STR = "int";

    String LONG_OBJ_STR = "class java.lang.Long";
    String LONG_STR = "long";

    String DOUBLE_OBJ_STR = "class java.lang.Double";
    String DOUBLE_STR = "double";

    Reflections REFLECTIONS = new Reflections("");

    public default <T extends AbstractPage> T initPage(Class<T> parentClass, Object... parameters) {
        return initPage(getDriver(), parentClass, parameters);
    }

    public default <T extends AbstractPage> T initPage(WebDriver driver, Class<T> parentClass, Object... parameters) {

        if (driver == null) {
            PAGEFACTORY_LOGGER.error("Page isn't created. There is no any initialized driver for thread: " + Thread.currentThread().getId());
            throw new RuntimeException("Page isn't created. Driver isn't initialized.");
        }

        Set<Class<? extends T>> setClasses = REFLECTIONS.getSubTypesOf(parentClass);
        PAGEFACTORY_LOGGER.debug("Relatives classes count:" + setClasses.size());
        Class<? extends T> versionClass = null, majorVersionClass = null, deviceClass = null, familyClass = null, requiredClass = null;
        Type screenType = IDriverPool.getDefaultDevice().getDeviceType();

        Device device = IDriverPool.getDefaultDevice();
        // default version in case if it is desktop driver
        String deviceVersion = "1";
        if (!device.getOsVersion().isEmpty()) {
            deviceVersion = device.getOsVersion();
        }
        String majorVersionNumber = deviceVersion.split(VERSION_SPLITTER)[0];
        PAGEFACTORY_LOGGER.debug("Major version of device OS: " + majorVersionNumber);
        for (Class<? extends T> clazz : setClasses) {
            if (clazz.getAnnotation(DeviceType.class) == null || clazz.getAnnotation(DeviceType.class).parentClass() != parentClass) {
                PAGEFACTORY_LOGGER.debug(String.format("Removing as parentClass (%s) is not satisfied or due to absence of @DeviceType annotation on class: %s",
                        parentClass.getName(), clazz.getName()));
                continue;
            }
            DeviceType dt = clazz.getAnnotation(DeviceType.class);

            PAGEFACTORY_LOGGER.debug(String.format("Expected screenType: %s, Actual screenType: %s", screenType, dt.pageType()));
            if (dt.pageType().equals(screenType)) {
                if (Arrays.asList(dt.version()).contains(deviceVersion)) {
                    PAGEFACTORY_LOGGER.debug("Expected version: " + deviceVersion);
                    PAGEFACTORY_LOGGER.debug("Actual versions: " + dt.version());
                    versionClass = clazz;
                    break;
                }

                for (String version : dt.version()) {
                    if (version.split(VERSION_SPLITTER)[0].equals(majorVersionNumber)) {
                        majorVersionClass = clazz;
                        PAGEFACTORY_LOGGER.debug("Class was chosen by major version number of device");
                        break;
                    }
                }

                deviceClass = clazz;
                continue;
            }
            if (dt.pageType().getFamily().equals(screenType.getFamily())) {
                PAGEFACTORY_LOGGER.debug(String.format("Family class '%s' correspond to required page.", screenType.getFamily()));
                familyClass = clazz;
            }

        }
        try {
            if (versionClass != null) {
                PAGEFACTORY_LOGGER.debug("Instance by version and platform will be created.");
                requiredClass = versionClass;
            } else if (majorVersionClass != null) {
                PAGEFACTORY_LOGGER.debug("Instance by major version and platform will be created.");
                requiredClass = majorVersionClass;
            } else if (deviceClass != null) {
                PAGEFACTORY_LOGGER.debug("Instance by platform will be created.");
                requiredClass = deviceClass;
            } else if (familyClass != null) {
                PAGEFACTORY_LOGGER.debug("Instance by family will be created.");
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
            PAGEFACTORY_LOGGER.debug("Invoking constructor for " + requiredClass);
            Constructor<? extends T> requiredCtor = getConstructorByParams(requiredClass, parameters);

            return requiredCtor.newInstance(parameters);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
            PAGEFACTORY_LOGGER.debug(
                    "Discovered one of the InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException");
            throw new RuntimeException("Unable to instantiate page!", e);
        }
    }

    // TODO: make it private after migration to Java 9
    /**
     * Get constructor from clazz that satisfy specific range of parameters
     * (using Reflection)
     * 
     * @param <T> This is the type parameter
     * 
     * @param clazz
     * 			Class clazz
     * @param parameters
     * 			Object... parameters
     * @return constructor
     * 			
     */
    @SuppressWarnings("unchecked")
    default <T extends AbstractPage> Constructor<? extends T> getConstructorByParams(Class<T> clazz, Object... parameters) {
        PAGEFACTORY_LOGGER.debug("Attempt to find costructor that satisfy to following parameters: " + Arrays.toString(parameters));
        Class<?>[] parametersTypes;
        List<Class<?>> parametersTypesList = new ArrayList<Class<?>>();
        for (Object param : parameters) {
            parametersTypesList.add(param.getClass());
        }
        parametersTypes = parametersTypesList.toArray(new Class<?>[parametersTypesList.size()]);
        Constructor<?> requiredCtor = null;
        Constructor<?>[] ctors = clazz.getDeclaredConstructors();
        PAGEFACTORY_LOGGER.debug(String.format("Class %s contains %d ctors ", clazz.toString(), ctors.length));
        for (Constructor<?> constructor : ctors) {
            PAGEFACTORY_LOGGER.debug("Constructor: ".concat(constructor.toString()));
        }
        for (Constructor<?> constructor : ctors) {
            Class<?>[] ctorTypes = constructor.getParameterTypes();

            // Check if passed parameters quantity satisfy to constructor's
            // parameters size
            if (parametersTypes.length != ctorTypes.length) {
                PAGEFACTORY_LOGGER.debug(String.format("Ctors quantity doesn't satisfy to requirements. " + "Expected: %d. Actual: %d", parametersTypes.length,
                        ctorTypes.length));
                continue;
            }
            if (parametersTypes.length == 0) {
                requiredCtor = constructor;
                break;
            }
            int foundParams = 0;

            // comparison logic for passed parameters type and ctor' parameters
            // type
            for (Class<?> ctorType : ctorTypes) {
                for (Class<?> paramType : parametersTypes) {
                    if (paramType.isInstance(ctorType) || ctorType.isAssignableFrom(paramType) || comparePrimitives(ctorType, paramType)) {
                        foundParams++;
                        break;
                    }
                }
            }

            if (foundParams == ctorTypes.length) {
                requiredCtor = constructor;
            }

        }

        if (null == requiredCtor) {
            throw new RequiredCtorNotFoundException();
        }

        return (Constructor<? extends T>) requiredCtor;
    }

    // TODO: make it private after migration to Java 9
    /**
     * Method to compare primitives with corresponding wrappers
     * 
     * @param obj1
     * 			Object obj1
     * @param obj2
     * 			Object obj2
     * @return boolean
     * 			boolean result
     */
    default boolean comparePrimitives(Object obj1, Object obj2) {

        switch (obj1.toString()) {
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
