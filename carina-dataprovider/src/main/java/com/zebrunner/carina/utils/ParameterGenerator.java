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
package com.zebrunner.carina.utils;

import java.lang.invoke.MethodHandles;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.XLSParser;
import com.zebrunner.carina.utils.exception.InvalidArgsException;
import com.zebrunner.carina.utils.resources.L10N;

public class ParameterGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static Pattern GENERATE_UUID_PATTERN = Pattern.compile(SpecialKeywords.GENERATE_UUID);
    private static Pattern GENERATE_PATTERN = Pattern.compile(SpecialKeywords.GENERATE);
    private static Pattern GENERATEAN_PATTERN = Pattern.compile(SpecialKeywords.GENERATEAN);
    private static Pattern GENERATEN_PATTERN = Pattern.compile(SpecialKeywords.GENERATEN);
    private static Pattern TESTDATA_PATTERN = Pattern.compile(SpecialKeywords.TESTDATA);
    private static Pattern ENV_PATTERN = Pattern.compile(SpecialKeywords.ENV);
    private static Pattern L10N_PATTERN = Pattern.compile(SpecialKeywords.L10N_PATTERN);
    private static Pattern EXCEL_PATTERN = Pattern.compile(SpecialKeywords.EXCEL);

    private static Matcher matcher;

    private static String UUID;

    public static Object process(String param) {
        try {
            if (param == null || param.toLowerCase().equals("nil")) {
                return null;
            }

            matcher = GENERATE_UUID_PATTERN.matcher(param);
            if (matcher.find()) {
                return StringUtils.replace(param, matcher.group(), UUID);
            }
            matcher = GENERATE_PATTERN.matcher(param);
            if (matcher.find()) {
                int start = param.indexOf(":") + 1;
                int end = param.indexOf("}");
                int size = Integer.valueOf(param.substring(start, end));
                return StringUtils.replace(param, matcher.group(), StringGenerator.generateWord(size));
            }

            matcher = GENERATEAN_PATTERN.matcher(param);
            if (matcher.find()) {
                int start = param.indexOf(":") + 1;
                int end = param.indexOf("}");
                int size = Integer.valueOf(param.substring(start, end));
                return StringUtils.replace(param, matcher.group(), StringGenerator.generateWordAN(size));
            }

            matcher = GENERATEN_PATTERN.matcher(param);
            if (matcher.find()) {
                int start = param.indexOf(":") + 1;
                int end = param.indexOf("}");
                int size = Integer.valueOf(param.substring(start, end));
                return StringUtils.replace(param, matcher.group(), StringGenerator.generateNumeric(size));
            }

            matcher = ENV_PATTERN.matcher(param);
            if (matcher.find()) {
                int start = param.indexOf(":") + 1;
                int end = param.indexOf("}");
                String key = param.substring(start, end);
                return StringUtils.replace(param, matcher.group(), Configuration.getEnvArg(key));
            }

            matcher = TESTDATA_PATTERN.matcher(param);
            if (matcher.find()) {
                int start = param.indexOf(":") + 1;
                int end = param.indexOf("}");
                String key = param.substring(start, end);
                return StringUtils.replace(param, matcher.group(), R.TESTDATA.get(key));
            }

            matcher = EXCEL_PATTERN.matcher(param);
            if (matcher.find()) {
                int start = param.indexOf(":") + 1;
                int end = param.indexOf("}");
                String key = param.substring(start, end);
                return StringUtils.replace(param, matcher.group(), getValueFromXLS(key));
            }

            matcher = L10N_PATTERN.matcher(param);
            String initStrL10N = param;
            while (matcher.find()) {
                int start = param.indexOf(SpecialKeywords.L10N + ":") + 5;
                int end = param.indexOf("}");
                String key = param.substring(start, end);
                param = StringUtils.replace(param, matcher.group(), L10N.getText(key));
            }
            // in case if L10N pattern was applied
            if (!initStrL10N.equalsIgnoreCase(param)) {
                return param;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return param;
    }

    private static String getValueFromXLS(String xlsSheetKey) {
        if (StringUtils.isEmpty(xlsSheetKey)) {
            throw new InvalidArgsException("Invalid excel key, should be 'xls_file#sheet#key'.");
        }

        String xls = xlsSheetKey.split("#")[0];
        String sheet = xlsSheetKey.split("#")[1];
        String key = xlsSheetKey.split("#")[2];

        return XLSParser.parseValue(xls, sheet, key);
    }

    public static String getUUID() {
        return UUID;
    }

    public static void setUUID(String uUID) {
        UUID = uUID;
    }
}
