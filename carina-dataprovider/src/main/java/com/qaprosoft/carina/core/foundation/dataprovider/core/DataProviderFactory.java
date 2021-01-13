/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
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

        Map<String, String> testNameArgsMap = Collections.synchronizedMap(new HashMap<>());
        Map<String, String> testMethodOwnerArgsMap = Collections.synchronizedMap(new HashMap<>());
        Map<String, String> jiraArgsMap = Collections.synchronizedMap(new HashMap<>());
        Map<String, String> testRailsArgsMap = Collections.synchronizedMap(new HashMap<>());
        List<String> doNotRunTests = Collections.synchronizedList(new ArrayList<>());

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
                    testNameArgsMap.putAll(activeProvider.getTestNameArgsMap());
                    testMethodOwnerArgsMap.putAll(activeProvider.getTestMethodOwnerArgsMap());
                    jiraArgsMap.putAll(activeProvider.getJiraArgsMap());
                    testRailsArgsMap.putAll(activeProvider.getTestRailsArgsMap());
                    doNotRunTests.addAll(activeProvider.getDoNotRunRowsIDs());
                }

            } catch (Exception e) {
                LOGGER.error("DataProvider failure", e);
            }
        }

        if (!GroupByMapper.getInstanceInt().isEmpty() || !GroupByMapper.getInstanceStrings().isEmpty()) {
            provider = getGroupedList(provider);
        }

        context.setAttribute(SpecialKeywords.TEST_NAME_ARGS_MAP, testNameArgsMap);
        context.setAttribute(SpecialKeywords.JIRA_ARGS_MAP, jiraArgsMap);

        // clear group by settings
        GroupByMapper.getInstanceInt().clear();
        GroupByMapper.getInstanceStrings().clear();

        return provider;
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
