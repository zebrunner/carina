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

import com.qaprosoft.carina.core.foundation.dataprovider.core.groupping.GroupByImpl;
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

    public Object[][] createGroupedDataProvider(List<List<Map<String, String>>> groupedList, DSBean dsBean, ITestNGMethod testMethod) {
        Object[][] dataProvider = declareDataProviderArray(groupedList, dsBean);

        if (dsBean.getArgs().isEmpty()) {
            fillArgsAsMap(dataProvider, groupedList, dsBean);
        } else {
            fillArgsAsGropedMap(dataProvider, groupedList, dsBean);
        }

        configureTestNamingVarsForGroupedProvider(dataProvider, dsBean, groupedList, testMethod);
        return dataProvider;
    }

    public Object[][] createDataProvider(AbstractTable table, DSBean dsBean, ITestNGMethod testMethod) {
        Object[][] dataProvider = declareDataProviderArray(table.getDataRows(), dsBean);

        if (dsBean.isArgsToMap()) {
            fillArgsAsMap(dataProvider, table.getDataRows(), dsBean);
        } else {
            fillArgsAsArray(dataProvider, table, dsBean);
        }

        configureTestNamingVars(dataProvider, dsBean, table, testMethod);

        return dataProvider;
    }

    private void fillArgsAsGropedMap(Object[][] dataProvider, List<List<Map<String, String>>> gropedList, DSBean dsBean) {
        List<String> argsToPass = dsBean.getArgs();
        for (int rowIndex = 0; rowIndex < gropedList.size(); rowIndex++) {
            List<Map<String, String>> listToPass = new ArrayList<>();
            for (Map<String, String> groupedMap : gropedList.get(rowIndex)) {
                Map<String, String> mapToPass = new HashMap<>();
                for (String argName : argsToPass) {
                    mapToPass.put(argName, groupedMap.get(argName));
                }
                listToPass.add(mapToPass);
            }
            dataProvider[rowIndex][0] = listToPass;

            for (int staticArgsColumn = 0; staticArgsColumn < dsBean.getStaticArgs().size(); staticArgsColumn++) {
                String staticArgName = dsBean.getStaticArgs().get(staticArgsColumn);
                dataProvider[rowIndex][staticArgsColumn + 1] = getStaticParam(staticArgName, dsBean);
            }
        }
    }

    private void fillArgsAsMap(Object[][] dataProvider, List<?> dataRows, DSBean dsBean) {
        for (int rowIndex = 0; rowIndex < dataProvider.length; rowIndex++) {
            // populate arguments by parameters from data source
            dataProvider[rowIndex][0] = dataRows.get(rowIndex);

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

    private Object[][] declareDataProviderArray(List<?> dataRows, DSBean dsBean) {
        int numberOfRowsToExecute = dataRows.size();

        int numberOfArgsInTest;
        if (dsBean.isArgsToMap()) {
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
            addValueToMap(testColumnNamesMap, rowHash, getValueFromRow(row, List.of(dsBean.getTestMethodColumn())));
        }
    }

    private void configureTestNamingVarsForGroupedProvider(Object[][] dataProvider,
                                                           DSBean dsBean,
                                                           List<List<Map<String, String>>> groupedList,
                                                           ITestNGMethod testNGMethod) {
        for (int rowIndex = 0; rowIndex < dataProvider.length; rowIndex++) {

            String rowHash = hash(dataProvider[rowIndex], testNGMethod);

            String testUid = getValueFromGroupList(rowIndex, groupedList, dsBean.getUidArgs());
            addValueToMap(tuidMap, rowHash, testUid);

            String testName = getValueFromGroupList(rowIndex, groupedList, List.of(dsBean.getTestMethodColumn()));
            addValueToMap(testColumnNamesMap, rowHash, testName.split(",")[0]);
        }
    }

    private String getValueFromGroupList(int rowIndex, List<List<Map<String, String>>> groupedList, List<String> columnNames) {
        Set<String> values = new LinkedHashSet<>();
        List<Map<String, String>> dataRowList = groupedList.get(rowIndex);

        for (Map<String, String> dataMap : dataRowList) {
            values.add(getValueFromRow(dataMap, columnNames));
        }

        StringBuilder valueRes = new StringBuilder(String.join(",", values));
        if (valueRes.indexOf(",") != -1) {
            valueRes.replace(valueRes.length() - 1, valueRes.length(), "");
        }
        return valueRes.toString();
    }

    public String getValueFromRow(Map<String, String> row, List<String> columnNames) {
        StringBuilder valueRes = new StringBuilder();

        for (String key : columnNames) {
            if (!key.isEmpty() && row.containsKey(key)) {
                String value = row.get(key);
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
