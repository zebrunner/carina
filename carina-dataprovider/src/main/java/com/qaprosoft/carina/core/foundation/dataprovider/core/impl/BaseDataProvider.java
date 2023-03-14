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

import com.qaprosoft.carina.core.foundation.dataprovider.parser.AbstractTable;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.DSBean;
import com.zebrunner.carina.utils.ParameterGenerator;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Created by Patotsky on 19.12.2014.
 */

public abstract class BaseDataProvider {
    protected Map<String, String> tuidMap = Collections.synchronizedMap(new HashMap<>());
    protected Map<String, String> testColumnNamesMap = Collections.synchronizedMap(new HashMap<>());

    public abstract Object[][] getDataProvider(Annotation annotation, ITestContext context, ITestNGMethod testMethod);

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

    protected Object[][] fillDataProviderWithData(AbstractTable table, DSBean dsBean, ITestNGMethod testMethod) {
        processTable(table);

        Object[][] dataProvider = declareDataProviderArray(table, dsBean);

        if (dsBean.isArgsToHashMap()) {
            fillArgsAsHashMap(dataProvider, table, dsBean);
        } else {
            fillArgsAsArray(dataProvider, table, dsBean);
        }

        configureTestNamingVars(dataProvider, dsBean, table, testMethod);

        return dataProvider;
    }

    private void processTable(AbstractTable table) {
        for (Map<String, String> row : table.getDataRows()) {
            ParameterGenerator.processMap(row);
        }
    }

    private void fillArgsAsHashMap(Object[][] dataProvider, AbstractTable table, DSBean dsBean) {
        for (int rowIndex = 0; rowIndex < dataProvider.length; rowIndex++) {
            Map<String, String> row = table.getDataRows().get(rowIndex);

            // populate arguments by parameters from data source
            dataProvider[rowIndex][0] = row;

            // populate the rest of arguments by static parameters from testParams
            for (int staticArgsColumn = 0; staticArgsColumn < dsBean.getStaticArgs().size(); staticArgsColumn++) {
                String staticArgName = dsBean.getStaticArgs().get(staticArgsColumn);
                dataProvider[rowIndex][staticArgsColumn + 1] = getStaticParam(staticArgName, dsBean);
            }
        }
    }

    private void fillArgsAsArray(Object[][] dataProvider, AbstractTable table, DSBean dsBean) {
        for (int rowIndex = 0; rowIndex < dataProvider.length; rowIndex++) {
            Map<String, String> row = table.getDataRows().get(rowIndex);

            // populate arguments by parameters from data source
            for (int argsColumn = 0; argsColumn < dsBean.getArgs().size(); argsColumn++) {
                dataProvider[rowIndex][argsColumn] = row.get(dsBean.getArgs().get(argsColumn));
            }

            // populate the rest of arguments by static parameters from testParams
            for (int staticArgsColumn = 0; staticArgsColumn < dsBean.getStaticArgs().size(); staticArgsColumn++) {
                String staticArgName = dsBean.getStaticArgs().get(staticArgsColumn);
                dataProvider[rowIndex][staticArgsColumn + row.entrySet().size()] = getStaticParam(staticArgName, dsBean);
            }
        }
    }

    private Object[][] declareDataProviderArray(AbstractTable table, DSBean dsBean) {
        int numberOfRowsToExecute = table.getDataRows().size();

        int numberOfArgsInTest;
        if (dsBean.isArgsToHashMap()) {
            // first element is dynamic HashMap<String, String>
            numberOfArgsInTest = 1 + dsBean.getStaticArgs().size();
        } else {
            numberOfArgsInTest = dsBean.getArgs().size() + dsBean.getStaticArgs().size();
        }

        return new Object[numberOfRowsToExecute][numberOfArgsInTest];
    }

    private void configureTestNamingVars(Object[][] dataProvider, DSBean dsBean, AbstractTable table, ITestNGMethod testNGMethod) {
        for (int rowIndex = 0; rowIndex < dataProvider.length; rowIndex++) {
            Map<String, String> row = table.getDataRows().get(rowIndex);

            String rowHash = hash(dataProvider[rowIndex], testNGMethod);
            addValueToMap(tuidMap, rowHash, getValueFromRow(row, dsBean.getUidArgs()));
            addValueToMap(testColumnNamesMap, rowHash, getValueFromRow(row, dsBean.getTestMethodColumn()));
        }
    }

    protected static Object getStaticParam(String name, DSBean dsBean) {
        return ParameterGenerator.process(dsBean.getTestParams().get(name));
    }

    public Map<String, String> getTestColumnNamesMap() {
        return testColumnNamesMap;
    }

    public Map<String, String> getTuidMap() {
        return tuidMap;
    }

}
