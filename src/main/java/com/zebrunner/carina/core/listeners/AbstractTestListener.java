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
import java.util.Collection;
import java.util.Map;

import com.zebrunner.carina.core.config.ReportConfiguration;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.internal.annotations.DisabledRetryAnalyzer;

import com.zebrunner.agent.testng.core.retry.RetryAnalyzerInterceptor;
import com.zebrunner.agent.testng.core.testname.TestNameResolverRegistry;
import com.zebrunner.agent.testng.listener.RetryService;
import com.zebrunner.carina.core.report.email.EmailReportItemCollector;
import com.zebrunner.carina.core.testng.TestNamingService;
import com.zebrunner.carina.utils.DateUtils;
import com.zebrunner.carina.utils.ParameterGenerator;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.StringGenerator;
import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.utils.messager.Messager;
import com.zebrunner.carina.utils.report.ReportContext;
import com.zebrunner.carina.utils.report.TestResultItem;
import com.zebrunner.carina.utils.report.TestResultType;
import com.zebrunner.carina.utils.retry.RetryAnalyzer;
import com.zebrunner.carina.webdriver.IDriverPool;

public class AbstractTestListener extends TestListenerAdapter implements IDriverPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private void startItem(ITestResult result, Messager messager) {
        String test = TestNameResolverRegistry.get().resolve(result);
        messager.info(test, DateUtils.now());
    }

    private void passItem(ITestResult result, Messager messager) {
        String test = TestNameResolverRegistry.get().resolve(result);

        messager.info(test, DateUtils.now());

        EmailReportItemCollector
                .push(createTestResult(result, TestResultType.PASS, null, result.getMethod().getDescription()));
        result.getTestContext().removeAttribute(SpecialKeywords.TEST_FAILURE_MESSAGE);

    }

    private String failItem(ITestResult result, Messager messager) {
        String test = TestNameResolverRegistry.get().resolve(result);

        String errorMessage = getFailureReason(result);
        
        TestResultType failType = TestResultType.FAIL;
        if (result.getStatus() == 3) {
            failType = TestResultType.SKIP;
        }

        // TODO: remove hard-coded text
        if (!errorMessage.contains("All tests were skipped! Analyze logs to determine possible configuration issues.")) {
            messager.error(test, DateUtils.now(), errorMessage);
            EmailReportItemCollector.push(createTestResult(result, failType, errorMessage, result
                    .getMethod().getDescription()));
        }

        result.getTestContext().removeAttribute(SpecialKeywords.TEST_FAILURE_MESSAGE);
        return errorMessage;
    }

    private void afterTest(ITestResult result) {
        ReportConfiguration.generateTestReport();
        ReportContext.emptyTestDirData();
    }

    @Override
    public void beforeConfiguration(ITestResult result) {
        LOGGER.debug("AbstractTestListener->beforeConfiguration");
        super.beforeConfiguration(result);
    }

    @Override
    public void onConfigurationSuccess(ITestResult result) {
        LOGGER.debug("AbstractTestListener->onConfigurationSuccess");
        super.onConfigurationSuccess(result);
    }

    @Override
    public void onConfigurationSkip(ITestResult result) {
        LOGGER.debug("AbstractTestListener->onConfigurationSkip");
        super.onConfigurationSkip(result);
    }

    @Override
    public void onConfigurationFailure(ITestResult result) {
        LOGGER.debug("AbstractTestListener->onConfigurationFailure");
        super.onConfigurationFailure(result);
    }

    @Override
    public void onStart(ITestContext context) {
        LOGGER.debug("AbstractTestListener->onStart(ITestContext context)");
        String uuid = StringGenerator.generateNumeric(8);
        ParameterGenerator.setUUID(uuid);

        super.onStart(context);
    }

    @Override
    public void onTestStart(ITestResult result) {
        // create new folder for test report
        ReportContext.createTestDir();
        LOGGER.debug("AbstractTestListener->onTestStart");
        LOGGER.debug("Test Directory: {}", ReportContext.getTestDir().getName());
        IRetryAnalyzer curRetryAnalyzer = getRetryAnalyzer(result);
        
        if (curRetryAnalyzer == null
                || curRetryAnalyzer instanceof DisabledRetryAnalyzer
                || curRetryAnalyzer instanceof RetryAnalyzerInterceptor) {
            // this call register retryAnalyzer.class both in Carina and Zebrunner client
            RetryService.setRetryAnalyzerClass(RetryAnalyzer.class, result.getTestContext(), result.getMethod());
            result.getMethod().setRetryAnalyzerClass(RetryAnalyzerInterceptor.class);
        } else if (!(curRetryAnalyzer instanceof RetryAnalyzerInterceptor)) {
            LOGGER.warn("Custom RetryAnalyzer is used: {}", curRetryAnalyzer.getClass().getName());
            RetryService.setRetryAnalyzerClass(curRetryAnalyzer.getClass(), result.getTestContext(), result.getMethod());
            result.getMethod().setRetryAnalyzerClass(RetryAnalyzerInterceptor.class);
        }
        
        generateParameters(result);

        //TODO: do not write STARTED at message for retry! or move it into the DEBUG level!
        startItem(result, Messager.TEST_STARTED);
        
        super.onTestStart(result);
    }
    
    private void generateParameters(ITestResult result) {
        if (result != null && result.getParameters() != null) {
            for (int i = 0; i < result.getParameters().length; i++) {
                if (result.getParameters()[i] instanceof String) {
                    result.getParameters()[i] = ParameterGenerator.process(result.getParameters()[i].toString());
                }

                if (result.getParameters()[i] instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> dynamicAgrs = (Map<String, String>) result.getParameters()[i];
                    for (Map.Entry<String, String> entry : dynamicAgrs.entrySet()) {
                        Object param = ParameterGenerator.process(entry.getValue());
                        if (param != null)
                            dynamicAgrs.put(entry.getKey(), param.toString());
                        else
                            dynamicAgrs.put(entry.getKey(), null);
                    }
                }
            }
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOGGER.debug("AbstractTestListener->onTestSuccess");
        passItem(result, Messager.TEST_PASSED);

        afterTest(result);
        super.onTestSuccess(result);
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        LOGGER.debug("AbstractTestListener->onTestFailure");
        failItem(result, Messager.TEST_FAILED);
        afterTest(result);
        super.onTestFailure(result);
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        LOGGER.debug("AbstractTestListener->onTestSkipped");
        failItem(result, Messager.TEST_SKIPPED);
        //there is no need to afterTest as it is retry failure and we wanna to proceed with the same test.log etc
        super.onTestSkipped(result);
    }

    @Override
    public void onFinish(ITestContext context) {
        LOGGER.debug("AbstractTestListener->onFinish(ITestContext context)");
        super.onFinish(context);
    }

    protected TestResultItem createTestResult(ITestResult result, TestResultType resultType, String failReason,
            String description) {
        String group = StringEscapeUtils.escapeHtml4(TestNamingService.getPackageName(result));
        
        String linkToLog = ReportConfiguration.getTestLogLink();
        String linkToScreenshots = ReportConfiguration.getTestScreenshotsLink();

        String test = StringEscapeUtils.escapeHtml4(TestNameResolverRegistry.get().resolve(result));
        return new TestResultItem(group, test, description, resultType, linkToScreenshots, linkToLog, failReason);
    }

    protected String getFailureReason(ITestResult result) {
        String errorMessage = "";
        String message = "";

        if (result.getThrowable() != null) {
            Throwable thr = result.getThrowable();
            errorMessage = getFullStackTrace(thr);
            message = thr.getMessage();
            result.getTestContext().setAttribute(SpecialKeywords.TEST_FAILURE_MESSAGE, message);
        }

        // handle in case of failed config (exclusion of expected skip)
        if (errorMessage.isEmpty()) {
            String methodName;
            Collection<ITestResult> results = result.getTestContext().getSkippedConfigurations().getAllResults();
            for (ITestResult resultItem : results) {
                methodName = resultItem.getMethod().getMethodName();
                if (methodName.equals(SpecialKeywords.BEFORE_TEST_METHOD)) {
                    errorMessage = getFullStackTrace(resultItem.getThrowable());
                }
            }
        }

        return errorMessage;
    }

    private String getFullStackTrace(Throwable thr) {
        StringBuilder stackTrace = new StringBuilder();
        if (thr != null) {
            stackTrace = new StringBuilder(thr.getMessage() + "\n");

            StackTraceElement[] elems = thr.getStackTrace();
            for (StackTraceElement elem : elems) {
                stackTrace.append("\n")
                        .append(elem.toString());
            }
        }
        return stackTrace.toString();
    }
    
    private IRetryAnalyzer getRetryAnalyzer(ITestResult result) {
        return result.getMethod().getRetryAnalyzer(result);
    }
}
