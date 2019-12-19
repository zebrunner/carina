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
package com.qaprosoft.carina.core.foundation.listeners;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.testng.Assert;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.qaprosoft.amazon.AmazonS3Manager;
import com.qaprosoft.carina.browsermobproxy.ProxyPool;
import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.jira.Jira;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.report.TestResultItem;
import com.qaprosoft.carina.core.foundation.report.TestResultType;
import com.qaprosoft.carina.core.foundation.report.email.EmailReportGenerator;
import com.qaprosoft.carina.core.foundation.report.email.EmailReportItemCollector;
import com.qaprosoft.carina.core.foundation.skip.ExpectedSkipManager;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.DateUtils;
import com.qaprosoft.carina.core.foundation.utils.JsonUtils;
import com.qaprosoft.carina.core.foundation.utils.Messager;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.async.AsyncOperation;
import com.qaprosoft.carina.core.foundation.utils.metadata.MetadataCollector;
import com.qaprosoft.carina.core.foundation.utils.metadata.model.ElementsInfo;
import com.qaprosoft.carina.core.foundation.utils.resources.I18N;
import com.qaprosoft.carina.core.foundation.utils.resources.L10N;
import com.qaprosoft.carina.core.foundation.utils.resources.L10Nparser;
import com.qaprosoft.carina.core.foundation.webdriver.CarinaDriver;
import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.Screenshot;
import com.qaprosoft.carina.core.foundation.webdriver.TestPhase;
import com.qaprosoft.carina.core.foundation.webdriver.TestPhase.Phase;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.CapabilitiesLoader;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.appcenter.AppCenterManager;
import com.qaprosoft.zafira.client.ZafiraSingleton;
import com.qaprosoft.zafira.listener.ZafiraEventRegistrar;
import com.qaprosoft.zafira.models.dto.TestRunType;

/*
 * CarinaListener - base carin-core TestNG Listener.
 * 
 * @author Vadim Delendik
 */
public class CarinaListener extends AbstractTestListener implements ISuiteListener {
    private static final Logger LOGGER = Logger.getLogger(CarinaListener.class);

    protected static final long EXPLICIT_TIMEOUT = Configuration.getLong(Parameter.EXPLICIT_TIMEOUT);

    protected static final String SUITE_TITLE = "%s%s%s - %s (%s%s)";
    protected static final String XML_SUITE_NAME = " (%s)";
    
    protected static boolean automaticDriversCleanup = true; 

