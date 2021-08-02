/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation.report.testrail;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ITestCases {
    static final Logger TEST_CASES_LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    ThreadLocal<List<String>> casesIds = ThreadLocal.withInitial(ArrayList::new);

    default List<String> getCases() {
        return casesIds.get();
    }

    default void setCases(String... cases) {
        for (String _case : cases) {
            casesIds.get().add(_case);
        }
    }

    default void clearCases() {
        casesIds.set(new ArrayList<String>());
    }

    default boolean isValidPlatform(String platform) {
        boolean isValid = platform.equalsIgnoreCase(Configuration.getPlatform()) || platform.isEmpty();
        if (!isValid){
            TEST_CASES_LOGGER.error("Invalid platform passed: " + platform + ", expected: " + Configuration.getPlatform());
        }
        return isValid;
    }
    
    default boolean isValidLocale(String locale) {
        boolean isValid = locale.equalsIgnoreCase(Configuration.get(Parameter.LOCALE)) || locale.isEmpty();
        if (!isValid){
            TEST_CASES_LOGGER.error("Invalid locale passed: " + locale + ", expected: " + Configuration.get(Parameter.LOCALE));
        }
        return isValid;
    }
}
