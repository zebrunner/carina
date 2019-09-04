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
package com.qaprosoft.carina.core.foundation;

import java.lang.annotation.Annotation;

import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.dataprovider.core.DataProviderFactory;
import com.qaprosoft.carina.core.foundation.listeners.CarinaListener;
import com.qaprosoft.carina.core.foundation.report.testrail.ITestCases;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.common.CommonUtils;
import com.qaprosoft.carina.core.foundation.utils.factory.ICustomTypePageFactory;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;

/*
 * AbstractTest - base test for UI and API tests.
 * 
 * @author Alex Khursevich
 */
@Listeners({ CarinaListener.class })
public abstract class AbstractTest implements ICustomTypePageFactory, ITestCases {

    protected static final long EXPLICIT_TIMEOUT = Configuration.getLong(Parameter.EXPLICIT_TIMEOUT);
    
    
	@BeforeSuite(alwaysRun = true)
	private void onCarinaBeforeSuite() {
		// do nothing
	}

	@BeforeClass(alwaysRun = true)
	private void onCarinaBeforeClass() {
		// do nothing
	}

	@BeforeMethod(alwaysRun = true)
	private void onCarinaBeforeMethod() {
		// do nothing
	}

    @DataProvider(name = "DataProvider", parallel = true)
    public Object[][] createData(final ITestNGMethod testMethod, ITestContext context) {
        Annotation[] annotations = testMethod.getConstructorOrMethod().getMethod().getDeclaredAnnotations();
        Object[][] objects = DataProviderFactory.getNeedRerunDataProvider(annotations, context, testMethod);
        return objects;
    }

    @DataProvider(name = "SingleDataProvider")
    public Object[][] createDataSingleThread(final ITestNGMethod testMethod,
            ITestContext context) {
        Annotation[] annotations = testMethod.getConstructorOrMethod().getMethod().getDeclaredAnnotations();
        Object[][] objects = DataProviderFactory.getNeedRerunDataProvider(annotations, context, testMethod);
        return objects;
    }

    protected void setBug(String id) {
        String test = TestNamingUtil.getTestNameByThread();
        TestNamingUtil.associateBug(test, id);
    }

    
    /**
     * Pause for specified timeout.
     *
     * @param timeout in seconds.
     */

    public void pause(long timeout) {
        CommonUtils.pause(timeout);
    }

    public void pause(Double timeout) {
        CommonUtils.pause(timeout);
    }
    
    protected void skipExecution(String message) {
        throw new SkipException(SpecialKeywords.SKIP_EXECUTION + ": " + message);
    }

}