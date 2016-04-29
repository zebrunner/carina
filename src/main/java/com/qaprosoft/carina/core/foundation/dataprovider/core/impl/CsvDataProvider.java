package com.qaprosoft.carina.core.foundation.dataprovider.core.impl;

import au.com.bytecode.opencsv.CSVReader;
import com.qaprosoft.carina.core.foundation.dataprovider.annotations.CsvDataSourceParameters;
import com.qaprosoft.carina.core.foundation.dataprovider.core.groupping.GroupByMapper;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.DSBean;
import com.qaprosoft.carina.core.foundation.report.spira.Spira;
import org.apache.log4j.Logger;
import org.testng.ITestContext;

import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;

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

        executeColumn = dsBean.getExecuteColumn();
        executeValue = dsBean.getExecuteValue();
        
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

        // handle empty argsList inside initMapper
        mapper = initMapper(argsList, headers);
        list.remove(0);

        //exclude those lines which don't satisfy executeColumn/executeValue filter
        Iterator<String[]> iter = list.iterator();
        while (iter.hasNext()) {
            int index = mapper.get(executeColumn);

            String[] line = iter.next();
            if (!line[index].equalsIgnoreCase(executeValue)) {
                iter.remove();
            }
        }

        int listSize = list.size();

        int width = 0;
        if (argsList.size() == 0) {
        	//first element is dynamic HashMap<String, String>
            width = staticArgsList.size() + 1;
        } else {
            width = argsList.size() + staticArgsList.size();
        }
        
        Object[][] args = new Object[listSize][width];
        int rowIndex = 0;
        for (String[] strings : list) {
        	String testName = context.getName();

            int i = 0;
        	if (argsList.size() == 0) {
                //read all csv data into the single HashMap<String, String> object
        		HashMap<String, String> dynamicAttrs = new HashMap<String, String> ();
        		
                for (String header : headers) {
                	int index = mapper.get(header);
                	dynamicAttrs.put(header,  strings[index]);
                	
                	args[rowIndex][0] = dynamicAttrs;
                }

                i++;
            } else {
                for (String arg : argsList) {
                    int index = mapper.get(arg);
                    args[rowIndex][i] = strings[index];
                    i++;
                }
            }

            for (int j = 0; j < staticArgsList.size(); j++) {
                args[rowIndex][i + j] = getStaticParam(staticArgsList.get(j),context,dsBean);
            }

            // update testName adding UID values from DataSource arguments if any
            testName = dsBean.setDataSorceUUID(testName, strings, mapper); //provide whole line from data provider for UUID generation
            
            HashMap<String,String> csvRow = (HashMap<String, String>) args[rowIndex][0];
            
            if (testMethodColumn.isEmpty()) {
            	testNameArgsMap.put(String.valueOf(Arrays.hashCode(args[rowIndex])), testName); //provide organized args to generate valid hash
            } else {
	            // add testName value from csv datasource to special hashMap
	            addValueToSpecialMap(testNameArgsMap, testMethodColumn, String.valueOf(Arrays.hashCode(args[rowIndex])), csvRow);
	            addValueToSpecialMap(testMethodNameArgsMap, testMethodColumn, String.valueOf(Arrays.hashCode(args[rowIndex])), csvRow);
            }

            // add testMethoOwner from xls datasource to special hashMap
            addValueToSpecialMap(testMethodOwnerArgsMap, testMethodOwnerColumn, String.valueOf(Arrays.hashCode(args[rowIndex])), csvRow);

            // add jira ticket from xls datasource to special hashMap
            addValueToSpecialMap(jiraArgsMap, jiraColumn, String.valueOf(Arrays.hashCode(args[rowIndex])), csvRow);
            
            //TODO: need restore spiraArgsMap manipulations as transfering spiraIDes from DataProvider should be corrupted 
            // // add spira steps from xls datasource to special hashMap
            // addValueToSpecialMap(spiraArgsMap, spiraColumn, String.valueOf(Arrays.hashCode(args[rowIndex])), csvRow);

            
            if (!spiraColumn.isEmpty()) {
            	//register Spira ID values from DataProvider
            	Spira.setSteps(csvRow.get(spiraColumn));
            }
            
            // add testrails cases from xls datasource to special hashMap
            addValueToSpecialMap(testRailsArgsMap, testRailColumn, String.valueOf(Arrays.hashCode(args[rowIndex])), csvRow);
            
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
        
        if (argsList.size() == 0) {
        	// read all columns and put their name into the mapper
            for (String arg : headers) {
            	mapper.put(arg, getIndex(arg, headers));
            }
        } else {
            for (String arg : argsList) {
            	mapper.put(arg, getIndex(arg, headers));
            }
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
    
    private void addValueToSpecialMap(Map<String,String> map, String column, String hashCode, Map<String,String> csvRow) {
        if (column != null) {
            if (!column.isEmpty()) {
            	map.put(hashCode, csvRow.get(column));
            }
        }    	
    }    

}