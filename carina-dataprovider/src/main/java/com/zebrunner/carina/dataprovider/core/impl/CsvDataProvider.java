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
package com.zebrunner.carina.dataprovider.core.impl;

import com.zebrunner.carina.dataprovider.annotations.CsvDataSourceParameters;
import com.zebrunner.carina.dataprovider.parser.DSBean;
import com.zebrunner.carina.dataprovider.parser.csv.CSVParser;
import com.zebrunner.carina.dataprovider.parser.csv.CSVTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

/**
 * Created by Patotsky on 16.12.2014.
 */
public class CsvDataProvider extends BaseDataProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Create data provider for test based on csv data source, suite and annotation parameters
     *
     * @param annotation Annotation csv data source parameters
     * @param context    ITestContext suite data source parameters
     * @param testMethod ITestNGMethod current test method
     * @return Object[][] dataProvider
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object[][] getDataProvider(Annotation annotation, ITestContext context, ITestNGMethod testMethod) {
        CsvDataSourceParameters parameters = (CsvDataSourceParameters) annotation;
        DSBean dsBean = new DSBean(parameters, context.getCurrentXmlTest().getAllParameters());

        CSVTable csvTable = CSVParser.parseCsvFile(dsBean, parameters.separator(), parameters.quote());
        csvTable.excludeEntriesForNonExecution();
        csvTable.processTable();

        String groupColumn = dsBean.getGroupColumn();
        if (groupColumn.isEmpty()) {
            return createDataProvider(csvTable, dsBean, testMethod);
        } else {
            List<List<Map<String, String>>> groupedList = csvTable.getGroupedDataProviderMap(groupColumn);
            dsBean.setArgsToMap(true);
            return createGroupedDataProvider(groupedList, dsBean, testMethod);
        }
    }
}
