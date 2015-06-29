package com.qaprosoft.carina.core.foundation.dataprovider.core.impl;

import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.ITestContext;

import au.com.bytecode.opencsv.CSVReader;

import com.qaprosoft.carina.core.foundation.dataprovider.annotations.CsvDataSourceParameters;
import com.qaprosoft.carina.core.foundation.dataprovider.core.groupping.GroupByMapper;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.DSBean;
import com.qaprosoft.carina.core.foundation.report.spira.Spira;

/**
 * Created by Patotsky on 16.12.2014.
 */
public class CsvDataProvider extends BaseDataProvider {

    protected static final Logger LOGGER = Logger.getLogger(CsvDataProvider.class);
    private Map<String, Integer> mapper = new HashMap<String, Integer>();

    private String executeColumn; 
    private String executeValue;
    private String jiraColumn;
    private String spiraColumn;
    private String testRailColumn;
    private String testMethodColumn;
    private String testMethodOwnerColumn;
    
    @SuppressWarnings("unchecked")
	@Override
    public Object[][] getDataProvider(Annotation annotation, ITestContext context) {
        CsvDataSourceParameters parameters = (CsvDataSourceParameters) annotation;
        DSBean dsBean = new DSBean(parameters, context.getCurrentXmlTest().getAllParameters());

        char separator, quote;

        executeColumn = parameters.executeColumn();
        executeValue = parameters.executeValue();
        separator = parameters.separator();
        quote = parameters.quote();
        
        jiraColumn = parameters.jiraColumn();
        spiraColumn = parameters.spiraColumn();
        testRailColumn = parameters.testRailColumn();
        testMethodColumn = parameters.testMethodColumn();
        testMethodOwnerColumn = parameters.testMethodOwnerColumn();

        List<String> argsList = dsBean.getArgs();
        List<String> staticArgsList = dsBean.getStaticArgs();

        String groupByParameter = parameters.groupColumn();
        if (!groupByParameter.isEmpty()) {
            GroupByMapper.getInstanceInt().add(argsList.indexOf(groupByParameter));
            GroupByMapper.getInstanceStrings().add(groupByParameter);
        }

        if (parameters.dsArgs().isEmpty() )
        {
            GroupByMapper.setIsHashMapped(true);
        }
        CSVReader reader;
        List<String[]> list = new ArrayList<String[]>();

        try {
            String csvFile = ClassLoader.getSystemResource(dsBean.getDsFile()).getFile();
            reader = new CSVReader(new FileReader(csvFile), separator, quote);
            list = reader.readAll();
        } catch (IOException e) {
            LOGGER.error("Unable to read data from CSV DataProvider", e.getCause());
            e.printStackTrace();
        }

        if (list.size() == 0) {
            throw new RuntimeException("Unable to retrieve data from CSV DataProvider! Verify separator and quote settings.");
        }
        List<String> headers = Arrays.asList((String[]) list.get(0));

        mapper = initMapper(argsList, headers);
        list.remove(0);

        Iterator<String[]> iter = list.iterator();
        while (iter.hasNext()) {
            int index = mapper.get(executeColumn);

            String[] line = (String[]) iter.next();
            if (!line[index].equalsIgnoreCase(executeValue)) {
                iter.remove();
            }
        }

        int listSize = list.size();

        Object[][] args = new Object[listSize][argsList.size() + staticArgsList.size()];
        int rowIndex = 0;
        for (String[] strings : list) {
        	String testName = context.getName();

            int i = 0;
            for (String arg : argsList) {
                int index = mapper.get(arg);
                args[rowIndex][i] = strings[index];
                i++;
            }


            for (int j = 0; j < staticArgsList.size(); j++) {
                args[rowIndex][i + j] = getStaticParam(staticArgsList.get(j),context,dsBean);
            }

            // update testName adding UID values from DataSource arguments if any
            testName = dsBean.setDataSorceUUID(testName, strings, mapper); //provide whole line from data provider for UUID generation
            
            
            if (testMethodColumn.isEmpty()) {
            	testNameArgsMap.put(String.valueOf(Arrays.hashCode(args[rowIndex])), testName); //provide organized args to generate valid hash
            } else {
	            // add testName value from csv datasource to special hashMap
	            addValueToSpecialMap(testNameArgsMap, testMethodColumn, String.valueOf(Arrays.hashCode(args[rowIndex])), args[rowIndex]);
	            addValueToSpecialMap(testMethodNameArgsMap, testMethodColumn, String.valueOf(Arrays.hashCode(args[rowIndex])), args[rowIndex]);
            }

            // add testMethoOwner from xls datasource to special hashMap
            addValueToSpecialMap(testMethodOwnerArgsMap, testMethodOwnerColumn, String.valueOf(Arrays.hashCode(args[rowIndex])), args[rowIndex]);

            // add jira ticket from xls datasource to special hashMap
            addValueToSpecialMap(jiraArgsMap, jiraColumn, String.valueOf(Arrays.hashCode(args[rowIndex])), args[rowIndex]);
            
            //TODO: need restore spiraArgsMap manipulations as transfering spiraIDes from DataProvider should be corrupted 
            // // add spira steps from xls datasource to special hashMap
            // addValueToSpecialMap(spiraArgsMap, spiraColumn, String.valueOf(Arrays.hashCode(args[rowIndex])), args[rowIndex]);

            
            if (!spiraColumn.isEmpty()) {
            	//register Spira ID values from DataProvider
            	Spira.setSteps(args[rowIndex][mapper.get(spiraColumn)].toString());
            }
            
            // add testrails cases from xls datasource to special hashMap
            addValueToSpecialMap(testRailsArgsMap, testRailColumn, String.valueOf(Arrays.hashCode(args[rowIndex])), args[rowIndex]);
            
            rowIndex++;
        }

       
        return args;
    }

    /*
     * obligatory add to mapper all columns for DataProvider artifacts like:
     * executeColumn - filter column
     * jiraColumn
     * spiraColumn
     * testRailColumn
     * testMethodColumn
     * testMethodOwnerColumn
     * */
    private Map<String, Integer> initMapper(List<String> argsList, List<String> headers) {
        Map<String, Integer> mapper = new HashMap<String, Integer>();
        for (String arg : argsList) {
        	mapper.put(arg, getIndex(arg, headers));
        }

    	mapper.put(executeColumn, getIndex(executeColumn, headers));
    	mapper.put(jiraColumn, getIndex(jiraColumn, headers));
    	mapper.put(spiraColumn, getIndex(spiraColumn, headers));
    	mapper.put(testRailColumn, getIndex(testRailColumn, headers));
    	mapper.put(testMethodColumn, getIndex(testMethodColumn, headers));
    	mapper.put(testMethodOwnerColumn, getIndex(testMethodOwnerColumn, headers));
    	
        return mapper;
    }
    
    private Integer getIndex(String arg, List<String> headers) {
    	if (arg.isEmpty()) {
    		return -1;
    	}
    	
	    int index = headers.indexOf(arg);
	    if (index == -1) {
	    	throw new RuntimeException("Unable to find column '" + arg + "' in DataProvider among '" + headers + "'!  Verify separator and quote settings.");
	    }
	    return index;
    }
    
    private void addValueToSpecialMap(Map<String,String> map, String column, String hashCode, Object[] csvRow) {
        if (column != null) {
            if (!column.isEmpty()) {
            	map.put(hashCode, csvRow[mapper.get(column)].toString());
            }
        }    	
    }    
    


}