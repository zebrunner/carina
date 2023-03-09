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
package com.qaprosoft.carina.core.foundation.dataprovider.parser;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.testng.ITestContext;

import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.dataprovider.annotations.CsvDataSourceParameters;
import com.qaprosoft.carina.core.foundation.dataprovider.annotations.XlsDataSourceParameters;
import com.zebrunner.carina.utils.exception.InvalidArgsException;

public class DSBean {
    Map<String, String> testParams;
    private List<String> args = new ArrayList<>();
    private List<String> uidArgs = new ArrayList<>();
    private List<String> staticArgs = new ArrayList<>();

    private String dsFile;
    private String xlsSheet;

    private String executeColumn;
    private String executeValue;

    private boolean spreadsheet;

    public DSBean(ITestContext context) {
        this(context.getCurrentXmlTest().getAllParameters());
    }

    public DSBean(Map<String, String> testParams) {
        this.testParams = testParams;
        this.xlsSheet = testParams.get(SpecialKeywords.EXCEL_DS_SHEET);
        initParamsFromSuite(testParams, "excel");
    }

    public DSBean(XlsDataSourceParameters xlsDataSourceParameters, Map<String, String> suiteParams) {
        // params init order: 1) from test annotation 2) from suite
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
    }

    public DSBean(CsvDataSourceParameters csvDataSourceParameters, Map<String, String> suiteParams) {
        // initialize default Xls data source parameters from suite xml file
        if (csvDataSourceParameters != null) {
            this.initParamsFromAnnotation(csvDataSourceParameters);
        }

        if (!suiteParams.isEmpty()) {
            initParamsFromSuite(suiteParams, "");
        }

        this.testParams = suiteParams;
        this.xlsSheet = null;
    }

