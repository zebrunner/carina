/*******************************************************************************
 * Copyright 2020-2023 Zebrunner Inc (https://www.zebrunner.com).
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
package com.zebrunner.carina.core.filter.v2;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestNGMethod;

import com.zebrunner.carina.utils.config.Configuration;

public class TestRunFilterListener implements ISuiteListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final LazyInitializer<List<ITestFilter>> FILTERS = new LazyInitializer<>() {
        @Override
        protected List<ITestFilter> initialize() throws ConcurrentException {
            List<ITestFilter> filters = List.of(new CountryFilter(), new MethodsFilter());
            List<String> usedFilters = Configuration.get("test_run_filters")
                    .map(StringUtils::lowerCase)
                    .map(p -> StringUtils.split(p, ","))
                    .map(Arrays::asList)
                    .orElse(List.of());

            if (usedFilters.isEmpty()) {
                return List.of();
            }
            return filters.stream()
                    .filter(filter -> usedFilters.contains(filter.key()))
                    .collect(Collectors.toList());
        }
    };

    @Override
    public void onStart(ISuite suite) {
        try {
            List<ITestFilter> filters = FILTERS.get();
            if (filters.isEmpty()) {
                return;
            }

            for (ITestNGMethod testMethod : suite.getAllMethods().stream().filter(ITestNGMethod::isTest)
                    .collect(Collectors.toList())) {
                boolean isPerform = filters.stream()
                        .allMatch(filter -> filter.isPerform(testMethod));

                if (!isPerform) {
                    LOGGER.info("Disable test: [{}]", testMethod.getMethodName());
                    testMethod.setInvocationCount(0);
                }
            }
        } catch (ConcurrentException e) {
            ExceptionUtils.rethrow(e);
        }
    }
}
