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
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zebrunner.carina.utils.config.Configuration;
import com.zebrunner.carina.utils.config.StandardConfigurationOption;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestNGMethod;

import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.utils.resources.L10N;

public class ParameterGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Pattern GENERATE_UUID_PATTERN = Pattern.compile(SpecialKeywords.GENERATE_UUID);
    private static final Pattern GENERATE_PATTERN = Pattern.compile(SpecialKeywords.GENERATE);
    private static final Pattern GENERATEAN_PATTERN = Pattern.compile(SpecialKeywords.GENERATEAN);
    private static final Pattern GENERATEN_PATTERN = Pattern.compile(SpecialKeywords.GENERATEN);
    private static final Pattern TESTDATA_PATTERN = Pattern.compile(SpecialKeywords.TESTDATA);
    private static final Pattern ENV_PATTERN = Pattern.compile(SpecialKeywords.ENV);
    private static final Pattern L10N_PATTERN = Pattern.compile(SpecialKeywords.L10N_PATTERN);
    private static String uuid;

    private ParameterGenerator() {
        // do nothing
    }

    public static Object process(String param) {
        try {
            if (param == null || param.equalsIgnoreCase("nil")) {
                return null;
            }

            Matcher matcher = GENERATE_UUID_PATTERN.matcher(param);
            if (matcher.find()) {
                return StringUtils.replace(param, matcher.group(), uuid);
            }
            matcher = GENERATE_PATTERN.matcher(param);
            if (matcher.find()) {
                int start = param.indexOf(':') + 1;
                int end = param.indexOf('}');
                int size = Integer.parseInt(param.substring(start, end));
                return StringUtils.replace(param, matcher.group(), StringGenerator.generateWord(size));
            }

            matcher = GENERATEAN_PATTERN.matcher(param);
            if (matcher.find()) {
                int start = param.indexOf(':') + 1;
                int end = param.indexOf('}');
                int size = Integer.parseInt(param.substring(start, end));
                return StringUtils.replace(param, matcher.group(), StringGenerator.generateWordAN(size));
            }

            matcher = GENERATEN_PATTERN.matcher(param);
            if (matcher.find()) {
                int start = param.indexOf(':') + 1;
                int end = param.indexOf('}');
                int size = Integer.parseInt(param.substring(start, end));
                return StringUtils.replace(param, matcher.group(), StringGenerator.generateNumeric(size));
            }

            matcher = ENV_PATTERN.matcher(param);
            if (matcher.find()) {
                int start = param.indexOf(':') + 1;
                int end = param.indexOf('}');
                String key = param.substring(start, end);
                return StringUtils.replace(param, matcher.group(), Configuration.get(key, StandardConfigurationOption.ENVIRONMENT).orElse(""));
            }

            matcher = TESTDATA_PATTERN.matcher(param);
            if (matcher.find()) {
                int start = param.indexOf(':') + 1;
                int end = param.indexOf('}');
                String key = param.substring(start, end);
                return StringUtils.replace(param, matcher.group(), R.TESTDATA.get(key));
            }
            matcher = L10N_PATTERN.matcher(param);
            String initStrL10N = param;
            while (matcher.find()) {
                int start = param.indexOf(SpecialKeywords.L10N + ":") + 5;
                int end = param.indexOf('}');
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

    public static void processMap(Map<String, String> paramsMap) {
        paramsMap.entrySet()
                .stream()
                .filter(Objects::nonNull).forEach(entry -> {
                    String value = entry.getValue();
                    if (value == null)
                        return;
                    Object param = process(value);
                    if (param == null)
                        return;
                    String newValue = param.toString();
                    if (!value.equals(newValue)) {
                        entry.setValue(newValue);
                    }
                });
    }

    public static String getUUID() {
        return uuid;
    }

    public static void setUUID(String uUID) {
        uuid = uUID;
    }

    /**
     * Generate hash by class name, method name and arg values.
     *
     * @param args Object[] test method arguments
     * @param method ITestNGMethod
     * @return String hash
     */
    public static String hash(Object[] args, ITestNGMethod method) {
        String toHash = "";
        toHash += Arrays.hashCode(args);
        toHash += method.getMethodName();
        toHash += (method.getRealClass());
        return String.valueOf(toHash.hashCode());
    }
}
