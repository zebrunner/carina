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

import au.com.bytecode.opencsv.CSVReader;
import com.qaprosoft.carina.core.foundation.dataprovider.annotations.CsvDataSourceParameters;
import com.qaprosoft.carina.core.foundation.dataprovider.core.groupping.GroupByMapper;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.DSBean;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.csv.CSVParser;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.csv.CSVTable;
import com.zebrunner.carina.utils.ParameterGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.util.*;

/**
 * Created by Patotsky on 16.12.2014.
 */
public class CsvDataProvider extends BaseDataProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Create data for tests from csv data source by annotation and context parameters
     *
     * @param annotation Annotation csv data source parameters
     * @param context    ITestContext
     * @param testMethod ITestNGMethod
     *
     * @return Object[][] dataProvider
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object[][] getDataProvider(Annotation annotation, ITestContext context, ITestNGMethod testMethod) {
        CsvDataSourceParameters parameters = (CsvDataSourceParameters) annotation;
        DSBean dsBean = new DSBean(parameters, context.getCurrentXmlTest().getAllParameters());

        CSVTable csvTable = CSVParser.parseCsvFile(dsBean, parameters.separator(), parameters.quote());
        csvTable.excludeEntriesForNonExecution();

        String groupByParameter = dsBean.getGroupColumn();
        if (!groupByParameter.isEmpty()) {
            GroupByMapper.getInstanceInt().add(dsBean.getArgs().indexOf(groupByParameter));
            GroupByMapper.getInstanceStrings().add(groupByParameter);
        }
        GroupByMapper.setIsHashMapped(dsBean.isArgsToHashMap());

        return fillDataProviderWithData(csvTable, dsBean, testMethod);
    }
}
