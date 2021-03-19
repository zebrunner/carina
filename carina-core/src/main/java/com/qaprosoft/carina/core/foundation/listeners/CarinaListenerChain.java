package com.qaprosoft.carina.core.foundation.listeners;

import com.google.common.collect.Lists;
import com.nordstrom.automation.testng.ListenerChain;
import org.apache.log4j.Logger;
import org.testng.IConfigurationListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;

public class CarinaListenerChain extends ListenerChain {

    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

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
