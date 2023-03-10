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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.qaprosoft.carina.core.foundation.dataprovider.annotations.CsvDataSourceParameters;
import com.qaprosoft.carina.core.foundation.dataprovider.annotations.XlsDataSourceParameters;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.dataprovider.core.groupping.GroupByImpl;
import com.qaprosoft.carina.core.foundation.dataprovider.core.groupping.GroupByMapper;
import com.qaprosoft.carina.core.foundation.dataprovider.core.groupping.exceptions.GroupByException;
import com.qaprosoft.carina.core.foundation.dataprovider.core.impl.BaseDataProvider;

/**
 * Created by Patotsky on 16.12.2014.
 */
public class DataProviderFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private DataProviderFactory() {
    }

    public static Object[][] getDataProvider(Annotation[] annotations, ITestContext context, ITestNGMethod m) {
        Map<String, String> tuidMap = Collections.synchronizedMap(new HashMap<>());
        Map<String, String> testNameFromColumn = Collections.synchronizedMap(new HashMap<>());
        Object[][] provider = new Object[][]{};

        for (Annotation annotation : annotations) {
            String providerClass = findProviderClass(annotation);
            if (providerClass.isEmpty()) {
                continue;
            }

            Object providerObject = initDataProvider(providerClass);

            if (providerObject instanceof BaseDataProvider) {
                BaseDataProvider dataProvider = (BaseDataProvider) providerObject;
                provider = ArrayUtils.addAll(provider, dataProvider.getDataProvider(annotation, context, m));
                tuidMap.putAll(dataProvider.getTuidMap());
                testNameFromColumn.putAll(dataProvider.getTestColumnNamesMap());
            }
        }

        if (!GroupByMapper.getInstanceInt().isEmpty() || !GroupByMapper.getInstanceStrings().isEmpty()) {
            provider = getGroupedList(provider);
        }
        putValuesToContext(context, tuidMap, testNameFromColumn);

        // clear group by settings
        GroupByMapper.getInstanceInt().clear();
        GroupByMapper.getInstanceStrings().clear();

        return provider;
    }

    private static void putValuesToContext(ITestContext context,
                                           Map<String, String> tuidMap,
                                           Map<String, String> testNameFromColumn) {
        @SuppressWarnings("unchecked")
        Map<String, String> contextTUID = (Map<String, String>) context.getAttribute(SpecialKeywords.TUID);
        if (contextTUID != null) {
            contextTUID.putAll(tuidMap);
        } else {
            context.setAttribute(SpecialKeywords.TUID, tuidMap);
        }

        @SuppressWarnings("unchecked")
        Map<String, String> contextTestName = (Map<String, String>) context.getAttribute(SpecialKeywords.TEST_NAME);
        if (contextTestName != null) {
            contextTUID.putAll(tuidMap);
        } else {
            context.setAttribute(SpecialKeywords.TEST_NAME, testNameFromColumn);
        }
    }

    /**
     * Finds class name for data provider implementation.
     *
     * @param annotation test method annotation.
     * @return class name of data provider if it was found in annotation classname() method.
     * Empty string if not.
     */
    private static String findProviderClass(Annotation annotation) {
        Class<? extends Annotation> type = annotation.annotationType();
        String providerClass = "";

        try {
            for (Method method : type.getDeclaredMethods()) {
                if (method.getName().equalsIgnoreCase("classname")) {
                    providerClass = (String) method.invoke(annotation);
                    break;
                }
            }
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failure on finding DataProvider class instance", e);
        }

        return providerClass;
    }

    /**
     * Initialize DataProvider based on className parameter.
     *
     * @param providerClass String full className.
     * @return DataProvider Instance.
     */
    private static Object initDataProvider(String providerClass) {
        Class<?> clazz;
        Object dataProvider = null;
        try {
            clazz = Class.forName(providerClass);
            Constructor<?> ctor = clazz.getConstructor();
            dataProvider = ctor.newInstance();
        } catch (Exception e) {
            LOGGER.error("DataProvider initialization failure", e);
        }

        return dataProvider;
    }

    private static Object[][] getGroupedList(Object[][] provider) {
        Object[][] finalProvider;
        if (GroupByMapper.isHashMapped()) {
            if (GroupByMapper.getInstanceStrings().size() == 1) {
                finalProvider = GroupByImpl.getGroupedDataProviderMap(provider, GroupByMapper.getInstanceStrings().iterator().next());
            } else {
                throw new GroupByException("Incorrect groupColumn annotation parameter!");
            }
        } else {
            if (GroupByMapper.getInstanceInt().size() == 1 && !GroupByMapper.getInstanceInt().contains(-1)) {

                finalProvider = GroupByImpl.getGroupedDataProviderArgs(provider, GroupByMapper.getInstanceInt().iterator().next());
            } else {
                throw new GroupByException("Incorrect groupColumn annotation  parameter!");
            }
        }

        return finalProvider;
    }

}
