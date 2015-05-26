package com.qaprosoft.carina.core.foundation.dataprovider.core.impl;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;

import org.testng.ITestContext;

import com.qaprosoft.carina.core.foundation.dataprovider.annotations.XlsDataSourceParameters;
import com.qaprosoft.carina.core.foundation.dataprovider.core.groupping.GroupByMapper;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.DSBean;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.XLSParser;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.XLSTable;
import com.qaprosoft.carina.core.foundation.utils.ParameterGenerator;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;

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
        staticArgsList = dsBean.getStaticArgs();
        
        if (parameters.dsArgs().isEmpty())
        {
            GroupByMapper.setIsHashMapped(true);
        }

        String groupByParameter = parameters.groupColumn();
        if (!groupByParameter.isEmpty()) {
            GroupByMapper.getInstanceInt().add(argsList.indexOf(groupByParameter));
            GroupByMapper.getInstanceStrings().add(groupByParameter);
        }

        String jiraColumn = context.getCurrentXmlTest().getParameter(SpecialKeywords.EXCEL_DS_JIRA);
        
        if (!parameters.jiraColumn().isEmpty())
        	jiraColumn = parameters.jiraColumn();
        
        String spiraColumn = "";
        if (!parameters.spiraColumn().isEmpty())
        	spiraColumn = parameters.spiraColumn();
        
        String testRailsColumn = "";
        if (!parameters.testRailColumn().isEmpty())
        	testRailsColumn = parameters.testRailColumn();

        
        String testMethodColumn = "";
        if (!parameters.testMethodColumn().isEmpty())
        	testMethodColumn = parameters.testMethodColumn();
        
        String testMethodOwnerColumn = "";
        if (!parameters.testMethodOwnerColumn().isEmpty())
        	testMethodOwnerColumn = parameters.testMethodOwnerColumn();

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
        			
            		Object param = ParameterGenerator.process(entry.getValue().toString());
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
                                    .get(argsList.get(i)));
                }
                //populate the rest of items by static parameters from testParams
                for (int j = 0; j < staticArgsList.size(); j++) {
                    args[rowIndex][i + j] = getStaticParam(staticArgsList.get(j), context, dsBean);
                }
            }
            // update testName adding UID values from DataSource arguments if any
            testName = dsBean.setDataSorceUUID(testName, xlsRow);


            if (testMethodColumn.isEmpty()) {
                testNameArgsMap.put(String.valueOf(Arrays.hashCode(args[rowIndex])), testName);
            } else {
	            // add testName value from xls datasource to special hashMap
	            addValueToSpecialMap(testNameArgsMap, testMethodColumn, String.valueOf(Arrays.hashCode(args[rowIndex])), xlsRow);
            }
            
            // add testMethoOwner from xls datasource to special hashMap
            addValueToSpecialMap(testMethodOwnerArgsMap, testMethodOwnerColumn, String.valueOf(Arrays.hashCode(args[rowIndex])), xlsRow);

            // add jira ticket from xls datasource to special hashMap
            addValueToSpecialMap(jiraArgsMap, jiraColumn, String.valueOf(Arrays.hashCode(args[rowIndex])), xlsRow);
            
            // add spira steps from xls datasource to special hashMap
            addValueToSpecialMap(spiraArgsMap, spiraColumn, String.valueOf(Arrays.hashCode(args[rowIndex])), xlsRow);
            
            // add testrails cases from xls datasource to special hashMap
            addValueToSpecialMap(testRailsArgsMap, testRailsColumn, String.valueOf(Arrays.hashCode(args[rowIndex])), xlsRow);
           
            rowIndex++;
        }

        return args;
    }
    
    private void addValueToSpecialMap(Map<String,String> map, String column, String hashCode, Map<String,String> xlsRow) {
        if (column != null) {
            if (!column.isEmpty()) {
            	map.put(hashCode, xlsRow.get(column));
            }
        }    	
    }
}