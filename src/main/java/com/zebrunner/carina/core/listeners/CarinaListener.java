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

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.zebrunner.agent.core.config.provider.SystemPropertiesConfigurationProvider;
import com.zebrunner.agent.core.webdriver.CapabilitiesCustomizerChain;
import com.zebrunner.carina.webdriver.IDriverPool;
import com.zebrunner.carina.webdriver.core.capability.CarinaCapabilitiesCustomizer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IClassListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.internal.ConfigurationMethod;
import org.testng.xml.XmlSuite;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.config.provider.PropertiesConfigurationProvider;
import com.zebrunner.agent.core.config.provider.YamlConfigurationProvider;
import com.zebrunner.agent.core.registrar.CurrentTest;
import com.zebrunner.agent.core.registrar.CurrentTestRun;
import com.zebrunner.agent.core.registrar.Label;
import com.zebrunner.agent.core.registrar.TestRail;
import com.zebrunner.agent.core.registrar.label.CompositeLabelResolver;
import com.zebrunner.agent.core.registrar.maintainer.ChainedMaintainerResolver;
import com.zebrunner.agent.core.webdriver.RemoteWebDriverFactory;
import com.zebrunner.agent.testng.core.testname.TestNameResolverRegistry;
import com.zebrunner.carina.core.IAbstractTest;
import com.zebrunner.carina.core.config.ReportConfiguration;
import com.zebrunner.carina.core.config.TestConfiguration;
import com.zebrunner.carina.core.registrar.ownership.Ownership;
import com.zebrunner.carina.core.registrar.ownership.SuiteOwnerResolver;
import com.zebrunner.carina.core.registrar.tag.PriorityManager;
import com.zebrunner.carina.core.registrar.tag.TagManager;
import com.zebrunner.carina.core.report.email.EmailReportGenerator;
import com.zebrunner.carina.core.report.email.EmailReportItemCollector;
import com.zebrunner.carina.core.report.qtest.IQTestManager;
import com.zebrunner.carina.core.report.testrail.ITestRailManager;
import com.zebrunner.carina.core.skip.ExpectedSkipManager;
import com.zebrunner.carina.core.testng.ZebrunnerNameResolver;
import com.zebrunner.carina.utils.DateUtils;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.utils.config.Configuration;
import com.zebrunner.carina.utils.encryptor.EncryptorUtils;
import com.zebrunner.carina.utils.messager.Messager;
import com.zebrunner.carina.utils.report.ReportContext;
import com.zebrunner.carina.utils.report.TestResult;
import com.zebrunner.carina.utils.report.TestResultItem;
import com.zebrunner.carina.utils.resources.L10N;
import com.zebrunner.carina.webdriver.CarinaDriver;
import com.zebrunner.carina.webdriver.Screenshot;
import com.zebrunner.carina.webdriver.ScreenshotType;
import com.zebrunner.carina.webdriver.TestPhase;
import com.zebrunner.carina.webdriver.TestPhase.Phase;
import com.zebrunner.carina.webdriver.config.WebDriverConfiguration;
import com.zebrunner.carina.webdriver.core.capability.CapabilitiesLoader;
import com.zebrunner.carina.webdriver.screenshot.DefaultSuccessfulDriverActionScreenshotRule;
import com.zebrunner.carina.webdriver.screenshot.DefaultUnSuccessfulDriverActionScreenshotRule;
import com.zebrunner.carina.webdriver.screenshot.ExplicitFullSizeScreenshotRule;
import com.zebrunner.carina.webdriver.screenshot.ExplicitVisibleScreenshotRule;
import com.zebrunner.carina.webdriver.screenshot.IScreenshotRule;

/*
 * CarinaListener - base carina-core TestNG Listener.
 *
 * @author Vadim Delendik
 */
