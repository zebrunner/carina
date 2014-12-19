package com.qaprosoft.carina.core.foundation.dataprovider.core.impl;

import au.com.bytecode.opencsv.CSVReader;
import com.qaprosoft.carina.core.foundation.dataprovider.annotations.CsvDataSourceParameters;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.DSBean;
import org.testng.ITestContext;

import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Created by Patotsky on 16.12.2014.
 */
public class CSVDataProvider extends BaseDataProvider {


    @Override
    public Object[][] getDataProvider(Annotation annotation, ITestContext context) {
        CsvDataSourceParameters parameters = (CsvDataSourceParameters) annotation;
        DSBean dsBean = new DSBean(parameters, context
                .getCurrentXmlTest().getAllParameters());
        String executeColumn = "Execute";
        String executeValue = "Y";

        if (!parameters.executeColumn().isEmpty())
            executeColumn = parameters.executeColumn();

        if (!parameters.executeValue().isEmpty())
            executeValue = parameters.executeValue();

        List<String> argsList = dsBean.getArgs();
        List<String> staticArgsList = dsBean.getStaticArgs();

        CSVReader reader;
        List<String[]> list = new ArrayList();
        try {
            reader = new CSVReader(new FileReader(ClassLoader.getSystemResource(dsBean.getDsFile()).getFile()),';');
            list = reader.readAll();
        } catch (IOException e) {

            e.printStackTrace();
        }
        List<String> headers = Arrays.asList((String[]) list.get(0));
        Map<String, Integer> mapper = getMapper(argsList, headers);
        list.remove(0);

        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            String[] d = (String[]) iter.next();
            if (!d[0].equals(executeValue)) {
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


    private static Map<String, Integer> getMapper(List<String> argsList, List<String> headers) {
        Map<String, Integer> mapper = new HashMap<String, Integer>();
        for (String arg : argsList) {
            int i = headers.indexOf(arg);
            if (i != -1) {
                mapper.put(arg, i);
            } else {
                throw new RuntimeException("NOOB!!!!");
            }
        }
        return mapper;
    }


}
