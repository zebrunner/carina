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

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import com.zebrunner.carina.dataprovider.annotations.XlsDataSourceParameters;
import com.zebrunner.carina.dataprovider.parser.DSBean;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import com.zebrunner.carina.utils.parser.xls.XLSParser;
import com.zebrunner.carina.utils.parser.xls.XLSTable;

/**
 * Created by Patotsky on 16.12.2014.
 */
public class XlsDataProvider extends BaseDataProvider {

    /**
     * Create data provider for test based on xls data source, suite and annotation parameters
     *
     * @param annotation Annotation xls data source parameters
     * @param context    ITestContext suite data source parameters
     * @param testMethod ITestNGMethod current test method
     * @return Object[][] dataProvider
     */
    @Override
    public Object[][] getDataProvider(Annotation annotation, ITestContext context, ITestNGMethod testMethod) {
        XlsDataSourceParameters parameters = (XlsDataSourceParameters) annotation;
        DSBean dsBean = new DSBean(parameters, context.getCurrentXmlTest().getAllParameters());

        XLSTable xlsTable = XLSParser.parseSpreadSheet(dsBean.getDsFile(), dsBean.getXlsSheet(), dsBean.getExecuteColumn(), dsBean.getExecuteValue());
        xlsTable.processTable();

        String groupColumn = dsBean.getGroupColumn();
        if (groupColumn.isEmpty()) {
            return createDataProvider(xlsTable, dsBean, testMethod);
        } else {
            List<List<Map<String, String>>> groupedList = xlsTable.getGroupedDataProviderMap(groupColumn);
            dsBean.setArgsToMap(true);
            return createGroupedDataProvider(groupedList, dsBean, testMethod);
        }
    }
}
