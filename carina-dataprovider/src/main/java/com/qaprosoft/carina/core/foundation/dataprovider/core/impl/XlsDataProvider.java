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
import java.util.Arrays;
import java.util.Map;

import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import com.qaprosoft.carina.core.foundation.dataprovider.annotations.XlsDataSourceParameters;
import com.qaprosoft.carina.core.foundation.dataprovider.core.groupping.GroupByMapper;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.DSBean;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.XLSParser;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.XLSTable;
import com.zebrunner.carina.utils.ParameterGenerator;

/**
 * Created by Patotsky on 16.12.2014.
 */
public class XlsDataProvider extends BaseDataProvider {

    /**
     * Create data for tests from xls data source by annotation and context parameters
     *
     * @param annotation Annotation xls data source parameters
     * @param context ITestContext
     * @param testMethod ITestNGMethod
     *
     * @return Object[][] dataProvider
     */
    @Override
    public Object[][] getDataProvider(Annotation annotation, ITestContext context, ITestNGMethod testMethod) {

        XlsDataSourceParameters parameters = (XlsDataSourceParameters) annotation;

        DSBean dsBean = new DSBean(parameters, context.getCurrentXmlTest().getAllParameters());

        XLSTable dsData = XLSParser.parseSpreadSheet(dsBean.getDsFile(), dsBean.getXlsSheet(), dsBean.getExecuteColumn(), dsBean.getExecuteValue());


        if (dsBean.getArgs().isEmpty()) {
            GroupByMapper.setIsHashMapped(true);
        }

        String groupByParameter = dsBean.getGroupColumn();
        if (!groupByParameter.isEmpty()) {
            GroupByMapper.getInstanceInt().add(dsBean.getArgs().indexOf(groupByParameter));
            GroupByMapper.getInstanceStrings().add(groupByParameter);
        }

        int width = 0;
        if (dsBean.getArgs().size() == 0) {
            width = dsBean.getStaticArgs().size() + 1;
        } else {
            width = dsBean.getArgs().size() + dsBean.getStaticArgs().size();
        }
        Object[][] args = new Object[dsData.getDataRows().size()][width];

        int rowIndex = 0;
        for (Map<String, String> xlsRow : dsData.getDataRows()) {

            if (dsBean.getArgs().size() == 0) {
                // process each column in xlsRow data obligatory replacing special keywords like UUID etc
                for (Map.Entry<String, String> entry : xlsRow.entrySet()) {
                    if (entry == null)
                        continue;

                    String value = entry.getValue();
                    if (value == null)
                        continue;

                    Object param = ParameterGenerator.process(value);
                    if (param == null)
                        continue;

                    String newValue = param.toString();
                    if (!value.equals(newValue)) {
                        entry.setValue(newValue);
                    }
                }
                args[rowIndex][0] = xlsRow;
                for (int i = 0; i < dsBean.getStaticArgs().size(); i++) {
                    args[rowIndex][i + 1] = getStaticParam(dsBean.getStaticArgs().get(i), dsBean);
                }
            } else {
                int i;
                for (i = 0; i < dsBean.getArgs().size(); i++) {
                    args[rowIndex][i] = ParameterGenerator.process(xlsRow.get(dsBean.getArgs().get(i)));
                }
                // populate the rest of items by static parameters from testParams
                for (int j = 0; j < dsBean.getStaticArgs().size(); j++) {
                    args[rowIndex][i + j] = getStaticParam(dsBean.getStaticArgs().get(j), dsBean);
                }
            }
            String recordHash = hash(args[rowIndex], testMethod);
            addValueToMap(tuidMap, recordHash, getValueFromRow(xlsRow, dsBean.getUidArgs()));
            addValueToMap(testColumnNamesMap, recordHash, getValueFromRow(xlsRow, dsBean.getTestMethodColumn()));

            rowIndex++;
        }

        return args;
    }
}
