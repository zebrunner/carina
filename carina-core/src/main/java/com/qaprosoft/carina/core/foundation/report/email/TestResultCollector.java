package com.qaprosoft.carina.core.foundation.report.email;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import com.zebrunner.carina.utils.report.TestResultItem;import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
public class TestResultCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static LinkedHashMap<String, TestResultItem> emailResultsMap = new LinkedHashMap<String, TestResultItem>();
    private static Map<String, TestResultItem> testResultsMap = Collections.synchronizedMap(new HashMap<String, TestResultItem>());
    private static List<String> createdItems = new ArrayList<String>();

    public static synchronized void push(TestResultItem emailItem) {
        emailResultsMap.put(emailItem.hash(), emailItem);
        testResultsMap.put(emailItem.getTest(), emailItem);
    }

    public static synchronized void push(String itemToDelete) {
        createdItems.add(itemToDelete);
    }

    public static synchronized TestResultItem pull(ITestResult result) {
        TestResultItem testResultItem = null;
        try {
            Class<?> artifactClass = ClassUtils.getClass("com.zebrunner.agent.testng.core.testname.TestNameResolverRegistry");
            Object object = MethodUtils.invokeStaticMethod(artifactClass, "get");
            Object name = MethodUtils.invokeMethod(object, "resolve", result);
            testResultItem = testResultsMap.get(name);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            LOGGER.debug("Cannot get info from TestNameResolverRegistry class because Zebrunner agent does not loaded in classloader");
        }
        return testResultItem;
    }

    public static List<TestResultItem> getTestResults() {
        return new ArrayList<TestResultItem>(emailResultsMap.values());
    }

    public static List<String> getCreatedItems() {
        return createdItems;
    }
}