public class CarinaListener extends AbstractTestListener implements ISuiteListener, IQTestManager, ITestRailManager, IClassListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final ThreadLocal<Boolean> IS_REMOVE_DRIVER = ThreadLocal.withInitial(() -> Boolean.FALSE);

    protected static final String SUITE_TITLE = "%s%s%s - %s (%s)";
    protected static final String XML_SUITE_NAME = " (%s)";

    protected static boolean automaticDriversCleanup = true;

    protected boolean isRunLabelsRegistered = false;

    public CarinaListener() {
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());

        // Zebrunner core java agent is user for capturing events of RemoteDriverSession instances.
        // Internally, the agent uses java instrumentation agent for its purposes.
        // The instrumentation agent implicitly triggers initialization of the R class because it uses logger.
        // Carina has the ThreadLogAppender class which is closely related to logging and internally uses the R class.
        // Technically, this happen when the maven-surefire-plugin has not set inherited program arguments (passed to mvn process).
        // That is why it is necessary to reinit R class here when TestNG loads the CarinaListener class.
        R.reinit();
        reinitAgentToken();
        reinitAgentEnv();
        CapabilitiesCustomizerChain.getInstance()
                .addLast(new CarinaCapabilitiesCustomizer());
        ReportConfiguration.removeOldReports();

        LOGGER.info(getTestRunConfigurationDescription());
        // Configuration.validateConfiguration();

        // if we initialize the logger in onStart(suite), all classes we access up to that point are initialized with INFO level
        // if me init logger here, we still lose the debug logs for this class only
        if (!"INFO".equalsIgnoreCase(Configuration.getRequired(ReportConfiguration.Parameter.CORE_LOG_LEVEL))) {
            LoggerContext ctx = (LoggerContext) LogManager.getContext(this.getClass().getClassLoader(), false);
            org.apache.logging.log4j.core.config.Configuration config = ctx.getConfiguration();
            LoggerConfig logger = config.getLoggerConfig("com.zebrunner.carina.core");
            logger.setLevel(Level.getLevel(Configuration.getRequired(ReportConfiguration.Parameter.CORE_LOG_LEVEL)));

            if ("DEBUG".equalsIgnoreCase(Configuration.getRequired(ReportConfiguration.Parameter.CORE_LOG_LEVEL))) {
                config.getLoggerConfig("io.netty")
                        .setLevel(Level.OFF);
                config.getLoggerConfig("org.asynchttpclient.netty")
                        .setLevel(Level.OFF);
            }
        }

        try {
            L10N.load();
        } catch (Exception e) {
            LOGGER.error("L10N bundle is not initialized successfully!", e);
        }

        // declare global capabilities in configuration if custom_capabilities is declared
        Configuration.get(TestConfiguration.Parameter.CUSTOM_CAPABILITIES).ifPresent(customCapabilities -> {
            // redefine core CONFIG properties using global custom capabilities file
            new CapabilitiesLoader().loadCapabilities(customCapabilities);
        });

        // declare global capabilities from Zebrunner Launcher if any
        Capabilities zebrunnerCapabilities = RemoteWebDriverFactory.getCapabilities();
        if (!zebrunnerCapabilities.asMap().isEmpty()) {
            // redefine core CONFIG properties using caps from Zebrunner launchers
            new CapabilitiesLoader().loadCapabilities(zebrunnerCapabilities);
        }

        List<IScreenshotRule> screenshotRules = List.of(
                new DefaultSuccessfulDriverActionScreenshotRule(),
                new DefaultUnSuccessfulDriverActionScreenshotRule(),
                new ExplicitFullSizeScreenshotRule(),
                new ExplicitVisibleScreenshotRule());
        Screenshot.addRules(screenshotRules);

        TestNameResolverRegistry.set(new ZebrunnerNameResolver());
        CompositeLabelResolver.addResolver(new TagManager());
        CompositeLabelResolver.addResolver(new PriorityManager());
        ReportContext.getBaseDir(); // create directory for logging as soon as possible
    }

    @Override
    public void onStart(ISuite suite) {
        LOGGER.debug("CarinaListener->onStart(ISuite suite)");

        ChainedMaintainerResolver.addLast(new SuiteOwnerResolver(suite));
        // first means that ownership/maintainer resolver from carina has higher priority
        ChainedMaintainerResolver.addFirst(new Ownership());

        setThreadCount(suite);

        WebDriverConfiguration.getCapability(CapabilityType.PLATFORM_NAME).ifPresent(platformName -> {
            if (platformName.equalsIgnoreCase(SpecialKeywords.API)) {
                CurrentTestRun.setPlatform(SpecialKeywords.API);
            }
        });

        CurrentTestRun.setLocale(WebDriverConfiguration.getLocale().toString());

        Configuration.get(ReportConfiguration.Parameter.APP_VERSION).ifPresent(appVersion -> {
            // register app_version/build as artifact if available...
            if (ConfigurationHolder.isReportingEnabled()) {
                CurrentTestRun.setBuild(appVersion);
            }
        });

        Configuration.get(ReportConfiguration.Parameter.GIT_HASH).ifPresent(hash -> {
            if (ConfigurationHolder.isReportingEnabled()) {
                Label.attachToTestRun("sha1", hash);
            }
        });

        // register owner of the run
        registerOwner();

        // register branch if available
        Configuration.get("branch")
                .ifPresent(branch -> Label.attachToTestRun("Branch", branch));

        /*
         * To support multi-suite declaration as below we have to init test run labels at once only!
         * <suite-files>
         * <suite-file path="suite1.xml"/>
         * <suite-file path="suite2.xml"/>
         * </suite-files>
         */

        if (!this.isRunLabelsRegistered) {
            attachTestRunLabels(suite);
            this.isRunLabelsRegistered = true;
        }

        LOGGER.info("CARINA_CORE_VERSION: {}", getCarinaVersion());
    }

    @Override
    public void onStart(ITestContext context) {
        LOGGER.debug("CarinaListener->OnTestStart(ITestContext context): {}", context.getName());
        super.onStart(context);
    }

    @Override
    public void beforeConfiguration(ITestResult result) {
        LOGGER.debug("CarinaListener->beforeConfiguration");
        super.beforeConfiguration(result);

        // remember active test phase to organize valid driver pool manipulation
        // process
        if (result.getMethod().isBeforeSuiteConfiguration()) {
            TestPhase.setActivePhase(Phase.BEFORE_SUITE);
        }

        if (result.getMethod().isBeforeTestConfiguration()) {
            TestPhase.setActivePhase(Phase.BEFORE_TEST);
        }

        if (result.getMethod().isBeforeClassConfiguration()) {
            TestPhase.setActivePhase(Phase.BEFORE_CLASS);
        }

        if (result.getMethod().isBeforeMethodConfiguration()) {
            TestPhase.setActivePhase(Phase.BEFORE_METHOD);
        }

        if (result.getMethod().isAfterMethodConfiguration()) {
            TestPhase.setActivePhase(Phase.AFTER_METHOD);
        }

        if (result.getMethod().isAfterClassConfiguration()) {
            TestPhase.setActivePhase(Phase.AFTER_CLASS);
        }

        if (result.getMethod().isAfterTestConfiguration()) {
            TestPhase.setActivePhase(Phase.AFTER_TEST);
        }

        if (result.getMethod().isAfterSuiteConfiguration()) {
            TestPhase.setActivePhase(Phase.AFTER_SUITE);
        }
    }

    @Override
    public void onConfigurationSuccess(ITestResult result) {
        LOGGER.debug("CarinaListener->onConfigurationSuccess");
        onConfigurationFinish(result);
        super.onConfigurationSuccess(result);
    }

    @Override
    public void onConfigurationSkip(ITestResult result) {
        LOGGER.debug("CarinaListener->onConfigurationSkip");
        onConfigurationFinish(result);
        super.onConfigurationSkip(result);
    }

    @Override
    public void onConfigurationFailure(ITestResult result) {
        LOGGER.debug("CarinaListener->onConfigurationFailure");
        onConfigurationFinish(result);
        super.onConfigurationFailure(result);
    }

    /**
     * Logic executed after configuration methods.
     *
     * 1. Remove the drivers if it was created in beforeMethod or in the test method itself
     * 
     * @param configurationResult see {@link ITestResult}
     */
    private void onConfigurationFinish(ITestResult configurationResult) {
        ITestNGMethod testMethod = configurationResult.getMethod();
        if (testMethod instanceof ConfigurationMethod) {
            ConfigurationMethod configurationMethod = (ConfigurationMethod) testMethod;
            if (configurationMethod.isAfterMethodConfiguration() &&
                    IAbstractTest.class.equals(configurationMethod.getRealClass()) &&
                    StringUtils.equals("onCarinaAfterMethod", configurationMethod.getMethodName())) {
                // If an error occurs in afterMethod , then all subsequent test methods in the class? become skipped (if run in one thread).
                // If this occurred (the number of threads is unimportant), then onCarinaAfterMethod received an incorrect ITestResult
                // object
                // (namely, as a result of calling result.getTestContext() on it, we got null, and when we tried to call .getSuite().getAllMethods()
                // we got a NullPointerException. Also, the test method status was CREATED.
                if (IS_REMOVE_DRIVER.get()) {
                    quitDrivers(Phase.BEFORE_METHOD, Phase.METHOD, Phase.AFTER_METHOD);
                }
                IS_REMOVE_DRIVER.remove();
            }
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        LOGGER.debug("CarinaListener->onTestStart");
        TestPhase.setActivePhase(Phase.METHOD);

        // handle expected skip
        Method testMethod = result.getMethod().getConstructorOrMethod().getMethod();
        if (ExpectedSkipManager.getInstance().isSkip(testMethod, result.getTestContext())) {
            skipExecution("Based on rule listed above");
        }

        super.onTestStart(result);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOGGER.debug("CarinaListener->onTestSuccess");
        onTestFinish(result);
        super.onTestSuccess(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LOGGER.debug("CarinaListener->onTestFailure");
        takeScreenshot();
        onTestFinish(result);
        super.onTestFailure(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LOGGER.debug("CarinaListener->onTestSkipped");
        takeScreenshot();
        onTestFinish(result);
        super.onTestSkipped(result);
    }

    private boolean hasDependencies(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        String className = result.getMethod().getTestClass().getName();

        // analyze all suite methods and return true if any of them depends on
        // existing method
        List<ITestNGMethod> methods = result.getTestContext().getSuite().getAllMethods();
        for (ITestNGMethod method : methods) {
            List<String> dependencies = Arrays.asList(method.getMethodsDependedUpon());

            if (dependencies.contains(methodName) ||
                    dependencies.contains(className + "." + methodName)) {
                LOGGER.debug("dependency detected for {}", methodName);
                return true;
            }
        }
        return false;
    }

    private void onTestFinish(ITestResult result) {
        try {
            // clear all kind of temporary properties
            R.CONFIG.clearTestProperties();
            R.TESTDATA.clearTestProperties();
            R.DATABASE.clearTestProperties();
            R.REPORT.clearTestProperties();
            R.ZAFIRA.clearTestProperties();
            LOGGER.debug("Test result is : {}", result.getStatus());
            // result status == 2 means failure, status == 3 means skip. We need to quit driver anyway for failure and skip
            if (((automaticDriversCleanup &&
                    !hasDependencies(result)) ||
                    result.getStatus() == 2 ||
                    result.getStatus() == 3) &&
                    !Configuration.get(TestConfiguration.Parameter.FORCIBLY_DISABLE_DRIVER_QUIT, Boolean.class).orElse(false)) {
                IS_REMOVE_DRIVER.set(Boolean.TRUE);
            }
            attachTestLabels(result);
        } catch (Exception e) {
            LOGGER.error("Exception in CarinaListener->onTestFinish!", e);
        }
    }

    @Override
    public void onAfterClass(ITestClass testClass) {
        LOGGER.debug("CarinaListener->onAfterClass(ITestClass testClass)");
        quitDrivers(Phase.BEFORE_CLASS);
    }

    @Override
    public void onFinish(ITestContext context) {
        LOGGER.debug("CarinaListener->onFinish(ITestContext context)");
        super.onFinish(context);

        // [SZ] it's still needed to close driver from BeforeClass stage.
        // Otherwise it could be potentially used in other test classes
        // quitDrivers(Phase.BEFORE_CLASS); already exited in onAfterClass() method
        quitDrivers(Phase.BEFORE_TEST);

        LOGGER.debug("CarinaListener->onFinish(context): {}", context.getName());
    }

    @Override
    public void onFinish(ISuite suite) {
        LOGGER.debug("CarinaListener->onFinish(ISuite suite)");
        try {
            String browser = WebDriverConfiguration.getBrowser().orElse("");
            String title = getTitle(suite.getXmlSuite());

            TestResult testResult = EmailReportGenerator.getSuiteResult(EmailReportItemCollector.getTestResults());
            String status = testResult.getTestResultType().getName();

            title = status + ": " + title;

            AtomicReference<String> env = new AtomicReference<>(Configuration.get(Configuration.Parameter.ENV).orElse(""));
            Configuration.get(WebDriverConfiguration.Parameter.URL).ifPresent(url -> {
                String link = String.format(" - <a href='%s'>%s</a>", url, url);
                env.set(env.get() + link);
            });

            ReportContext.getTempDir().delete();
            LOGGER.debug("Generating email report...");

            // Generate emailable html report using regular method
            EmailReportGenerator report = new EmailReportGenerator(title, env.get(),
                    Configuration.get(ReportConfiguration.Parameter.APP_VERSION).orElse(""),
                    browser, DateUtils.now(), EmailReportItemCollector.getTestResults(),
                    EmailReportItemCollector.getCreatedItems());

            String emailContent = report.getEmailBody();
            // Store emailable report under emailable-report.html
            ReportConfiguration.generateHtmlReport(emailContent);

            printExecutionSummary(EmailReportItemCollector.getTestResults());

            LOGGER.debug("Finish email report generation.");

        } catch (Exception e) {
            LOGGER.error("Exception in CarinaListener->onFinish(ISuite suite)", e);
        }
    }

    /**
     * Disable automatic drivers cleanup after each TestMethod and switch to controlled by tests itself.
     * But anyway all drivers will be closed forcibly as only suite is finished or aborted
     */
    public static void disableDriversCleanup() {
        LOGGER.info("Automatic drivers cleanup will be disabled!");
        automaticDriversCleanup = false;
    }

    protected String getTitle(XmlSuite suite) {
        AtomicReference<String> browser = new AtomicReference<>("");
        // insert the space before
        WebDriverConfiguration.getBrowser().ifPresent(b -> browser.set(" " + b));

        String env = Configuration.get(Configuration.Parameter.ENV)
                .orElse(Configuration.get(WebDriverConfiguration.Parameter.URL).orElse(""));

        String title = "";
        AtomicReference<String> appVersion = new AtomicReference<>("");
        Configuration.get(ReportConfiguration.Parameter.APP_VERSION).ifPresent(version -> {
            // if nothing is specified then title will contain nothing
            appVersion.set(version + " - ");
        });

        String suiteName = getSuiteName(suite);
        String xmlFile = getSuiteFileName(suite);
        title = String.format(SUITE_TITLE, appVersion.get(), suiteName, String.format(XML_SUITE_NAME, xmlFile), env, browser.get());
        return title;
    }

    private String getSuiteFileName(XmlSuite suite) {
        // TODO: investigate why we need such method and suite file name at all
        String fileName = suite.getFileName();
        if (fileName == null) {
            fileName = "undefined";
        }
        LOGGER.debug("Full suite file name: {}", fileName);
        if (fileName.contains("\\")) {
            fileName = fileName.replaceAll("\\\\", "/");
        }
        fileName = StringUtils.substringAfterLast(fileName, "/");
        LOGGER.debug("Short suite file name: {}", fileName);
        return fileName;
    }

    protected String getSuiteName(XmlSuite suite) {
        String suiteName = "";
        if (suite != null && !"Default suite".equals(suite.getName())) {
            suiteName = Configuration.get(ReportConfiguration.Parameter.SUITE_NAME).orElse(suite.getName());
        } else {
            suiteName = Configuration.get(ReportConfiguration.Parameter.SUITE_NAME).orElseThrow();
        }
        return suiteName;
    }

    private void printExecutionSummary(List<TestResultItem> tris) {
        Messager.INFORMATION.info("**************** Test execution summary ****************");
        int num = 1;
        for (TestResultItem tri : tris) {
            String failReason = tri.getFailReason();
            if (failReason == null) {
                failReason = "";
            }

            if (!tri.isConfig()) {
                String reportLinks = !StringUtils.isEmpty(tri.getLinkToScreenshots())
                        ? "screenshots=" + tri.getLinkToScreenshots() + " | "
                        : "";
                reportLinks += !StringUtils.isEmpty(tri.getLinkToLog()) ? "log=" + tri.getLinkToLog() : "";
                Messager.TEST_RESULT.info(String.valueOf(num++), tri.getTest(), tri.getResult().toString(),
                        reportLinks);
            }
        }
    }

    protected void skipExecution(String message) {
        CurrentTest.revertRegistration();
        throw new SkipException(message);
    }

    /*
     * Parse TestNG <suite ...> tag and return any attribute
     * 
     * @param ISuite suite
     * 
     * @param IString attribute
     * 
     * @return String attribute value or empty string
     *
     */
    private String getAttributeValue(ISuite suite, String attribute) {
        String res = "";

        if (suite.getXmlSuite() == null || suite.getXmlSuite().getFileName() == null) {
            // #1514 Unable to execute the test classes from maven command line
            return res;
        }

        File file = new File(suite.getXmlSuite().getFileName());
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        documentBuilderFactory.setValidating(false);
        documentBuilderFactory.setNamespaceAware(true);
        try {
            documentBuilderFactory.setFeature("http://xml.org/sax/features/namespaces", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/validation", false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(file);

            for (int i = 0; i < document.getChildNodes().getLength(); i++) {
                NamedNodeMap nodeMapAttributes = document.getChildNodes().item(i).getAttributes();
                if (nodeMapAttributes == null) {
                    continue;
                }

                // get "name" from suite element
                // <suite verbose="1" name="Carina Demo Tests - API Sample" thread-count="3" >
                Node nodeName = nodeMapAttributes.getNamedItem("name");
                if (nodeName == null) {
                    continue;
                }

                if (suite.getName().equals(nodeName.getNodeValue())) {
                    // valid suite node detected
                    Node nodeAttribute = nodeMapAttributes.getNamedItem(attribute);
                    if (nodeAttribute != null) {
                        res = nodeAttribute.getNodeValue();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to get attribute '" + attribute + "' from suite: " + suite.getXmlSuite().getFileName(), e);
        }

        return res;

    }

    private void setThreadCount(ISuite suite) {
        // Reuse default thread-count value from suite TestNG file if it is not overridden in _config.properties

        /*
         * WARNING! We coudn't override default thread-count="5" and data-provider-thread-count="10"!
         * suite.getXmlSuite().toXml() add those default values anyway even if the absent in suite xml file declaraton.
         * To make possible to parse correctly we had to reuse external parser and private getAttributeValue
         */

        Optional<Integer> threadCount = Configuration.get(TestConfiguration.Parameter.THREAD_COUNT, Integer.class);
        if (threadCount.isPresent() && threadCount.get() >= 1) {
            // use thread-count from config.properties
            suite.getXmlSuite().setThreadCount(threadCount.get());
            LOGGER.debug("Updated thread-count={}", suite.getXmlSuite().getThreadCount());
        } else {
            String suiteThreadCount = getAttributeValue(suite, "thread-count");
            LOGGER.debug("thread-count from suite: {}", suiteThreadCount);
            if (suiteThreadCount.isEmpty()) {
                LOGGER.info("Set thread-count=1");
                R.CONFIG.put(TestConfiguration.Parameter.THREAD_COUNT.getKey(), "1");
                suite.getXmlSuite().setThreadCount(1);
            } else {
                // reuse value from suite xml file
                LOGGER.debug("Synching thread-count with values from suite xml file...");
                R.CONFIG.put(TestConfiguration.Parameter.THREAD_COUNT.getKey(), suiteThreadCount);
                LOGGER.info("Use thread-count='{}' from suite file.", suite.getXmlSuite().getThreadCount());
            }
        }

        Optional<Integer> dataProviderThreadCount = Configuration.get(TestConfiguration.Parameter.DATA_PROVIDER_THREAD_COUNT, Integer.class);

        if (dataProviderThreadCount.isPresent() && dataProviderThreadCount.get() >= 1) {
            // use thread-count from config.properties
            suite.getXmlSuite().setDataProviderThreadCount(dataProviderThreadCount.get());
            LOGGER.debug("Updated data-provider-thread-count={}", suite.getXmlSuite().getDataProviderThreadCount());
        } else {
            String suiteDataProviderThreadCount = getAttributeValue(suite, "data-provider-thread-count");
            LOGGER.debug("data-provider-thread-count from suite: {}", suiteDataProviderThreadCount);

            if (suiteDataProviderThreadCount.isEmpty()) {
                LOGGER.info("Set data-provider-thread-count=1");
                R.CONFIG.put(TestConfiguration.Parameter.DATA_PROVIDER_THREAD_COUNT.getKey(), "1");
                suite.getXmlSuite().setDataProviderThreadCount(1);
            } else {
                // reuse value from suite xml file
                LOGGER.debug("Synching data-provider-thread-count with values from suite xml file...");
                R.CONFIG.put(TestConfiguration.Parameter.DATA_PROVIDER_THREAD_COUNT.getKey(), suiteDataProviderThreadCount);
                LOGGER.info("Use data-provider-thread-count='{}' from suite file.", suite.getXmlSuite().getDataProviderThreadCount());
            }
        }
    }

    private String getCarinaVersion() {

        String carinaVersion = "";
        try {
            Class<CarinaListener> theClass = CarinaListener.class;

            String classPath = theClass.getResource(theClass.getSimpleName() + ".class").toString();
            LOGGER.debug("Class: {}", classPath);

            Pattern pattern = Pattern.compile(".*\\/(.*)\\/.*!");
            Matcher matcher = pattern.matcher(classPath);

            if (matcher.find()) {
                carinaVersion = matcher.group(1);
            }
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }

        return carinaVersion;
    }

    private void attachTestLabels(ITestResult result) {
        // register testrail cases...
        Set<String> trCases = getTestRailCasesUuid(result);
        for (String trCase : trCases) {
            TestRail.setTestCaseId(trCase);
        }

        // register qtest cases...
        Set<String> qtestCases = getQTestCasesUuid(result);
        if (!qtestCases.isEmpty()) {
            Label.attachToTest(SpecialKeywords.QTEST_TESTCASE_UUID, Arrays.copyOf(qtestCases.toArray(), qtestCases.size(), String[].class));
        }
    }

    private void attachTestRunLabels(ISuite suite) {
        String trSuite = getTestRailSuiteId(suite);

        if (!trSuite.isEmpty()) {
            TestRail.setSuiteId(trSuite);
        }

        // read command line argument to improve test rail integration capabilities.
        if (!Configuration.getRequired(ReportConfiguration.Parameter.TESTRAIL_ENABLED, Boolean.class)) {
            LOGGER.debug("disable TestRail integration!");
            TestRail.disableSync();
        }

        if (Configuration.getRequired(ReportConfiguration.Parameter.INCLUDE_ALL, Boolean.class)) {
            LOGGER.info("enable include_all for TestRail integration!");
            TestRail.includeAllTestCasesInNewRun();
        }

        Configuration.get(ReportConfiguration.Parameter.MILESTONE).ifPresent(milestone -> {
            LOGGER.info("Set TestRail milestone name: {}", milestone);
            TestRail.setMilestone(milestone);
        });

        Configuration.get(ReportConfiguration.Parameter.RUN_NAME).ifPresent(runName -> {
            LOGGER.info("Set TestRail run name: {}", runName);
            TestRail.setRunName(runName);
        });

        Configuration.get(ReportConfiguration.Parameter.ASSIGNEE).ifPresent(assignee -> {
            LOGGER.info("Set TestRail assignee: {}", assignee);
            TestRail.setAssignee(assignee);
        });

        String qtestProject = getQTestProjectId(suite);
        if (!qtestProject.isEmpty()) {
            Label.attachToTestRun(SpecialKeywords.QTEST_PROJECT_ID, qtestProject);
        }
    }

    /*
     * Capture screenshots for all available drivers after test fail/skip.
     * Request full size error screenshots if allowed by IScreenshotRules (allow_fullsize_screenshot property)
     * 
     * @param msg String comment
     * 
     */
    private void takeScreenshot() {
        Map<String, CarinaDriver> drivers = IDriverPool.getDrivers();
        try {
            for (Map.Entry<String, CarinaDriver> entry : drivers.entrySet()) {
                WebDriver drv = entry.getValue().getDriver();
                Screenshot.capture(drv, ScreenshotType.UNSUCCESSFUL_DRIVER_ACTION);
            }
        } catch (Throwable thr) {
            LOGGER.error("Failure detected on screenshot generation after failure: ", thr);
        }
    }

    public static class ShutdownHook extends Thread {

        private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownHook.class);

        private void quitAllDriversOnHook() throws InterruptedException {
            List<ImmutablePair<Long, String>> drivers4Close = new ArrayList<>();
            for (Map<String, CarinaDriver> drivers : IDriverPool.DRIVERS_POOL.values()) {
                drivers.keySet().forEach(key -> drivers4Close.add(new ImmutablePair<>(drivers.get(key).getThreadId(), key)));
            }
            drivers4Close.forEach(driver -> IDriverPool.quitDriver(driver.getRight(), driver.getLeft()));
            IDriverPool.EXECUTOR_SERVICE.shutdown();

            IDriverPool.EXECUTOR_SERVICE.awaitTermination(10, TimeUnit.MINUTES);
        }

        @Override
        public void run() {
            LOGGER.debug("Running shutdown hook");
            if (!Configuration.get(TestConfiguration.Parameter.FORCIBLY_DISABLE_DRIVER_QUIT, Boolean.class).orElse(false)) {
                try {
                    quitAllDriversOnHook();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    /**
     * Register owner launch attribute<br>
     * -DBUILD_USER_ID="develop"<br>
     * 1. read from system properties BUILD_USER_ID<br>
     * 2. if it is not empty -> register as owner label for run/launch<br>
     * 3. if it is empty -> read env var USERNAME<br>
     * 4. if not empty -> register as owner<br>
     * 5.Label.attachToTestRun("Author", "Andrei Kamarouski");<br>
     */
    private void registerOwner() {
        String owner = System.getProperty("BUILD_USER_ID");
        if (owner == null || owner.isEmpty()) {
            owner = System.getenv("BUILD_USER_ID");
        }

        if (owner == null || owner.isEmpty()) {
            owner = System.getenv("USERNAME");
        }

        if (owner != null && !owner.isEmpty()) {
            Label.attachToTestRun("Owner", owner);
        }
    }

    /**
     * Get the value of the token (system/yaml/properties)
     * and write it to the system properties
     */
    private static void reinitAgentToken() {
        String accessToken = new SystemPropertiesConfigurationProvider()
                .getConfiguration()
                .getServer()
                .getAccessToken();
        if (StringUtils.isBlank(accessToken)) {
            accessToken = new YamlConfigurationProvider()
                    .getConfiguration()
                    .getServer()
                    .getAccessToken();
        }
        if (StringUtils.isBlank(accessToken)) {
            accessToken = new PropertiesConfigurationProvider()
                    .getConfiguration()
                    .getServer()
                    .getAccessToken();
        }
        if (StringUtils.isNotBlank(accessToken)) {
            System.setProperty("reporting.server.accessToken", EncryptorUtils.decrypt(accessToken));
        }
    }

    private static void reinitAgentEnv() {
        Configuration.get(Configuration.Parameter.ENV).ifPresent(configEnv -> {
            String agentEnv = new SystemPropertiesConfigurationProvider()
                    .getConfiguration()
                    .getRun()
                    .getEnvironment();
            if (StringUtils.isBlank(agentEnv)) {
                agentEnv = new YamlConfigurationProvider()
                        .getConfiguration()
                        .getRun()
                        .getEnvironment();
            }
            if (StringUtils.isBlank(agentEnv)) {
                agentEnv = new PropertiesConfigurationProvider()
                        .getConfiguration()
                        .getRun()
                        .getEnvironment();
            }

            if (StringUtils.isBlank(agentEnv)) {
                System.setProperty("reporting.run.environment", configEnv);
            }
        });
    }

    private static String getTestRunConfigurationDescription() {
        return StringUtils.chomp(new Configuration().toString()) + new Reflections(new ConfigurationBuilder()
                .setScanners(Scanners.SubTypes)
                .forPackages("com.zebrunner.carina")).getSubTypesOf(Configuration.class)
                        .stream().map(clazz -> {
                            try {
                                return ConstructorUtils.invokeConstructor(clazz);
                            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                                throw new RuntimeException(String.format("Cannot create instance of Configuration class: '%s'", clazz));
                            }
                        })
                        .map(Configuration::toString)
                        .map(StringUtils::chomp)
                        .reduce("", String::concat)
                + "\n===============================================\n";
    }
}