    private void initParamsFromAnnotation(XlsDataSourceParameters parameters) {
        if (parameters != null) {
            if (!parameters.path().isEmpty()) {
                this.dsFile = parameters.path();
            }
            if (!parameters.executeColumn().isEmpty()) {
                this.executeColumn = parameters.executeColumn();
            }
            if (!parameters.executeValue().isEmpty()) {
                this.executeValue = parameters.executeValue();
            }
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
    }

    private void initParamsFromAnnotation(CsvDataSourceParameters parameters) {
        if (parameters != null) {
            if (!parameters.path().isEmpty()) {
                this.dsFile = parameters.path();
            }
            if (!parameters.executeColumn().isEmpty()) {
                this.executeColumn = parameters.executeColumn();
            }
            if (!parameters.executeValue().isEmpty()) {
                this.executeValue = parameters.executeValue();
            }
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
    }

    private void initParamsFromSuite(Map<String, String> suiteParams, String specialKeyPrefix) {
        if (suiteParams.get(insert(SpecialKeywords.DS_FILE, specialKeyPrefix)) != null) {
            this.dsFile = suiteParams.get(insert(SpecialKeywords.DS_FILE, specialKeyPrefix));
        }
        if (suiteParams.get(SpecialKeywords.DS_EXECUTE_COLUMN) != null) {
            this.executeColumn = suiteParams.get(SpecialKeywords.DS_EXECUTE_COLUMN);
        }
        if (suiteParams.get(SpecialKeywords.DS_EXECUTE_VALUE) != null) {
            this.executeValue = suiteParams.get(SpecialKeywords.DS_EXECUTE_VALUE);
        }
        if (suiteParams.get(insert(SpecialKeywords.DS_ARGS, specialKeyPrefix)) != null) {
            this.args = Arrays.asList(suiteParams.get(insert(SpecialKeywords.DS_ARGS, specialKeyPrefix))
                    .replace(" ", "").split(","));
        }
        if (suiteParams.get(insert(SpecialKeywords.DS_UID, specialKeyPrefix)) != null) {
            this.uidArgs = Arrays.asList(suiteParams.get(insert(SpecialKeywords.DS_UID, specialKeyPrefix))
                    .replace(" ", "").split(","));
        }
//            TODO: Add staticArgs to SpecialKeywords
//            if (testParams.get(SpecialKeywords.DS_STATIC_ARGS) != null) {
//                dsStaticArgs = testParams.get(SpecialKeywords.DS_STATIC_ARGS);
//            }
    }

    private String insert(String into, String insertion) {
        StringBuilder newString = new StringBuilder(into);
        newString.insert(1, insertion);
        return newString.toString();
    }

    public String getDsFile() {
        return dsFile;
    }

    public void setDsFile(String xlsFile) {
        this.dsFile = xlsFile;
    }

    public String getXlsSheet() {
        return xlsSheet;
    }

    public void setXlsSheet(String xlsSheet) {
        this.xlsSheet = xlsSheet;
    }

    public List<String> getUidArgs() {
        return uidArgs;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public List<String> getStaticArgs() {
        return staticArgs;
    }

    public void setStaticArgs(List<String> staticArgs) {
        this.staticArgs = staticArgs;
    }

    public Map<String, String> getTestParams() {
        return testParams;
    }

    public void setTestParams(Map<String, String> testParams) {
        this.testParams = testParams;
    }

    public String argsToString(List<String> args, Map<String, String> params) {
        if (args.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            if (SpecialKeywords.TUID.equals(arg))
                continue;
            sb.append(String.format("%s=%s; ", arg, params.get(arg)));
        }
        return StringUtils.removeEnd(sb.toString(), "; ");
    }

    @Deprecated(forRemoval = true, since = "8.0.6")
    public String setDataSorceUUID(String testName, Map<String, String> params) {

        if (!params.isEmpty()) {
            if (params.containsKey(SpecialKeywords.TUID)) {
                testName = params.get(SpecialKeywords.TUID) + " - " + testName;
            }
        }
        if (!uidArgs.isEmpty()) {
            if (!argsToString(uidArgs, params).isEmpty()) {
                testName = testName + " [" + argsToString(uidArgs, params) + "]";
            }
        }

        return testName;
    }

    /**
     * Get TUID
     * 
     * @param params
     * @return tuid if exists, empty string otherwise
     */
    public String getDataSourceTUID(Map<String, String> params) {
        String tuid = "";
        if (!params.isEmpty() && params.containsKey(SpecialKeywords.TUID)) {
            tuid = params.get(SpecialKeywords.TUID);
        }
        return tuid;
    }

    public String getDataSourceTUID(Object[] params, Map<String, Integer> mapper) {
        String tuid = "";
        // looks through the parameters for TUID or get column value by uidArgs parameter
        Integer indexTUID = mapper.get(SpecialKeywords.TUID);
        if (indexTUID != null) {
            tuid = params[indexTUID] + "";
        }
        return tuid;
    }

    public String getDataSourceUUID(Map<String, String> params) {
        String uid = "";
        if (!uidArgs.isEmpty()) {
            if (!argsToString(uidArgs, params).isEmpty()) {
                uid = argsToString(uidArgs, params);
            }
        }
        return uid;
    }

    public String getDataSourceUUID(Object[] params, Map<String, Integer> mapper) {
        String uid = "";
        if (!uidArgs.isEmpty()) {
            String uidString = argsToString(uidArgs, params, mapper);
            if (!uidString.isEmpty()) {
                uid = uidString;
            }
        }
        return uid;

    }

    @Deprecated(forRemoval = true, since = "8.0.6")
    public String setDataSorceUUID(String testName, Object[] params, Map<String, Integer> mapper) {
        // looks through the parameters for TUID or get column value by uidArgs parameter
        Integer indexTUID = mapper.get(SpecialKeywords.TUID);
        if (indexTUID != null) {
            testName = params[indexTUID] + " - " + testName;
        }

        if (!uidArgs.isEmpty()) {
            String uidString = argsToString(uidArgs, params, mapper);
            if (!uidString.isEmpty()) {
                testName = testName + " [" + uidString + "]";
            }
        }
        return testName;
    }

    public String argsToString(List<String> args, Object[] params, Map<String, Integer> mapper) {
        if (args.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            Integer index = mapper.get(arg);
            if (index != null) {
                sb.append(String.format("%s=%s; ", arg, params[index].toString()));
            }
        }
        return StringUtils.removeEnd(sb.toString(), "; ");
    }

    public String getExecuteColumn() {
        return executeColumn;
    }

    public String getExecuteValue() {
        return executeValue;
    }

    public boolean isSpreadsheet() {
        return spreadsheet;
    }
}
