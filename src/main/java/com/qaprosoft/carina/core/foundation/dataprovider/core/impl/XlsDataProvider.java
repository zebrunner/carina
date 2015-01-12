package com.qaprosoft.carina.core.foundation.dataprovider.core.impl;

import com.qaprosoft.carina.core.foundation.dataprovider.annotations.XlsDataSourceParameters;
import com.qaprosoft.carina.core.foundation.dataprovider.core.groupping.GroupByMapper;
import com.qaprosoft.carina.core.foundation.utils.ParameterGenerator;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.DSBean;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.XLSParser;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.XLSTable;
import org.testng.ITestContext;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Created by Patotsky on 16.12.2014.
 */
public class XlsDataProvider extends BaseDataProvider {


    @Override
    public Object[][] getDataProvider(Annotation annotation, ITestContext context) {


        XlsDataSourceParameters parameters = (XlsDataSourceParameters) annotation;

        DSBean dsBean = new DSBean(parameters, context
                .getCurrentXmlTest().getAllParameters());

        String executeColumn = "Execute";
        String executeValue = "y";

        if (!parameters.executeColumn().isEmpty())
            executeColumn = parameters.executeColumn();

        if (!parameters.executeValue().isEmpty())
            executeValue = parameters.executeValue();

        XLSTable dsData = XLSParser.parseSpreadSheet(dsBean.getDsFile(),
                dsBean.getXlsSheet(), executeColumn, executeValue);

        argsList = dsBean.getArgs();

        if (parameters.dsArgs().isEmpty() )
        {
            GroupByMapper.setIsHashMapped(true);
        }


        staticArgsList = dsBean.getStaticArgs();

        String groupByParameter = parameters.groupColumn();
        if (!groupByParameter.isEmpty()) {
            GroupByMapper.getInstanceInt().add(argsList.indexOf(groupByParameter));
            GroupByMapper.getInstanceStrings().add(groupByParameter);
        }

        String jiraColumnName = context.getCurrentXmlTest().getParameter(
                SpecialKeywords.EXCEL_DS_JIRA);

        int width = 0;
        if (argsList.size() == 0) {
            width = staticArgsList.size() + 1;
        } else {
            width = argsList.size() + staticArgsList.size();
        }
        Object[][] args = new Object[dsData.getDataRows().size()][width];

        int rowIndex = 0;
        for (Map<String, String> xlsRow : dsData.getDataRows()) {
            String testName = context.getName();

            if (argsList.size() == 0) {
            	//process each column in xlsRow data obligatory replacing special keywords like UUID etc
            	for (Map.Entry<String, String> entry : xlsRow.entrySet()) {
            		if (entry == null)
            			continue;
            		
            		String value = entry.getValue();
            		if (value == null)
            			continue;
        			
            		Object param = ParameterGenerator.process(entry.getValue().toString(), context.getAttribute(SpecialKeywords.UUID).toString());
        			if (param == null)
        				continue;
            		
        			String newValue = param.toString();
            		if (!value.equals(newValue)) {
            			entry.setValue(newValue);
            		}
            	}
                args[rowIndex][0] = xlsRow;
                for (int i = 0; i < staticArgsList.size(); i++) {
                    args[rowIndex][i + 1] = getStaticParam(staticArgsList.get(i), context, dsBean);
                }
            } else {
                int i;
                for (i = 0; i < argsList.size(); i++) {
                    args[rowIndex][i] = ParameterGenerator.process(xlsRow
                                    .get(argsList.get(i)),
                            context.getAttribute(SpecialKeywords.UUID)
                                    .toString());
                }
                //populate the rest of items by static parameters from testParams
                for (int j = 0; j < staticArgsList.size(); j++) {
                    args[rowIndex][i + j] = getStaticParam(staticArgsList.get(j), context, dsBean);
                }
            }
            // update testName adding UID values from DataSource arguments if any
            testName = dsBean.setDataSorceUUID(testName, xlsRow);


            testNameArgsMap.put(
                    String.valueOf(Arrays.hashCode(args[rowIndex])), testName);

            // add jira ticket from xls datasource to special hashMap
            if (jiraColumnName != null) {
                if (!jiraColumnName.isEmpty()) {
                    jiraArgsMap.put(
                            String.valueOf(Arrays.hashCode(args[rowIndex])),
                            xlsRow.get(jiraColumnName));
                }
            }

            rowIndex++;
        }

        return args;
    }
}