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
package com.qaprosoft.apitools.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;

public class PropertiesProcessorMain {

    private static final List<PropertiesProcessor> processors;

    static {
        processors = new ArrayList<>();
        processors.add(new GenerateProcessor());
        processors.add(new CryptoProcessor());
		processors.add(new NotStringValuesProcessor());
    }

    public static Properties processProperties(Properties in, List<Class<? extends PropertiesProcessor>> ignoredPropertiesProcessorClasses) {
        Properties out = new Properties();
        out.putAll(in);
        processors.stream()
                .filter(isProcessorToExecute(ignoredPropertiesProcessorClasses))
                .forEach(processor -> out.putAll(processor.process(in)));
        return out;
    }

    private static Predicate<PropertiesProcessor> isProcessorToExecute(List<Class<? extends PropertiesProcessor>> ignoredPropertiesProcessorClasses) {
        return pr -> ignoredPropertiesProcessorClasses == null || ignoredPropertiesProcessorClasses.stream()
                .noneMatch(pc -> pr.getClass().isAssignableFrom(pc));
    }
}
