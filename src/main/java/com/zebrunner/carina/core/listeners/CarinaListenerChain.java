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
package com.zebrunner.carina.core.listeners;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IConfigurationListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import com.google.common.collect.Lists;
import com.nordstrom.automation.testng.ListenerChain;

public class CarinaListenerChain extends ListenerChain {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // The IConfigurationListener interface has two methods with the same meaning:
    // 1. void beforeConfiguration(ITestResult tr)
    // 2. void beforeConfiguration(ITestResult tr, ITestNGMethod tm)
    //
    // The only difference is that the version with two arguments also passes the actual test method
    // for which a configuration method is going to be invoked, but the first version don't.
    //
    // The Zebrunner agent expects the second version of the method to be invoked.
    // This is basically necessary to correlate a test method with corresponding to it @BeforeMethod and @AfterMethod methods.
    //
    // The problem is that Nordstrom's ListenerChain class doesn't decorate this method.
    // As a result, the Zebrunner agent misses invocations of @BeforeMethod and @AfterMethod methods.
    @Override
    public void beforeConfiguration(ITestResult tr, ITestNGMethod tm) {
        try {
            Field configListenersField = this.getClass().getSuperclass().getDeclaredField("configListeners");
            configListenersField.setAccessible(true);
            List<IConfigurationListener> listeners = (List<IConfigurationListener>) configListenersField.get(this);

            invokeListeners(tr, tm, listeners);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.error("Could not invoke configuration listeners.", e);
        }
    }

    private void invokeListeners(ITestResult tr, ITestNGMethod tm, List<IConfigurationListener> listeners) {
        synchronized (listeners) {
            for (IConfigurationListener configListener : Lists.reverse(listeners)) {
                configListener.beforeConfiguration(tr, tm);
            }
        }
    }

}
