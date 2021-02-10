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
package com.qaprosoft.carina.core.foundation.listeners;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.internal.annotations.DisabledRetryAnalyzer;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.DSBean;
import com.qaprosoft.carina.core.foundation.jira.Jira;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.report.TestResultItem;
import com.qaprosoft.carina.core.foundation.report.TestResultType;
import com.qaprosoft.carina.core.foundation.report.email.EmailReportItemCollector;
import com.qaprosoft.carina.core.foundation.retry.RetryAnalyzer;
import com.qaprosoft.carina.core.foundation.utils.DateUtils;
import com.qaprosoft.carina.core.foundation.utils.Messager;
import com.qaprosoft.carina.core.foundation.utils.ParameterGenerator;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.StringGenerator;
import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.zebrunner.agent.testng.core.retry.RetryAnalyzerInterceptor;
import com.zebrunner.agent.testng.core.testname.TestNameResolverRegistry;
import com.zebrunner.agent.testng.listener.RetryService;

public class AbstractTestListener extends TestListenerAdapter implements IDriverPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private void startItem(ITestResult result, Messager messager) {
        String test = TestNameResolverRegistry.get().resolve(result);
        String deviceName = getDeviceName();
        messager.info(deviceName, test, DateUtils.now());
    }

    private void passItem(ITestResult result, Messager messager) {
        String test = TestNameResolverRegistry.get().resolve(result);

        String deviceName = getDeviceName();

        messager.info(deviceName, test, DateUtils.now());

        EmailReportItemCollector
                .push(createTestResult(result, TestResultType.PASS, null, result.getMethod().getDescription()));
        result.getTestContext().removeAttribute(SpecialKeywords.TEST_FAILURE_MESSAGE);

    }

    private String failItem(ITestResult result, Messager messager) {
        String test = TestNameResolverRegistry.get().resolve(result);

        String errorMessage = getFailureReason(result);
        String deviceName = getDeviceName();

        // TODO: remove hard-coded text
        if (!errorMessage.contains("All tests were skipped! Analyze logs to determine possible configuration issues.")) {
            messager.error(deviceName, test, DateUtils.now(), errorMessage);
            if (!R.EMAIL.getBoolean("fail_full_stacktrace_in_report") && result.getThrowable() != null
                    && result.getThrowable().getMessage() != null
                    && !StringUtils.isEmpty(result.getThrowable().getMessage())) {
                EmailReportItemCollector.push(createTestResult(result, TestResultType.FAIL,
                        result.getThrowable().getMessage(), result.getMethod().getDescription()));
            } else {
                EmailReportItemCollector.push(createTestResult(result, TestResultType.FAIL, errorMessage, result
                        .getMethod().getDescription()));
            }
        }

        result.getTestContext().removeAttribute(SpecialKeywords.TEST_FAILURE_MESSAGE);
        return errorMessage;
    }

    private String getDeviceName() {
        String deviceName = IDriverPool.getDefaultDevice().getName();
        String deviceUdid = IDriverPool.getDefaultDevice().getUdid();

        if (!deviceName.isEmpty() && !deviceUdid.isEmpty()) {
            deviceName = deviceName + " - " + deviceUdid;
        }

        return deviceName;
    }

    private void afterTest(ITestResult result) {
        // do not publish log/demo anymore
        //Artifacts.add("Logs", ReportContext.getTestLogLink(test));
        //Artifacts.add("Demo", ReportContext.getTestScreenshotsLink(test));
        
        ReportContext.generateTestReport();
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

        ReportContext.getBaseDir(); // create directory for logging as soon as possible

        super.onStart(context);
    }

    @Override
    public void onTestStart(ITestResult result) {
        LOGGER.debug("AbstractTestListener->onTestStart");
        IRetryAnalyzer curRetryAnalyzer = getRetryAnalyzer(result);
        
        if (curRetryAnalyzer == null
                || curRetryAnalyzer instanceof DisabledRetryAnalyzer
                || curRetryAnalyzer instanceof RetryAnalyzerInterceptor) {
            // this call register retryAnalyzer.class both in Carina and Zebrunner client
            RetryService.setRetryAnalyzerClass(RetryAnalyzer.class, result.getTestContext(), result.getMethod());
            result.getMethod().setRetryAnalyzerClass(RetryAnalyzerInterceptor.class);
        } else if (!(curRetryAnalyzer instanceof RetryAnalyzerInterceptor)) {
            LOGGER.warn("Custom RetryAnalyzer is used: " + curRetryAnalyzer.getClass().getName());
            RetryService.setRetryAnalyzerClass(curRetryAnalyzer.getClass(), result.getTestContext(), result.getMethod());
            result.getMethod().setRetryAnalyzerClass(RetryAnalyzerInterceptor.class);
        }
        
        generateParameters(result);

        if (!result.getTestContext().getCurrentXmlTest().getAllParameters()
                .containsKey(SpecialKeywords.EXCEL_DS_CUSTOM_PROVIDER) &&
                result.getParameters().length > 0) // set parameters from XLS only if test contains any parameter at
                                                   // all)
        {
            if (result.getTestContext().getCurrentXmlTest().getAllParameters()
                    .containsKey(SpecialKeywords.EXCEL_DS_ARGS)) {
                DSBean dsBean = new DSBean(result.getTestContext());
                int index = 0;
                for (String arg : dsBean.getArgs()) {
                    dsBean.getTestParams().put(arg, (String) result.getParameters()[index++]);
                }
                result.getTestContext().getCurrentXmlTest().setParameters(dsBean.getTestParams());

            }
        }

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
        
        String linkToLog = ReportContext.getTestLogLink();
        String linkToScreenshots = ReportContext.getTestScreenshotsLink();
        List<String> linksToVideo = ReportContext.getTestVideoLinks(getSessionsForCurrentTest());

        String test = StringEscapeUtils.escapeHtml4(TestNameResolverRegistry.get().resolve(result));
        TestResultItem testResultItem = new TestResultItem(group, test, resultType, linkToScreenshots, linkToLog, linksToVideo, failReason);
        testResultItem.setDescription(description);
        // AUTO-1081 eTAF report does not show linked Jira tickets if test PASSED
        // jira tickets should be used for tracking tasks. application issues will be tracked by planned zafira feature
        testResultItem.setJiraTickets(Jira.getTickets(result));
        return testResultItem;
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
        String stackTrace = "";

        if (thr != null) {
            stackTrace = thr.getMessage() + "\n";

            StackTraceElement[] elems = thr.getStackTrace();
            for (StackTraceElement elem : elems) {
                stackTrace = stackTrace + "\n" + elem.toString();
            }
        }
        return stackTrace;
    }
    
    private IRetryAnalyzer getRetryAnalyzer(ITestResult result) {
        return result.getMethod().getRetryAnalyzer(result);
    }

}
