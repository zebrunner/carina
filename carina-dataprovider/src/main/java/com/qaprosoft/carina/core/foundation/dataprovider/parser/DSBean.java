/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.testng.ITestContext;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.dataprovider.annotations.CsvDataSourceParameters;
import com.qaprosoft.carina.core.foundation.dataprovider.annotations.XlsDataSourceParameters;

public class DSBean {
    Map<String, String> testParams;
    private List<String> args;
    private List<String> uidArgs;
    private List<String> staticArgs;

    private String dsFile;
    private String xlsSheet;

    private String executeColumn;
    private String executeValue;

    @Deprecated
    public DSBean(ITestContext context) {
        this(context.getCurrentXmlTest().getAllParameters());
    }

    @Deprecated
    public DSBean(Map<String, String> testParams) {
        this.testParams = testParams;
        this.dsFile = testParams.get(SpecialKeywords.EXCEL_DS_FILE);
        this.xlsSheet = testParams.get(SpecialKeywords.EXCEL_DS_SHEET);
        this.args = new ArrayList<String>();
        this.uidArgs = new ArrayList<String>();

        if (testParams.get(SpecialKeywords.EXCEL_DS_ARGS) != null) {
            args = Arrays.asList(testParams.get(SpecialKeywords.EXCEL_DS_ARGS).replace(",", ";").replace(" ", "").split(";"));
        }
        if (testParams.get(SpecialKeywords.EXCEL_DS_UID) != null) {
            uidArgs = Arrays.asList(testParams.get(SpecialKeywords.EXCEL_DS_UID).replace(",", ";").replace(" ", "").split(";"));
        }
    }

    @Deprecated
    public DSBean(String xlsFile, String xlsSheet, String dsArgs, String dsUids) {
        this.dsFile = xlsFile;
        this.xlsSheet = xlsSheet;
        this.args = new ArrayList<String>();
        this.uidArgs = new ArrayList<String>();

        if (dsArgs != null && !dsArgs.isEmpty()) {
            args = Arrays.asList(dsArgs.replace(",", ";").replace(" ", "").split(";"));
        }
        if (dsUids != null && !dsUids.isEmpty()) {
            uidArgs = Arrays.asList(dsUids.replace(",", ";").replace(" ", "").split(";"));
        }

    }

    // TODO: Analyze DSBean for XLS and CSV DataProviders and remove code duplicates
    public DSBean(XlsDataSourceParameters parameters, Map<String, String> testParams) {
        // initialize default Xls data source parameters from suite xml file
        String xlsFile = testParams.get(SpecialKeywords.EXCEL_DS_FILE);
        String xlsSheet = testParams.get(SpecialKeywords.EXCEL_DS_SHEET);
        String dsArgs = testParams.get(SpecialKeywords.EXCEL_DS_ARGS);
        String dsUid = testParams.get(SpecialKeywords.EXCEL_DS_UID);
        String dsStaticArgs = "";

        if (parameters != null) {
            // reinitialize parameters from annotation if any
            if (!parameters.path().isEmpty())
                xlsFile = parameters.path();

            if (!parameters.sheet().isEmpty())
                xlsSheet = parameters.sheet();

            if (!parameters.dsArgs().isEmpty())
                dsArgs = parameters.dsArgs();

            if (!parameters.dsUid().isEmpty())
                dsUid = parameters.dsUid();

            if (!parameters.staticArgs().isEmpty())
                dsStaticArgs = parameters.staticArgs();
        }

        this.testParams = testParams;
        this.dsFile = xlsFile;
        this.xlsSheet = xlsSheet;
        this.args = new ArrayList<String>();
        this.uidArgs = new ArrayList<String>();
        this.staticArgs = new ArrayList<String>();

        if (dsArgs != null && !dsArgs.isEmpty()) {
            args = Arrays.asList(dsArgs.replace(",", ";").replace(" ", "").split(";"));
        }
        if (dsUid != null && !dsUid.isEmpty()) {
            uidArgs = Arrays.asList(dsUid.replace(",", ";").replace(" ", "").split(";"));
        }

        if (dsStaticArgs != null && !dsStaticArgs.isEmpty()) {
            staticArgs = Arrays.asList(dsStaticArgs.replace(",", ";").replace(" ", "").split(";"));
        }

        this.executeColumn = "Execute";
        this.executeValue = "y";
        if (testParams.get(SpecialKeywords.DS_EXECUTE_COLUMN) != null)
            this.executeColumn = testParams.get(SpecialKeywords.DS_EXECUTE_COLUMN);
        if (testParams.get(SpecialKeywords.DS_EXECUTE_VALUE) != null)
            this.executeValue = testParams.get(SpecialKeywords.DS_EXECUTE_VALUE);

        if (!parameters.executeColumn().isEmpty())
            this.executeColumn = parameters.executeColumn();
        if (!parameters.executeValue().isEmpty())
            this.executeValue = parameters.executeValue();
    }

    // TODO: Analyze DSBean for XLS and CSV DataProviders and remove code duplicates
    public DSBean(CsvDataSourceParameters parameters, Map<String, String> testParams) {
        // initialize default Xls data source parameters from suite xml file
        String dsFile = testParams.get(SpecialKeywords.DS_FILE);
        String dsArgs = testParams.get(SpecialKeywords.DS_ARGS);
        String dsUid = testParams.get(SpecialKeywords.DS_UID);
        String dsStaticArgs = "";

        if (parameters != null) {
            // reinitialize parameters from annotation if any
            if (!parameters.path().isEmpty())
                dsFile = parameters.path();

            if (!parameters.dsArgs().isEmpty())
                dsArgs = parameters.dsArgs();

            if (!parameters.dsUid().isEmpty())
                dsUid = parameters.dsUid();

            if (!parameters.staticArgs().isEmpty())
                dsStaticArgs = parameters.staticArgs();
        }

        this.testParams = testParams;
        this.dsFile = dsFile;
        this.xlsSheet = null;
        this.args = new ArrayList<String>();
        this.uidArgs = new ArrayList<String>();
        this.staticArgs = new ArrayList<String>();

        if (dsArgs != null && !dsArgs.isEmpty()) {
            args = Arrays.asList(dsArgs.replace(",", ";").replace(" ", "").split(";"));
        }
        if (dsUid != null && !dsUid.isEmpty()) {
            uidArgs = Arrays.asList(dsUid.replace(",", ";").replace(" ", "").split(";"));
        }

        if (dsStaticArgs != null && !dsStaticArgs.isEmpty()) {
            staticArgs = Arrays.asList(dsStaticArgs.replace(",", ";").replace(" ", "").split(";"));
        }

        this.executeColumn = "Execute";
        this.executeValue = "y";
        if (testParams.get(SpecialKeywords.DS_EXECUTE_COLUMN) != null)
            this.executeColumn = testParams.get(SpecialKeywords.DS_EXECUTE_COLUMN);
        if (testParams.get(SpecialKeywords.DS_EXECUTE_VALUE) != null)
            this.executeValue = testParams.get(SpecialKeywords.DS_EXECUTE_VALUE);

        if (!parameters.executeColumn().isEmpty())
            this.executeColumn = parameters.executeColumn();
        if (!parameters.executeValue().isEmpty())
            this.executeValue = parameters.executeValue();
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
}
