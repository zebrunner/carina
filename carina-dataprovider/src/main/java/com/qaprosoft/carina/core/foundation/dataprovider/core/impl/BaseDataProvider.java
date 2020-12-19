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
package com.qaprosoft.carina.core.foundation.dataprovider.core.impl;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import com.qaprosoft.carina.core.foundation.dataprovider.parser.DSBean;
import com.qaprosoft.carina.core.foundation.utils.ParameterGenerator;

/**
 * Created by Patotsky on 19.12.2014.
 */

public abstract class BaseDataProvider {

    protected Map<String, String> testNameArgsMap = Collections.synchronizedMap(new HashMap<>());

    protected Map<String, String> testMethodOwnerArgsMap = Collections.synchronizedMap(new HashMap<>());

    protected Map<String, String> jiraArgsMap = Collections.synchronizedMap(new HashMap<>());
    protected Map<String, String> testRailsArgsMap = Collections.synchronizedMap(new HashMap<>());
    protected Map<String, String> bugArgsMap = Collections.synchronizedMap(new HashMap<>());

    protected List<String> argsList;
    protected List<String> staticArgsList;

    protected List<String> doNotRunTestNames;

    public abstract Object[][] getDataProvider(Annotation annotation, ITestContext context, ITestNGMethod testMethod);

    protected static Object getStaticParam(String name, ITestContext context, DSBean dsBean) {
        return ParameterGenerator.process(dsBean
                .getTestParams().get(name));
    }

    public Map<String, String> getTestNameArgsMap() {
        return testNameArgsMap;
    }

    public Map<String, String> getTestMethodOwnerArgsMap() {
        return testMethodOwnerArgsMap;
    }

    public Map<String, String> getJiraArgsMap() {
        return jiraArgsMap;
    }

    public Map<String, String> getTestRailsArgsMap() {
        return testRailsArgsMap;
    }

    public Map<String, String> getBugArgsMap() {
        return bugArgsMap;
    }

    public List<String> getDoNotRunRowsIDs() {
        return doNotRunTestNames;
    }

}
