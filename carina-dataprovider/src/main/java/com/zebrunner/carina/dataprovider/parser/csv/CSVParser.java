package com.zebrunner.carina.dataprovider.parser.csv;

import au.com.bytecode.opencsv.CSVReader;
import com.zebrunner.carina.dataprovider.parser.DSBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;

public class CSVParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static CSVTable parseCsvFile(DSBean dsBean, char separator, char quote) {
        CSVTable csvTable = new CSVTable(dsBean.getExecuteColumn(), dsBean.getExecuteValue());

        List<String[]> data = readData(dsBean, separator, quote);
        if (data.size() == 0) {
            throw new RuntimeException("Unable to retrieve data from CSV DataProvider! Verify separator and quote settings.");
        }

        csvTable.setHeaders(Arrays.asList(data.get(0)));

        mapData(csvTable, data);

        return csvTable;
    }

    @SuppressWarnings("unchecked")
    private static List<String[]> readData(DSBean dsBean, char separator, char quote) {
        CSVReader reader = null;
        List<String[]> list = new ArrayList<>();
        try {
            String csvFile = ClassLoader.getSystemResource(dsBean.getDsFile()).getFile();
            reader = new CSVReader(new FileReader(csvFile), separator, quote);
            list = reader.readAll();
        } catch (IOException e) {
            LOGGER.error("Unable to read data from CSV DataProvider", e);
        } finally {
            try {
                assert reader != null;
                reader.close();
            } catch (IOException e) {
                LOGGER.error("Unable to close CSV Reader", e);
            }
        }
        return list;
    }

    private static void mapData(CSVTable table, List<String[]> data) {
        for (int i = 1; i < data.size(); i ++) {
            table.addDataRow(List.of(data.get(i)));
        }
    }
}
