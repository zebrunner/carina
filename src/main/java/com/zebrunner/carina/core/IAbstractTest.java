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
package com.zebrunner.carina.core;

import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.nordstrom.automation.testng.LinkedListeners;
import com.zebrunner.agent.core.registrar.CurrentTest;
import com.zebrunner.agent.testng.listener.TestRunListener;
import com.zebrunner.carina.core.listeners.CarinaListener;
import com.zebrunner.carina.core.listeners.FilterTestsListener;
import com.zebrunner.carina.core.report.testrail.ITestCases;
import com.zebrunner.carina.utils.common.CommonUtils;
import com.zebrunner.carina.utils.config.Configuration;
import com.zebrunner.carina.utils.factory.ICustomTypePageFactory;
import com.zebrunner.carina.webdriver.config.WebDriverConfiguration;

/*
 * IAbstractTest - base test for UI and API tests.
 */

// https://github.com/zebrunner/carina/issues/951
// reused com.nordstrom.tools.testng-foundation to register ordered listeners

// on start order is FilterTestsListener, TestRunListener and CarinaListener
// on finish reverse order, i.e. CarinaListener, TestRunListener and FilterTestsListener
@LinkedListeners({ CarinaListener.class, TestRunListener.class, FilterTestsListener.class })
public interface IAbstractTest extends ICustomTypePageFactory, ITestCases {

    long EXPLICIT_TIMEOUT = Configuration.getRequired(WebDriverConfiguration.Parameter.EXPLICIT_TIMEOUT, Long.class);

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

    /**
     * Carina AfterMethod<br>
     * <b>For internal usage only - should not be overridden!</b>
     */
    @AfterMethod(alwaysRun = true)
    default void onCarinaAfterMethod() {
        // do nothing
    }

    /**
     * Pause for specified timeout.
     *
     * @param timeout in seconds.
     */
    default void pause(long timeout) {
        CommonUtils.pause(timeout);
    }

    default void pause(Double timeout) {
        CommonUtils.pause(timeout);
    }

    default void skipExecution(String message) {
        CurrentTest.revertRegistration();
        throw new SkipException(message);
    }
}
