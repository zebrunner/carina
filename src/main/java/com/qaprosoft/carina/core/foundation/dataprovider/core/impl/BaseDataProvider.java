package com.qaprosoft.carina.core.foundation.dataprovider.core.impl;

import com.qaprosoft.carina.core.foundation.utils.ParameterGenerator;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.DSBean;
import org.testng.ITestContext;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Patotsky on 19.12.2014.
 */

public abstract class BaseDataProvider {

    protected Map<String, String> testNameArgsMap = Collections.synchronizedMap(new HashMap<String, String>());

    protected Map<String, String> jiraArgsMap = Collections.synchronizedMap(new HashMap<String, String>());
	protected Map<String, String> spiraArgsMap = Collections.synchronizedMap(new HashMap<String, String>());
    protected Map<String, String> testRailsArgsMap = Collections.synchronizedMap(new HashMap<String, String>());

	protected List<String> argsList;
    protected List<String> staticArgsList;


    public abstract Object[][] getDataProvider(Annotation annotation, ITestContext context);

    protected static Object getStaticParam(String name, ITestContext context, DSBean dsBean) {
        return ParameterGenerator.process(dsBean
                        .getTestParams().get(name),
                context.getAttribute(SpecialKeywords.UUID)
                        .toString());
    }

    public Map<String, String> getTestNameArgsMap() {

        return testNameArgsMap;
    }

    public Map<String, String> getJiraArgsMap() {
        return jiraArgsMap;
    }

    public Map<String, String> getSpiraArgsMap() {
 		return spiraArgsMap;
 	}

    public Map<String, String> getTestRailsArgsMap() {
		return testRailsArgsMap;
	}


}
