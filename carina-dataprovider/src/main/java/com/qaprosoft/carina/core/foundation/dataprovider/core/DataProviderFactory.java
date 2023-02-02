/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
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
package com.qaprosoft.carina.core.foundation.dataprovider.core;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.internal.ConstructorOrMethod;

import com.qaprosoft.carina.core.foundation.dataprovider.core.groupping.GroupByImpl;
import com.qaprosoft.carina.core.foundation.dataprovider.core.groupping.GroupByMapper;
import com.qaprosoft.carina.core.foundation.dataprovider.core.groupping.exceptions.GroupByException;
import com.qaprosoft.carina.core.foundation.dataprovider.core.impl.BaseDataProvider;
import com.zebrunner.carina.utils.commons.SpecialKeywords;

/**
 * Created by Patotsky on 16.12.2014.
 */
public class DataProviderFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final String TUID_ARGS_MAP_CONTEXT_ATTRIBUTE = "tuidArgsMap";
    public static final String DATA_SOURCE_UID_ARG_ATTRIBUTE = "uidArgsMap";
    public static final String DATA_SOURCE_UID_ARG_PATTERN = "{data_source_uid_arg}";

    private DataProviderFactory() {
        // hide
    }

    public static Object[][] getDataProvider(Annotation[] annotations, ITestContext context, ITestNGMethod m) {
        Map<String, String> tuidArgsMap = Collections.synchronizedMap(new HashMap<>());
        Map<String, String> testNameArgsMap = Collections.synchronizedMap(new HashMap<>());
        Map<String, String> uidArgsMap = Collections.synchronizedMap(new HashMap<>());

        Object[][] provider = new Object[][] {};

        for (Annotation annotation : annotations) {
            try {
                Class<? extends Annotation> type = annotation.annotationType();

                String providerClass = "";

                for (Method method : type.getDeclaredMethods()) {
                    if (method.getName().equalsIgnoreCase("classname")) {
                        providerClass = (String) method.invoke(annotation);
                        break;
                    }
                }

                if (providerClass.isEmpty())
                    continue;

                Class<?> clazz;
                Object object = null;
                try {
                    clazz = Class.forName(providerClass);
                    Constructor<?> ctor = clazz.getConstructor();
                    object = ctor.newInstance();
                } catch (Exception e) {
                    LOGGER.error("DataProvider failure", e);
                }

                if (object instanceof com.qaprosoft.carina.core.foundation.dataprovider.core.impl.BaseDataProvider) {
                    BaseDataProvider activeProvider = (BaseDataProvider) object;
                    provider = ArrayUtils.addAll(provider, activeProvider.getDataProvider(annotation, context, m));
                    tuidArgsMap.putAll(activeProvider.getTUIDArgsMap());
                    testNameArgsMap.putAll(activeProvider.getTestNameArgsMap());
                    uidArgsMap.putAll(activeProvider.getUidArgsMap());
                }

            } catch (Exception e) {
                LOGGER.error("DataProvider failure", e);
            }
        }

        if (GroupByMapper.getNumberOfColumnForGrouping().isPresent() ||
                GroupByMapper.getNameOfColumnForGrouping().isPresent()) {
            provider = getGroupedList(provider);
        }

        context.setAttribute(constructCustomDPAttributeUUID(TUID_ARGS_MAP_CONTEXT_ATTRIBUTE, m), tuidArgsMap);
        context.setAttribute(constructCustomDPAttributeUUID(SpecialKeywords.TEST_NAME_ARGS_MAP, m), testNameArgsMap);
        context.setAttribute(constructCustomDPAttributeUUID(DATA_SOURCE_UID_ARG_ATTRIBUTE, m), uidArgsMap);

        GroupByMapper.clear();
        return provider;
    }

    /**
     * Create unique id for attrubute for dataprovider.<br>
     * <b>For internal usage only</b>
     *
     * @param method see {@link ITestNGMethod}
     * @return id
     */
    public static String constructCustomDPAttributeUUID(String attributeName, ITestNGMethod method) {
        String pattern = "%s.%s(%s)-%s";
        ConstructorOrMethod constructorOrMethod = method.getConstructorOrMethod();

        String className = method.getTestClass().getName();
        String methodName = constructorOrMethod.getName();
        String argumentTypes = Arrays.stream(constructorOrMethod.getParameterTypes())
                .map(Class::getName)
                .collect(Collectors.joining(","));

        return String.format(pattern, className, methodName, argumentTypes, attributeName);
    }

    private static Object[][] getGroupedList(Object[][] provider) {
        Object[][] finalProvider;
        if (GroupByMapper.isHashMapped()) {
            if (GroupByMapper.getNameOfColumnForGrouping().isPresent()) {
                finalProvider = GroupByImpl.getGroupedDataProviderMap(provider, GroupByMapper.getNameOfColumnForGrouping().get());
            } else {
                throw new GroupByException("Incorrect groupColumn annotation parameter!");
            }
        } else {
            if (GroupByMapper.getNumberOfColumnForGrouping().isPresent() &&
                    !GroupByMapper.getNumberOfColumnForGrouping().get().equals(-1)) {
                finalProvider = GroupByImpl.getGroupedDataProviderArgs(provider, GroupByMapper.getNumberOfColumnForGrouping().get());
            } else {
                throw new GroupByException("Incorrect groupColumn annotation  parameter!");
            }
        }

        return finalProvider;
    }

}
