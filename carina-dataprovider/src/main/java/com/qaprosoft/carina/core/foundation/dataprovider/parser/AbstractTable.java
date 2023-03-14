package com.qaprosoft.carina.core.foundation.dataprovider.parser;

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
}
