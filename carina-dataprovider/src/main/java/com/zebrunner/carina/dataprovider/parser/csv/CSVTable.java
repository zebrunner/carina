package com.zebrunner.carina.dataprovider.parser.csv;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zebrunner.carina.utils.parser.xls.AbstractTable;

public class CSVTable extends AbstractTable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public CSVTable() {
        super();
    }

    public CSVTable(String executeColumn, String executeValue) {
        super(executeColumn, executeValue);
    }

    @Override
    public void addDataRow(List<String> row) {
        if (row.size() == headers.size()) {
            Map<String, String> rowMap = new HashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                rowMap.put(headers.get(i), row.get(i));
            }
            if (!rowMap.isEmpty()) {
                dataRows.add(rowMap);
            }
        } else {
            LOGGER.error("Headers size and row size didn't match, can't add data to table");
        }
    }

    public void excludeEntriesForNonExecution() {
        for (int i = 0; i < dataRows.size(); i++) {
            Map<String, String> row = dataRows.get(i);
            if (!row.get(executeColumn).equalsIgnoreCase(executeValue)) {
                dataRows.remove(i);
                i--;
            }
        }
    }
}
