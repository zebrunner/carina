package com.qaprosoft.carina.core.foundation.dataprovider.core.impl;

import au.com.bytecode.opencsv.CSVReader;
import com.qaprosoft.carina.core.foundation.dataprovider.annotations.CsvDataSourceParameters;
import com.qaprosoft.carina.core.foundation.dataprovider.core.groupping.GroupByMapper;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.DSBean;
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


    @Override
    public Object[][] getDataProvider(Annotation annotation, ITestContext context) {
        CsvDataSourceParameters parameters = (CsvDataSourceParameters) annotation;
        DSBean dsBean = new DSBean(parameters, context.getCurrentXmlTest().getAllParameters());

        String executeColumn, executeValue;
        char separator, quote;

        executeColumn = parameters.executeColumn();
        executeValue = parameters.executeValue();
        separator = parameters.separator();
        quote = parameters.quote();

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

        Map<String, Integer> mapper = getMapper(argsList, headers, executeColumn);
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

            int i = 0;
            for (String arg : argsList) {
                int index = mapper.get(arg);
                args[rowIndex][i] = strings[index];
                i++;
            }


            for (int j = 0; j < staticArgsList.size(); j++) {
                args[rowIndex][i + j] = getStaticParam(staticArgsList.get(j),context,dsBean);
            }

            rowIndex++;
        }

        return args;
    }


    private static Map<String, Integer> getMapper(List<String> argsList, List<String> headers, String filterColumn /*obligatory add filter column into mapper*/) {
        Map<String, Integer> mapper = new HashMap<String, Integer>();
        for (String arg : argsList) {
            int i = headers.indexOf(arg);
            if (i != -1) {
                mapper.put(arg, i);
            } else {
                throw new RuntimeException("Unable to find column '" + arg + "' in DataProvider among '" + headers + "'!  Verify separator and quote settings.");
            }
        }

        //TODO: code duplicate!
        int i = headers.indexOf(filterColumn);
        if (i != -1) {
            mapper.put(filterColumn, i);
        } else {
            throw new RuntimeException("Unable to find column '" + filterColumn + "' in DataProvider among '" + headers + "'!  Verify separator and quote settings.");
        }

        return mapper;
    }


}