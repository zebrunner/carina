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
    protected Map<String, String> testColumnNamesMap = Collections.synchronizedMap(new HashMap<>());

    public abstract Object[][] getDataProvider(Annotation annotation, ITestContext context, ITestNGMethod testMethod);

    protected static Object getStaticParam(String name, DSBean dsBean) {
        return ParameterGenerator.process(dsBean.getTestParams().get(name));
    }

    public Map<String, String> getTestColumnNamesMap() {
        return testColumnNamesMap;
    }

    public Map<String, String> getTuidMap() {
        return tuidMap;
    }

    protected void addValueToMap(Map<String, String> map, String hashCode, String value) {
        if (!value.isEmpty()) {
            map.put(hashCode, value);
        }
    }

    /**
     * Generates hash by class name, method name and arg values.
     *
     * @param args   Object[] test method arguments
     * @param method ITestNGMethod
     * @return String hash
     */
    public static String hash(Object[] args, ITestNGMethod method) {
        String toHash = "";
        toHash += Arrays.hashCode(args);
        toHash += method.getMethodName();
        toHash += (method.getRealClass());
        return String.valueOf(toHash.hashCode());
    }

    /**
     * Get value from data source in specified row.
     *
     * @param dataRow Map<String, String> test method arguments/ record from source table
     * @param key     String argument name/ column name from source table
     * @return String "value " from record
     */
    protected String getValueFromRow(Map<String, String> dataRow, String key) {
        return getValueFromRow(dataRow, List.of(key));
    }

    /**
     * Get value from data source in specified row from several columns.
     *
     * @param dataRow Map<String, String> test method arguments/ record from source table
     * @param keys    List<String> argument names/ column names from source table
     * @return String "value1, value2, ..., valueN " from record
     */
    protected String getValueFromRow(Map<String, String> dataRow, List<String> keys) {
        StringBuilder valueRes = new StringBuilder();

        for (String key : keys) {
            if (!key.isEmpty() && dataRow.containsKey(key)) {
                String value = dataRow.get(key);
                if (value != null && !value.isEmpty()) {
                    valueRes.append(value);
                    valueRes.append(",");
                }
            }
        }

        if (valueRes.indexOf(",") != -1) {
            valueRes.replace(valueRes.length() - 1, valueRes.length(), "");
        }
        return valueRes.toString();
    }

}
