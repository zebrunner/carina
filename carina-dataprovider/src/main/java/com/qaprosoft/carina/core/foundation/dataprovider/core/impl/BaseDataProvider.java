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
package com.qaprosoft.carina.core.foundation.dataprovider.core.impl;

import java.lang.annotation.Annotation;
import java.util.*;

import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import com.qaprosoft.carina.core.foundation.dataprovider.parser.DSBean;
import com.zebrunner.carina.utils.ParameterGenerator;

/**
 * Created by Patotsky on 19.12.2014.
 */

public abstract class BaseDataProvider {
    protected Map<String, String> tuidMap = Collections.synchronizedMap(new HashMap<>());
    protected Map<String, String> testNameArgsMap = Collections.synchronizedMap(new HashMap<>());
    protected Map<String, String> testMethodOwnerArgsMap = Collections.synchronizedMap(new HashMap<>());
    protected Map<String, String> testRailsArgsMap = Collections.synchronizedMap(new HashMap<>());
    protected Map<String, String> testColumnNamesMap = Collections.synchronizedMap(new HashMap<>());

    protected List<String> argsList;
    protected List<String> staticArgsList;

    public abstract Object[][] getDataProvider(Annotation annotation, ITestContext context, ITestNGMethod testMethod);

    protected static Object getStaticParam(String name, ITestContext context, DSBean dsBean) {
        return ParameterGenerator.process(dsBean
                .getTestParams().get(name));
    }

    public Map<String, String> getTestColumnNamesMap() {
        return testColumnNamesMap;
    }

    public Map<String, String> getTuidMap() {
        return tuidMap;
    }

    public Map<String, String> getTestMethodOwnerArgsMap() {
        return testMethodOwnerArgsMap;
    }

    public Map<String, String> getTestRailsArgsMap() {
        return testRailsArgsMap;
    }

    protected void addValueToSpecialMap(Map<String, String> map, String column, String hashCode, Map<String, String> row) {
        if (column != null) {
            if (!column.isEmpty()) {
                if (row.get(column) != null) {
                    if (!row.get(column).isEmpty()) {
                        // put into the args only non empty jira tickets
                        map.put(hashCode, row.get(column));
                    }
                }
            }
        }
    }

    public static String hash(Object[] args, ITestNGMethod method) {
        String toHash = "";
        toHash += Arrays.hashCode(args);
        toHash += method.getMethodName();
        toHash += (method.getRealClass());
        return String.valueOf(toHash.hashCode());
    }

    protected String getValueFromRow(Map<String, String> xlsRow, String key) {
        return getValueFromRow(xlsRow, List.of(key));
    }

    protected String getValueFromRow(Map<String, String> xlsRow, List<String> keys) {
        StringBuilder valueRes = new StringBuilder();

        for (String key : keys) {
            if (xlsRow.containsKey(key)) {
                String value = xlsRow.get(key);
                if (value != null && !value.isEmpty()) {
                    valueRes.append(value);
                    valueRes.append(", ");
                }
            }
        }

        if (valueRes.indexOf(",") != -1) {
            valueRes.replace(valueRes.length() - 2, valueRes.length() - 1, "");
        }
        return valueRes.toString();
    }

}