    static {
        try {
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new ShutdownHook());
            // Set log4j properties
            URL log4jUrl = ClassLoader.getSystemResource("carina-log4j.properties");
            LOGGER.debug("carina-log4j.properties: " + log4jUrl);
            PropertyConfigurator.configure(log4jUrl);

            LOGGER.info(Configuration.asString());
            // Configuration.validateConfiguration();

            try {
                L10N.init();
            } catch (Exception e) {
                LOGGER.error("L10N bundle is not initialized successfully!", e);
            }

            try {
                I18N.init();
            } catch (Exception e) {
                LOGGER.error("I18N bundle is not initialized successfully!", e);
            }

            try {
                L10Nparser.init();
            } catch (Exception e) {
                LOGGER.error("L10Nparser bundle is not initialized successfully!", e);
            }

            // declare global capabilities in configuration if custom_capabilities is declared 
            String customCapabilities = Configuration.get(Parameter.CUSTOM_CAPABILITIES);
            if (!customCapabilities.isEmpty()) {
                // redefine core CONFIG properties using global custom capabilities file
                new CapabilitiesLoader().loadCapabilities(customCapabilities);
            }

            updateAppPath();

        } catch (Exception e) {
            LOGGER.error("Undefined failure during static carina listener init!", e);
        }
    }

    @Override
    public void onStart(ISuite suite) {
        // register programmatically carina based BeforeSuite/BeforeClass and
        // BeforeMethod to execute those configuration part obligatory
        /*
         * XmlTest xmlTest = new XmlTest(suite.getXmlSuite());
         * xmlTest.setName("Sample Test");
         * 
         * // Create a list which can contain the classes that you want to run.
         * List<XmlClass> myClasses = new ArrayList<XmlClass>();
         * myClasses.add(new
         * XmlClass("com.qaprosoft.carina.core.foundation.AbstractTest"));
         * 
         * // Assign that to the XmlTest Object created earlier.
         * xmlTest.setXmlClasses(myClasses);
         * 
         * suite.getXmlSuite().addTest(xmlTest);
         */

        List<String> coreLogPackages = new ArrayList<String>(
                Arrays.asList(Configuration.get(Parameter.CORE_LOG_PACKAGES).split(",")));
        if (coreLogPackages.size() > 0 && !"INFO".equalsIgnoreCase(Configuration.get(Parameter.CORE_LOG_LEVEL))) {
            // do core log level change only if custom properties are declared
            try {
                Logger root = Logger.getRootLogger();
                Enumeration<?> allLoggers = root.getLoggerRepository().getCurrentCategories();
                while (allLoggers.hasMoreElements()) {
                    Category tmpLogger = (Category) allLoggers.nextElement();
                    LOGGER.debug("loggerName: " + tmpLogger.getName());
                    if ("log4j.logger.org.apache.http.wire".equals(tmpLogger.getName())) {
                        // update this logger to be able to analyse ZafiraClient calls 
                        LOGGER.info("Updaged logger level for '" + tmpLogger.getName() + "' to "
                                + Configuration.get(Parameter.CORE_LOG_LEVEL));
                        tmpLogger.setLevel(Level.toLevel(Configuration.get(Parameter.CORE_LOG_LEVEL)));
                    }
                    for (String coreLogPackage : coreLogPackages) {
                        if (tmpLogger.getName().contains(coreLogPackage.trim())) {
                            LOGGER.info("Updaged logger level for '" + tmpLogger.getName() + "' to "
                                    + Configuration.get(Parameter.CORE_LOG_LEVEL));
                            tmpLogger.setLevel(Level.toLevel(Configuration.get(Parameter.CORE_LOG_LEVEL)));
                        }
                    }
                }
            } catch (NoSuchMethodError e) {
                LOGGER.error("Unable to redefine logger level due to the conflicts between log4j and slf4j!");
            }
        }

        // TODO: moved into separate class/method
        LOGGER.debug("Default thread_count=" + suite.getXmlSuite().getThreadCount());
        suite.getXmlSuite().setThreadCount(Configuration.getInt(Parameter.THREAD_COUNT));
        LOGGER.debug("Updated thread_count=" + suite.getXmlSuite().getThreadCount());

        // update DataProviderThreadCount if any property is provided otherwise
        // sync with value from suite xml file
        int count = Configuration.getInt(Parameter.DATA_PROVIDER_THREAD_COUNT);
        if (count > 0) {
            LOGGER.debug("Updated 'data_provider_thread_count' from " + suite.getXmlSuite().getDataProviderThreadCount()
                    + " to " + count);
            suite.getXmlSuite().setDataProviderThreadCount(count);
        } else {
            LOGGER.debug("Synching data_provider_thread_count with values from suite xml file...");
            R.CONFIG.put(Parameter.DATA_PROVIDER_THREAD_COUNT.getKey(),
                    String.valueOf(suite.getXmlSuite().getDataProviderThreadCount()));
            LOGGER.debug("Updated 'data_provider_thread_count': "
                    + Configuration.getInt(Parameter.DATA_PROVIDER_THREAD_COUNT));
        }

        LOGGER.debug("Default data_provider_thread_count=" + suite.getXmlSuite().getDataProviderThreadCount());
        LOGGER.debug("Updated data_provider_thread_count=" + suite.getXmlSuite().getDataProviderThreadCount());

        onHealthCheck(suite);
        
        LOGGER.info("CARINA_CORE_VERSION: " + getCarinaVersion());
    }

	private String getCarinaVersion() {

		String carinaVersion = "";
		try {
			Class<CarinaListener> theClass = CarinaListener.class;

			String classPath = theClass.getResource(theClass.getSimpleName() + ".class").toString();
			LOGGER.debug("Class: " + classPath);

			Pattern pattern = Pattern.compile(".*\\/(.*)\\/.*!");
			Matcher matcher = pattern.matcher(classPath);

			if (matcher.find()) {
				carinaVersion = matcher.group(1);
			}
		} catch (Exception e) {
			LOGGER.debug(e);
		}

		return carinaVersion;
	}

	@Override
    public void onStart(ITestContext context) {
        LOGGER.debug("CarinaListener->OnTestStart(context): " + context.getName());
        super.onStart(context);
    }

    @Override
    public void beforeConfiguration(ITestResult result) {
        super.beforeConfiguration(result);
        // remember active test phase to organize valid driver pool manipulation
        // process
        if (result.getMethod().isBeforeSuiteConfiguration()) {
            TestPhase.setActivePhase(Phase.BEFORE_SUITE);
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

        if (result.getMethod().isAfterSuiteConfiguration()) {
            TestPhase.setActivePhase(Phase.AFTER_SUITE);
        }
    }
    
    @Override
    public void onConfigurationFailure(ITestResult result) {
        String errorMessage = getFailureReason(result);
        takeScreenshot(result, "CONFIGURATION FAILED - " + errorMessage);

        super.onConfigurationFailure(result);
    }

    @Override
    public void onTestStart(ITestResult result) {
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
        onTestFinish(result);
        super.onTestSuccess(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String errorMessage = getFailureReason(result);
        takeScreenshot(result, "TEST FAILED - " + errorMessage);
        
        onTestFinish(result);
        super.onTestFailure(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String errorMessage = getFailureReason(result);
        takeScreenshot(result, "TEST FAILED - " + errorMessage);
        
        onTestFinish(result);
        super.onTestSkipped(result);
    }

    private boolean hasDependencies(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        String className = result.getMethod().getTestClass().getName();
        LOGGER.debug("current method: " + className + "." + methodName);

        // analyze all suite methods and return true if any of them depends on
        // existing method
        List<ITestNGMethod> methods = result.getTestContext().getSuite().getAllMethods();
        for (ITestNGMethod method : methods) {
            LOGGER.debug("analyze method for dependency: " + method.getMethodName());
            
            List<String> dependencies = Arrays.asList(method.getMethodsDependedUpon());

            if (dependencies.contains(methodName) ||
                    dependencies.contains(className + "." + methodName)) {
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
            R.EMAIL.clearTestProperties();
            R.REPORT.clearTestProperties();
            R.ZAFIRA.clearTestProperties();
            
            LOGGER.debug("Test result is : " + result.getStatus());
            // result status == 2 means failure, status == 3 means skip. We need to quit driver anyway for failure and skip
            if ((automaticDriversCleanup && !hasDependencies(result)) || result.getStatus() == 2 || result.getStatus() == 3) {
                quitDrivers(Phase.BEFORE_METHOD, Phase.METHOD);
            }

            // TODO: improve later removing duplicates with AbstractTestListener
            // handle Zafira already passed exception for re-run and do nothing.
            // maybe return should be enough
            if (result.getThrowable() != null && result.getThrowable().getMessage() != null
                    && result.getThrowable().getMessage().startsWith(SpecialKeywords.ALREADY_PASSED)) {
                // [VD] it is prohibited to release TestInfoByThread in this
                // place.!
                return;
            }

            // handle CarinaListener->SkipExecution
            if (result.getThrowable() != null && result.getThrowable().getMessage() != null
                    && result.getThrowable().getMessage().startsWith(SpecialKeywords.SKIP_EXECUTION)) {
                // [VD] it is prohibited to release TestInfoByThread in this
                // place.!
                return;
            }

            List<String> tickets = Jira.getTickets(result);
            result.setAttribute(SpecialKeywords.JIRA_TICKET, tickets);
            Jira.updateAfterTest(result);

            // we shouldn't deregister info here as all retries will not work
            // TestNamingUtil.releaseZafiraTest();

        } catch (Exception e) {
            LOGGER.error("Exception in CarinaListener->onTestFinish!", e);
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        super.onFinish(context);

        // [SZ] it's still needed to close driver from BeforeClass stage.
        // Otherwise it could be potentially used in other test classes 
        quitDrivers(Phase.BEFORE_CLASS);

        LOGGER.debug("CarinaListener->onFinish(context): " + context.getName());

        // TODO: refactor jira updater to make it as functional interface
        // Update JIRA
        Jira.updateAfterSuite(context, EmailReportItemCollector.getTestResults());
    }

    @Override
    public void onFinish(ISuite suite) {
        try {
            // TODO: quitAllDivers forcibly

            ReportContext.removeTempDir(); // clean temp artifacts directory
            // HtmlReportGenerator.generate(ReportContext.getBaseDir().getAbsolutePath());

            String browser = getBrowser();
            String deviceName = getDeviceName();
            // String suiteName = getSuiteName(context);
            String title = getTitle(suite.getXmlSuite());

            TestResultType testResult = EmailReportGenerator.getSuiteResult(EmailReportItemCollector.getTestResults());
            String status = testResult.getName();

            title = status + ": " + title;

            String env = "";
            if (!Configuration.isNull(Parameter.ENV)) {
                env = Configuration.get(Parameter.ENV);
            }

            if (!Configuration.get(Parameter.URL).isEmpty()) {
                env += " - <a href='" + Configuration.get(Parameter.URL) + "'>" + Configuration.get(Parameter.URL)
                        + "</a>";
            }

            ReportContext.getTempDir().delete();

            // // Update JIRA
            // Jira.updateAfterSuite(context,
            // EmailReportItemCollector.getTestResults());

            LOGGER.debug("Generating email report...");

            // Generate emailable html report using regular method
            EmailReportGenerator report = new EmailReportGenerator(title, env, Configuration.get(Parameter.APP_VERSION),
                    deviceName, browser, DateUtils.now(), EmailReportItemCollector.getTestResults(),
                    EmailReportItemCollector.getCreatedItems());

            String emailContent = report.getEmailBody();
            // Store emailable report under emailable-report.html
            ReportContext.generateHtmlReport(emailContent);

            printExecutionSummary(EmailReportItemCollector.getTestResults());

            TestResultType suiteResult = EmailReportGenerator.getSuiteResult(EmailReportItemCollector.getTestResults());
            switch (suiteResult) {
            case SKIP_ALL:
                Assert.fail("All tests were skipped! Analyze logs to determine possible configuration issues.");
                break;
            case SKIP_ALL_ALREADY_PASSED:
                LOGGER.info(
                        "Nothing was executed in rerun mode because all tests already passed and registered in Zafira Repoting Service!");
                break;
            default:
                // do nothing
            }
            LOGGER.debug("Finish email report generation.");

        } catch (Exception e) {
            LOGGER.error("Exception in CarinaListener->onFinish(ISuite suite)", e);
        } finally {
            // wait until all async  operations (i.e. artifacts uploading) are finished
            AsyncOperation.waitUntilFinish(30);
        }
    }
    
    /**
     * Disable automatic drivers cleanup after each TestMethod and switch to controlled by tests itself.
     * But anyway all drivers will be closed forcibly as only suite is finished or aborted 
     */
    public static void disableDriversCleanup() {
        automaticDriversCleanup = false;
    }

    // TODO: remove this private method
    private String getDeviceName() {
        String deviceName = "Desktop";

        if (!IDriverPool.getDefaultDevice().isNull()) {
            // Samsung - Android 4.4.2; iPhone - iOS 7
            Device device = IDriverPool.getDefaultDevice();
            String deviceTemplate = "%s - %s %s";
            deviceName = String.format(deviceTemplate, device.getName(), device.getOs(), device.getOsVersion());
        }

        return deviceName;
    }

    protected String getBrowser() {
        return Configuration.getBrowser();
    }

    protected String getTitle(XmlSuite suite) {
        String browser = getBrowser();
        if (!browser.isEmpty()) {
            browser = " " + browser; // insert the space before
        }
        String device = getDeviceName();

        String env = !Configuration.isNull(Parameter.ENV) ? Configuration.get(Parameter.ENV)
                : Configuration.get(Parameter.URL);

        String title = "";
        String app_version = "";

        if (!Configuration.get(Parameter.APP_VERSION).isEmpty()) {
            // if nothing is specified then title will contain nothing
            app_version = Configuration.get(Parameter.APP_VERSION) + " - ";
        }

        String suiteName = getSuiteName(suite);
        String xmlFile = getSuiteFileName(suite);

        title = String.format(SUITE_TITLE, app_version, suiteName, String.format(XML_SUITE_NAME, xmlFile), env, device,
                browser);

        return title;
    }

    private String getSuiteFileName(XmlSuite suite) {
        // TODO: investigate why we need such method and suite file name at all
        String fileName = suite.getFileName();
        if (fileName == null) {
            fileName = "undefined";
        }
        LOGGER.debug("Full suite file name: " + fileName);
        if (fileName.contains("\\")) {
            fileName = fileName.replaceAll("\\\\", "/");
        }
        fileName = StringUtils.substringAfterLast(fileName, "/");
        LOGGER.debug("Short suite file name: " + fileName);
        return fileName;
    }

    protected String getSuiteName(XmlSuite suite) {

        String suiteName = "";

        if (suite != null && !"Default suite".equals(suite.getName())) {
            suiteName = Configuration.get(Parameter.SUITE_NAME).isEmpty() ? suite.getName()
                    : Configuration.get(Parameter.SUITE_NAME);
        } else {
            suiteName = Configuration.get(Parameter.SUITE_NAME).isEmpty() ? R.EMAIL.get("title")
                    : Configuration.get(Parameter.SUITE_NAME);
        }

        return suiteName;
    }

    private void printExecutionSummary(List<TestResultItem> tris) {
        Messager.INROMATION.info("**************** Test execution summary ****************");
        int num = 1;
        for (TestResultItem tri : tris) {
            String failReason = tri.getFailReason();
            if (failReason == null) {
                failReason = "";
            }

            if (!tri.isConfig() && !failReason.contains(SpecialKeywords.ALREADY_PASSED)
                    && !failReason.contains(SpecialKeywords.SKIP_EXECUTION)) {
                String reportLinks = !StringUtils.isEmpty(tri.getLinkToScreenshots())
                        ? "screenshots=" + tri.getLinkToScreenshots() + " | " : "";
                reportLinks += !StringUtils.isEmpty(tri.getLinkToLog()) ? "log=" + tri.getLinkToLog() : "";
                Messager.TEST_RESULT.info(String.valueOf(num++), tri.getTest(), tri.getResult().toString(),
                        reportLinks);
            }
        }
    }

    /**
     * Redefine Jira tickets from test.
     *
     * @param tickets
     *            to set
     */
    @Deprecated
    protected void setJiraTicket(String... tickets) {
        List<String> jiraTickets = new ArrayList<String>();
        for (String ticket : tickets) {
            jiraTickets.add(ticket);
        }
        Jira.setTickets(jiraTickets);
    }

    protected void putS3Artifact(String key, String path) {
        AmazonS3Manager.getInstance().put(Configuration.get(Parameter.S3_BUCKET_NAME), key, path);
    }

    protected S3Object getS3Artifact(String bucket, String key) {
        return AmazonS3Manager.getInstance().get(Configuration.get(Parameter.S3_BUCKET_NAME), key);
    }

    protected S3Object getS3Artifact(String key) {
        return getS3Artifact(Configuration.get(Parameter.S3_BUCKET_NAME), key);
    }

    private static void updateAppPath() {

        try {
            if (!Configuration.get(Parameter.ACCESS_KEY_ID).isEmpty()) {
                updateS3AppPath();
            }
        } catch (Exception e) {
            LOGGER.error("AWS S3 manager exception detected!", e);
        }

        try {
            if (!Configuration.get(Parameter.APPCENTER_TOKEN).isEmpty()) {
                updateAppCenterAppPath();
            }
        } catch (Exception e) {
            LOGGER.error("AppCenter manager exception detected!", e);
        }

    }

    /**
     * Method to update MOBILE_APP path in case if apk is located in Hockey App.
     */
    private static void updateAppCenterAppPath() {
        // appcenter://appName/platformName/buildType/version
        Pattern APPCENTER_PATTERN = Pattern.compile(
                "appcenter:\\/\\/([a-zA-Z-0-9][^\\/]*)\\/([a-zA-Z-0-9][^\\/]*)\\/([a-zA-Z-0-9][^\\/]*)\\/([a-zA-Z-0-9][^\\/]*)");
        String mobileAppPath = Configuration.getMobileApp();
        Matcher matcher = APPCENTER_PATTERN.matcher(mobileAppPath);

        LOGGER.info("Analyzing if mobile_app is located on AppCenter...");
        if (matcher.find()) {
            LOGGER.info("app artifact is located on AppCenter...");
            String appName = matcher.group(1);
            String platformName = matcher.group(2);
            String buildType = matcher.group(3);
            String version = matcher.group(4);

            String appCenterAppLocalStorage = Configuration.get(Parameter.APPCENTER_LOCAL_STORAGE);
            // download file from AppCenter to local storage

            File file = AppCenterManager.getInstance().getBuild(appCenterAppLocalStorage, appName, platformName, buildType,
                    version);

            Configuration.setMobileApp(file.getAbsolutePath());

            LOGGER.info("Updated mobile app: " + Configuration.getMobileApp());

            // try to redefine app_version if it's value is latest or empty
            String appVersion = Configuration.get(Parameter.APP_VERSION);
            if (appVersion.equals("latest") || appVersion.isEmpty()) {
                R.CONFIG.put(Parameter.APP_VERSION.getKey(), file.getName());
            }
        }

    }

    /**
     * Method to update MOBILE_APP path in case if apk is located in s3 bucket.
     */
    private static void updateS3AppPath() {
        Pattern S3_BUCKET_PATTERN = Pattern.compile("s3:\\/\\/([a-zA-Z-0-9][^\\/]*)\\/(.*)");
        // get app path to be sure that we need(do not need) to download app
        // from s3 bucket
        String mobileAppPath = Configuration.getMobileApp();
        Matcher matcher = S3_BUCKET_PATTERN.matcher(mobileAppPath);

        LOGGER.info("Analyzing if mobile app is located on S3...");
        if (matcher.find()) {
            LOGGER.info("app artifact is located on s3...");
            String bucketName = matcher.group(1);
            String key = matcher.group(2);
            Pattern pattern = Pattern.compile(key);

            // analyze if we have any pattern inside mobile_app to make extra
            // search in AWS
            int position = key.indexOf(".*");
            if (position > 0) {
                // /android/develop/dfgdfg.*/Mapmyrun.apk
                int slashPosition = key.substring(0, position).lastIndexOf("/");
                if (slashPosition > 0) {
                    key = key.substring(0, slashPosition);
                    S3ObjectSummary lastBuild = AmazonS3Manager.getInstance().getLatestBuildArtifact(bucketName, key,
                            pattern);
                    key = lastBuild.getKey();
                }

            }

            S3Object objBuild = AmazonS3Manager.getInstance().get(bucketName, key);

            String s3LocalStorage = Configuration.get(Parameter.S3_LOCAL_STORAGE);

            // download file from AWS to local storage

            String fileName = s3LocalStorage + "/" + StringUtils.substringAfterLast(objBuild.getKey(), "/");
            File file = new File(fileName);

            // verify maybe requested artifact with the same size was already
            // download
            if (file.exists() && file.length() == objBuild.getObjectMetadata().getContentLength()) {
                LOGGER.info("build artifact with the same size already downloaded: " + file.getAbsolutePath());
            } else {
                LOGGER.info(String.format("Following data was extracted: bucket: %s, key: %s, local file: %s",
                        bucketName, key, file.getAbsolutePath()));
                AmazonS3Manager.getInstance().download(bucketName, key, new File(fileName));
            }

            Configuration.setMobileApp(file.getAbsolutePath());

            // try to redefine app_version if it's value is latest or empty
            String appVersion = Configuration.get(Parameter.APP_VERSION);
            if (appVersion.equals("latest") || appVersion.isEmpty()) {
                R.CONFIG.put(Parameter.APP_VERSION.getKey(), file.getName());
            }

        }
    }

    protected void skipExecution(String message) {
        throw new SkipException(SpecialKeywords.SKIP_EXECUTION + ": " + message);
    }

    protected void onHealthCheck(ISuite suite) {
        String healthCheckClass = Configuration.get(Parameter.HEALTH_CHECK_CLASS);
        if (suite.getParameter(Parameter.HEALTH_CHECK_CLASS.getKey()) != null) {
            // redefine by suite arguments as they have higher priority
            healthCheckClass = suite.getParameter(Parameter.HEALTH_CHECK_CLASS.getKey());
        }

        String healthCheckMethods = Configuration.get(Parameter.HEALTH_CHECK_METHODS);
        if (suite.getParameter(Parameter.HEALTH_CHECK_METHODS.getKey()) != null) {
            // redefine by suite arguments as they have higher priority
            healthCheckMethods = suite.getParameter(Parameter.HEALTH_CHECK_METHODS.getKey());
        }

        String[] healthCheckMethodsArray = null;
        if (!healthCheckMethods.isEmpty()) {
            healthCheckMethodsArray = healthCheckMethods.split(",");
        }
        checkHealth(suite, healthCheckClass, healthCheckMethodsArray);
    }

    private void checkHealth(ISuite suite, String className, String[] methods) {

        if (className.isEmpty()) {
            return;
        }

        // create runtime XML suite for health check
        XmlSuite xmlSuite = new XmlSuite();
        xmlSuite.setName("HealthCheck XmlSuite - " + className);

        XmlTest xmlTest = new XmlTest(xmlSuite);
        xmlTest.setName("HealthCheck TestCase");
        XmlClass xmlHealthCheckClass = new XmlClass();
        xmlHealthCheckClass.setName(className);

        // TestNG do not execute missed methods so we have to calulate expected
        // methods count to handle potential mistakes in methods naming
        int expectedMethodsCount = -1;
        if (methods != null) {
            // declare particular methods if they are provided
            List<XmlInclude> methodsToRun = constructIncludes(methods);
            expectedMethodsCount = methodsToRun.size();
            xmlHealthCheckClass.setIncludedMethods(methodsToRun);
        }

        xmlTest.setXmlClasses(Arrays.asList(new XmlClass[] { xmlHealthCheckClass }));
        xmlSuite.setTests(Arrays.asList(new XmlTest[] { xmlTest }));

        LOGGER.info("HealthCheck '" + className + "' is started.");
        LOGGER.debug("HealthCheck suite content:" + xmlSuite.toXml());

        // Second TestNG process to run HealthCheck
        TestNG testng = new TestNG();
        testng.setXmlSuites(Arrays.asList(xmlSuite));

        TestListenerAdapter tla = new TestListenerAdapter();
        testng.addListener(tla);

        testng.run();
        synchronized (this) {
            boolean passed = false;
            if (expectedMethodsCount == -1) {
                if (tla.getPassedTests().size() > 0 && tla.getFailedTests().size() == 0
                        && tla.getSkippedTests().size() == 0) {
                    passed = true;
                }
            } else {
                LOGGER.info("Expected passed tests count: " + expectedMethodsCount);
                if (tla.getPassedTests().size() == expectedMethodsCount && tla.getFailedTests().size() == 0
                        && tla.getSkippedTests().size() == 0) {
                    passed = true;
                }
            }
            if (passed) {
                LOGGER.info("HealthCheck suite '" + className + "' is finished successfully.");
            } else {
                throw new SkipException("Skip test(s) due to health check failures for '" + className + "'");
            }
        }
    }

    private List<XmlInclude> constructIncludes(String... methodNames) {
        List<XmlInclude> includes = new ArrayList<XmlInclude>();
        for (String eachMethod : methodNames) {
            includes.add(new XmlInclude(eachMethod));
        }
        return includes;
    }
    
    private String takeScreenshot(ITestResult result, String msg) {
        String screenId = "";

        ConcurrentHashMap<String, CarinaDriver> drivers = getDrivers();

        try {
            for (Map.Entry<String, CarinaDriver> entry : drivers.entrySet()) {
                String driverName = entry.getKey();
                WebDriver drv = entry.getValue().getDriver();
    
                if (drv instanceof EventFiringWebDriver) {
                    drv = ((EventFiringWebDriver) drv).getWrappedDriver();
                }
                
                screenId = Screenshot.captureFailure(drv, driverName + ": " + msg); // in case of failure
            }
        } catch (Throwable thr) {
            LOGGER.error("Failure detected on screenshot generation after failure: ", thr);
        }
        return screenId;
    }

    public static class ShutdownHook extends Thread {

        private static final Logger LOGGER = Logger.getLogger(ShutdownHook.class);

        private void generateMetadata() {
            Map<String, ElementsInfo> allData = MetadataCollector.getAllCollectedData();
            if (allData.size() > 0) {
                LOGGER.debug("Generating collected metadada start...");
            }
            for (String key : allData.keySet()) {
                LOGGER.debug("Creating... medata for '" + key + "' object...");
                File file = new File(
                        ReportContext.getArtifactsFolder().getAbsolutePath() + "/metadata/" + key.hashCode() + ".json");
                PrintWriter out = null;
                try {
                    out = new PrintWriter(file);
                    out.append(JsonUtils.toJson(MetadataCollector.getAllCollectedData().get(key)));
                    out.flush();
                } catch (FileNotFoundException e) {
                    LOGGER.error("Unable to write metadata to json file: " + file.getAbsolutePath(), e);
                } finally {
                    if (out != null) {
                        out.close();
                    }
                }
                LOGGER.debug("Created medata for '" + key + "' object...");
            }

            if (allData.size() > 0) {
                LOGGER.debug("Generating collected metadada finish...");
            }
        }

        private void quitAllDriversOnHook() {
            // #810 add zafira testrun abort as part of shutdown hook
            if (ZafiraSingleton.INSTANCE.isRunning()) {
                LOGGER.debug("Zafira test run is still in progress. trying to abort...");
                try {
                    Optional<TestRunType> testRun = ZafiraEventRegistrar.getTestRun();
                    if (testRun != null) {
                        LOGGER.debug("detected testrun id to abort: " + testRun.get().getId());
                        ZafiraSingleton.INSTANCE.getClient().abortTestRun(testRun.get().getId());
                        LOGGER.debug("aborted testrun");
                    }
                } catch (NoSuchElementException e) {
                    LOGGER.debug("No Zafira testrun detected.");
                }
            }

            // as it is shutdown hook just try to quit all existing drivers one by one
            for (CarinaDriver carinaDriver : driversPool) {
                // it is expected that all drivers are killed in appropriate AfterMethod/Class/Suite blocks
                String name = carinaDriver.getName();
                LOGGER.warn("Trying to quit driver '" + name + "' on shutdown hook action!");
                carinaDriver.getDevice().disconnectRemote();
                ProxyPool.stopProxy();
                try {
                    LOGGER.debug("Driver exiting..." + name);
                    carinaDriver.getDriver().quit();
                    LOGGER.debug("Driver exited..." + name);
                } catch (Exception e) {
                    // do nothing
                }
            }
        }

        @Override
        public void run() {
            LOGGER.debug("Running shutdown hook");
            quitAllDriversOnHook();
            generateMetadata();
        }

    }

}