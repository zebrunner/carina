package com.qaprosoft.carina.core.foundation.dataprovider.core;

import com.qaprosoft.carina.core.foundation.dataprovider.annotations.CsvDataSourceParameters;
import com.qaprosoft.carina.core.foundation.dataprovider.annotations.XlsDataSourceParameters;

/**
 * Created by Yauheni_Patotski on 12/18/2014.
 */
public enum DataProviderType {

    XLS_DATA_SOURCE(XlsDataSourceParameters.class.getSimpleName()), CSV_DATA_SOURCE(CsvDataSourceParameters.class.getSimpleName()),UNKNOWN("");


    private final String className;

    private DataProviderType(final String className) {
        this.className = className;
    }

    public static DataProviderType fromString(String text) {
        if (text != null) {
            for (DataProviderType dataProviderType : DataProviderType.values()) {
                if (text.equalsIgnoreCase(dataProviderType.className)) {
                    return dataProviderType;
                }
            }
        }
        return UNKNOWN;
    }
}
