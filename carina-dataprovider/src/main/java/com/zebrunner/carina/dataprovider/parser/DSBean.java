/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.zebrunner.carina.dataprovider.parser;

import com.zebrunner.carina.dataprovider.annotations.CsvDataSourceParameters;
import com.zebrunner.carina.dataprovider.annotations.XlsDataSourceParameters;
import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.utils.exception.InvalidArgsException;
import org.testng.ITestContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DSBean {
    private Map<String, String> testParams;
    private List<String> args = new ArrayList<>();
    private List<String> uidArgs = new ArrayList<>();
    private List<String> staticArgs = new ArrayList<>();

    private String dsFile;
    private String xlsSheet;
    private String executeColumn;
    private String executeValue;
    private boolean spreadsheet;
    private String groupColumn;
    private String testMethodColumn;
    private boolean argsToMap;

    public DSBean(ITestContext context) {
        initParamsFromSuite(context.getCurrentXmlTest().getAllParameters(), "excel");
        this.argsToMap = this.args.size() == 0;
    }

    public DSBean(XlsDataSourceParameters xlsDataSourceParameters, Map<String, String> suiteParams) {
        // params initialize order: 1) from test annotation 2) from suite
        if (xlsDataSourceParameters != null) {
            this.initParamsFromAnnotation(xlsDataSourceParameters);
            if (!xlsDataSourceParameters.sheet().isEmpty()) {
                xlsSheet = xlsDataSourceParameters.sheet();
            }
        }

        if (!suiteParams.isEmpty()) {
            initParamsFromSuite(suiteParams, "excel");
            if (suiteParams.get(SpecialKeywords.EXCEL_DS_SHEET) != null) {
                this.xlsSheet = suiteParams.get(SpecialKeywords.EXCEL_DS_SHEET);
            }
        }

        if (xlsDataSourceParameters != null && !xlsDataSourceParameters.spreadsheetId().isEmpty()) {
            if (!this.dsFile.isEmpty()) {
                throw new InvalidArgsException("Spreadsheet id and path parameters are mutually exclusive");
            } else {
                this.dsFile = xlsDataSourceParameters.spreadsheetId();
                this.spreadsheet = true;
            }
        }

        this.testParams = suiteParams;
        this.argsToMap = this.args.size() == 0;
    }

    public DSBean(CsvDataSourceParameters csvDataSourceParameters, Map<String, String> suiteParams) {
        // params initialize order: 1) from test annotation 2) from suite
        if (csvDataSourceParameters != null) {
            this.initParamsFromAnnotation(csvDataSourceParameters);
        }

        if (!suiteParams.isEmpty()) {
            initParamsFromSuite(suiteParams, "");
        }
        this.testParams = suiteParams;
        this.xlsSheet = null;
        this.argsToMap = this.args.size() == 0;
    }

    private void initParamsFromAnnotation(XlsDataSourceParameters parameters) {
        // initialize default xls data source parameters from annotation
        this.dsFile = parameters.path();
        this.executeColumn = parameters.executeColumn();
        this.executeValue = parameters.executeValue();
        this.groupColumn = parameters.groupColumn();
        this.testMethodColumn = parameters.testMethodColumn();

        if (!parameters.dsArgs().isEmpty()) {
            this.args = Arrays.asList(parameters.dsArgs().replace(" ", "").split(","));
        }
        if (!parameters.dsUid().isEmpty()) {
            this.uidArgs = Arrays.asList(parameters.dsUid().replace(" ", "").split(","));
        }
        if (!parameters.staticArgs().isEmpty()) {
            this.staticArgs = Arrays.asList(parameters.staticArgs().replace(" ", "").split(","));
        }

    }

    private void initParamsFromAnnotation(CsvDataSourceParameters parameters) {
        // initialize default xls data source parameters from annotation
        this.dsFile = parameters.path();
        this.executeColumn = parameters.executeColumn();
        this.executeValue = parameters.executeValue();
        this.groupColumn = parameters.groupColumn();
        this.testMethodColumn = parameters.testMethodColumn();

        if (!parameters.dsArgs().isEmpty()) {
            this.args = Arrays.asList(parameters.dsArgs().replace(" ", "").split(","));
        }
        if (!parameters.dsUid().isEmpty()) {
            this.uidArgs = Arrays.asList(parameters.dsUid().replace(" ", "").split(","));
        }
        if (!parameters.staticArgs().isEmpty()) {
            this.staticArgs = Arrays.asList(parameters.staticArgs().replace(" ", "").split(","));
        }
    }

    private void initParamsFromSuite(Map<String, String> suiteParams, String specialKeyPrefix) {
        // initialize data source parameters from suite xml file
        if (suiteParams.get(insert(SpecialKeywords.DS_FILE, specialKeyPrefix)) != null) {
            this.dsFile = suiteParams.get(insert(SpecialKeywords.DS_FILE, specialKeyPrefix));
        }
        if (suiteParams.get(SpecialKeywords.DS_EXECUTE_COLUMN) != null) {
            this.executeColumn = suiteParams.get(SpecialKeywords.DS_EXECUTE_COLUMN);
        }
        if (suiteParams.get(SpecialKeywords.DS_EXECUTE_VALUE) != null) {
            this.executeValue = suiteParams.get(SpecialKeywords.DS_EXECUTE_VALUE);
        }

        String dsArgs = suiteParams.get(insert(SpecialKeywords.DS_ARGS, specialKeyPrefix));
        if (dsArgs != null && !dsArgs.isEmpty()) {
            this.args = Arrays.asList(dsArgs.replace(" ", "").split(","));
        }
        String dsUid = suiteParams.get(insert(SpecialKeywords.DS_UID, specialKeyPrefix));
        if (dsUid != null && !dsUid.isEmpty()) {
            this.uidArgs = Arrays.asList(dsUid.replace(" ", "").split(","));
        }
    }

    private String insert(String into, String insertion) {
        StringBuilder newString = new StringBuilder(into);
        newString.insert(1, insertion);
        return newString.toString();
    }

    public Map<String, String> getTestParams() {
        return testParams;
    }

    public List<String> getArgs() {
        return args;
    }

    public List<String> getUidArgs() {
        return uidArgs;
    }

    public List<String> getStaticArgs() {
        return staticArgs;
    }

    public String getDsFile() {
        return dsFile;
    }

    public String getXlsSheet() {
        return xlsSheet;
    }

    public String getGroupColumn() {
        return groupColumn;
    }

    public String getTestMethodColumn() {
        return testMethodColumn;
    }

    public boolean isSpreadsheet() {
        return spreadsheet;
    }

    public String getExecuteColumn() {
        return executeColumn;
    }

    public String getExecuteValue() {
        return executeValue;
    }

    public boolean isArgsToMap() {
        return argsToMap;
    }

    public void setArgsToMap(boolean argsToMap) {
        this.argsToMap = argsToMap;
    }
}
