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
import java.util.List;
import java.util.Properties;

public class PropertiesProcessorMain {

    private static List<PropertiesProcessor> processors;

    static {
        processors = new ArrayList<PropertiesProcessor>();
        processors.add(new GenerateProcessor());
        processors.add(new CryptoProcessor());
		processors.add(new NotStringValuesProcessor());
    }

    public static Properties processProperties(Properties in) {
        Properties out = new Properties();
        out.putAll(in);
        for (PropertiesProcessor processor : processors) {
            out.putAll(processor.process(in));
        }
        return out;
    }

}
