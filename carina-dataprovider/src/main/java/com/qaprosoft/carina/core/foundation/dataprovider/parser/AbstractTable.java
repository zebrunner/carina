package com.qaprosoft.carina.core.foundation.dataprovider.parser;

import com.zebrunner.carina.utils.ParameterGenerator;

import java.util.*;

public abstract class AbstractTable {
    protected List<String> headers;
    protected List<Map<String, String>> dataRows;
    protected String executeColumn;
    protected String executeValue;

    public AbstractTable() {
        headers = Collections.synchronizedList(new LinkedList<String>());
        dataRows = Collections.synchronizedList(new LinkedList<Map<String, String>>());
    }

    public AbstractTable(String executeColumn, String executeValue) {
        this();
        this.executeColumn = executeColumn;
        this.executeValue = executeValue;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<Map<String, String>> getDataRows() {
        return dataRows;
    }

    public String getExecuteColumn() {
        return executeColumn;
    }

    public void setExecuteColumn(String executeColumn) {
        this.executeColumn = executeColumn;
    }

    public String getExecuteValue() {
        return executeValue;
    }

    public void setExecuteValue(String executeValue) {
        this.executeValue = executeValue;
    }

    public void setHeaders(Collection<String> row) {
        headers.clear();
        headers.addAll(row);
    }

    public abstract void addDataRow(List<String> row);

    public void processTable() {
        for (Map<String, String> row : dataRows) {
            ParameterGenerator.processMap(row);
        }
    }

    public List<List<Map<String, String>>> getGroupedDataProviderMap(String fieldName) {
        //add unique group values
        Set<String> groupValues = new LinkedHashSet<>();
        for (Map<String, String> item : dataRows) {
            String value = item.get(fieldName);
            groupValues.add(value);
        }

        //group maps into lists, that has the same unique group value
        List<List<Map<String, String>>> groupedList = new ArrayList<>();
        for (String groupBy: groupValues) {
            List<Map<String, String>> groupOfRows = new ArrayList<>();
            for (Map<String, String> item : dataRows) {
                String value = item.get(fieldName);
                if (value.equals(groupBy)) {
                    groupOfRows.add(item);
                }
            }
            groupedList.add(groupOfRows);
        }

        return groupedList;
    }
}
