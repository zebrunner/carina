/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
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
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.zebrunner.carina.utils.report.TestResult;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.decorators.Decorated;
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
import org.testng.xml.XmlSuite;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.azure.storage.blob.models.BlobProperties;
import com.qaprosoft.amazon.AmazonS3Manager;
import com.qaprosoft.appcenter.AppCenterManager;
import com.qaprosoft.azure.AzureManager;
import com.qaprosoft.carina.browserupproxy.ProxyPool;
import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.utils.report.ReportContext;
import com.zebrunner.carina.utils.report.TestResultItem;
import com.zebrunner.carina.utils.report.TestResultType;
import com.qaprosoft.carina.core.foundation.report.email.EmailReportGenerator;
import com.qaprosoft.carina.core.foundation.report.email.EmailReportItemCollector;
import com.qaprosoft.carina.core.foundation.report.qtest.IQTestManager;
import com.qaprosoft.carina.core.foundation.report.testrail.ITestRailManager;
import com.qaprosoft.carina.core.foundation.skip.ExpectedSkipManager;
import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.Configuration.Parameter;
import com.zebrunner.carina.utils.DateUtils;
import com.zebrunner.carina.utils.FileManager;
import com.zebrunner.carina.utils.messager.Messager;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.core.testng.ZebrunnerNameResolver;
import com.zebrunner.carina.core.registrar.ownership.Ownership;
import com.zebrunner.carina.core.registrar.ownership.SuiteOwnerResolver;
import com.zebrunner.carina.utils.resources.L10N;
import com.zebrunner.carina.core.registrar.tag.PriorityManager;
import com.zebrunner.carina.core.registrar.tag.TagManager;
import com.qaprosoft.carina.core.foundation.webdriver.CarinaDriver;
import com.qaprosoft.carina.core.foundation.webdriver.Screenshot;
import com.qaprosoft.carina.core.foundation.webdriver.TestPhase;
import com.qaprosoft.carina.core.foundation.webdriver.TestPhase.Phase;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.CapabilitiesLoader;
import com.qaprosoft.carina.core.foundation.webdriver.screenshot.AutoScreenshotRule;
import com.qaprosoft.carina.core.foundation.webdriver.screenshot.IScreenshotRule;
import com.zebrunner.agent.core.registrar.Artifact;
import com.zebrunner.agent.core.registrar.CurrentTest;
import com.zebrunner.agent.core.registrar.CurrentTestRun;
import com.zebrunner.agent.core.registrar.Label;
import com.zebrunner.agent.core.registrar.TestRail;
import com.zebrunner.agent.core.registrar.label.CompositeLabelResolver;
import com.zebrunner.agent.core.registrar.maintainer.ChainedMaintainerResolver;
import com.zebrunner.agent.core.webdriver.RemoteWebDriverFactory;
import com.zebrunner.agent.testng.core.testname.TestNameResolverRegistry;

/*
 * CarinaListener - base carina-core TestNG Listener.
 *
 * @author Vadim Delendik
 */
public class CarinaListener extends AbstractTestListener implements ISuiteListener, IQTestManager, ITestRailManager, IClassListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected static final long EXPLICIT_TIMEOUT = Configuration.getLong(Parameter.EXPLICIT_TIMEOUT);

    protected static final String SUITE_TITLE = "%s%s%s - %s (%s)";
    protected static final String XML_SUITE_NAME = " (%s)";

    protected static boolean automaticDriversCleanup = true;
    
    protected boolean isRunLabelsRegistered = false;
    
    
    private static final Pattern S3_BUCKET_PATTERN = Pattern.compile("s3:\\/\\/([a-zA-Z-0-9][^\\/]*)\\/(.*)");
    private static final Pattern AZURE_CONTAINER_PATTERN = Pattern.compile("\\/\\/([a-z0-9]{3,24})\\.blob.core.windows.net\\/(?:(\\$root|(?:[a-z0-9](?!.*--)[a-z0-9-]{1,61}[a-z0-9]))\\/)?(.{1,1024})");
    // appcenter://appName/platformName/buildType/version
    private static final Pattern APPCENTER_PATTERN = Pattern.compile(
            "appcenter:\\/\\/([a-zA-Z-0-9][^\\/]*)\\/([a-zA-Z-0-9][^\\/]*)\\/([a-zA-Z-0-9][^\\/]*)\\/([a-zA-Z-0-9][^\\/]*)");

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
        registerDecryptAgentProperties();
        
        LOGGER.info(Configuration.asString());
        // Configuration.validateConfiguration();

        try {
            L10N.load();
        } catch (Exception e) {
            LOGGER.error("L10N bundle is not initialized successfully!", e);
        }

        // declare global capabilities in configuration if custom_capabilities is declared
        String customCapabilities = Configuration.get(Parameter.CUSTOM_CAPABILITIES);
        if (!customCapabilities.isEmpty()) {
            // redefine core CONFIG properties using global custom capabilities file
            new CapabilitiesLoader().loadCapabilities(customCapabilities);
        }

        // declare global capabilities from Zebrunner Launcher if any
        Capabilities zebrunnerCapabilities = RemoteWebDriverFactory.getCapabilities();
        if (!zebrunnerCapabilities.asMap().isEmpty()) {
            // redefine core CONFIG properties using caps from Zebrunner launchers
            new CapabilitiesLoader().loadCapabilities(zebrunnerCapabilities);
        }

        IScreenshotRule autoScreenshotsRule = (IScreenshotRule) new AutoScreenshotRule();
        Screenshot.addScreenshotRule(autoScreenshotsRule);

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

        if (!"INFO".equalsIgnoreCase(Configuration.get(Parameter.CORE_LOG_LEVEL))) {
            LoggerContext ctx = (LoggerContext) LogManager.getContext(this.getClass().getClassLoader(), false);
            org.apache.logging.log4j.core.config.Configuration config = ctx.getConfiguration();
            // make sure to update after moving to "com.zebrunner"
            LoggerConfig logger = config.getLoggerConfig("com.qaprosoft.carina.core");
            logger.setLevel(Level.getLevel(Configuration.get(Parameter.CORE_LOG_LEVEL)));
        }

        updateAppPath();
        
        setThreadCount(suite);

        if (Configuration.getPlatform().equalsIgnoreCase(SpecialKeywords.API)) {
            CurrentTestRun.setPlatform(SpecialKeywords.API);
        }

        String mobileApp = Configuration.getMobileApp();
        if (!mobileApp.isEmpty()) {
            // [VD] do not move into the static block as Zebrunner reporting need registered test run!
            Artifact.attachReferenceToTestRun("app", mobileApp);
        }

        CurrentTestRun.setLocale(Configuration.get(Parameter.LOCALE));

        // register app_version/build as artifact if available...
        Configuration.setBuild(Configuration.get(Parameter.APP_VERSION));
        
        String sha1 = Configuration.get(Parameter.GIT_HASH);
        if (!sha1.isEmpty()) {
            Label.attachToTestRun("sha1", sha1);
        }
        
        /*
         * To support multi-suite declaration as below we have to init test run labels at once only!
         * <suite-files>
         *  <suite-file path="suite1.xml"/>
         *  <suite-file path="suite2.xml"/>
         * </suite-files>
         */
        
        if (!this.isRunLabelsRegistered) {
            attachTestRunLabels(suite);
            this.isRunLabelsRegistered = true;
        }

        LOGGER.info("CARINA_CORE_VERSION: " + getCarinaVersion());
    }

	@Override
    public void onStart(ITestContext context) {
        LOGGER.debug("CarinaListener->OnTestStart(ITestContext context): " + context.getName());
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

        if(result.getMethod().isBeforeTestConfiguration()){
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

        if (result.getMethod().isAfterTestConfiguration()){
            TestPhase.setActivePhase(Phase.AFTER_TEST);
        }

        if (result.getMethod().isAfterSuiteConfiguration()) {
            TestPhase.setActivePhase(Phase.AFTER_SUITE);
        }
    }

    @Override
    public void onConfigurationFailure(ITestResult result) {
        LOGGER.debug("CarinaListener->onConfigurationFailure");
        super.onConfigurationFailure(result);
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
                LOGGER.debug("dependency detected for " + methodName);
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
                if (!Configuration.getBoolean(Parameter.FORCIBLY_DISABLE_DRIVER_QUIT)) {
                    quitDrivers(Phase.BEFORE_METHOD, Phase.METHOD);
                }
            }

            attachTestLabels(result);
        } catch (Exception e) {
            LOGGER.error("Exception in CarinaListener->onTestFinish!", e);
        }
    }

    @Override
    public void onAfterClass(ITestClass testClass){
        LOGGER.debug("CarinaListener->onAfterClass(ITestClass testClass)");
        quitDrivers(Phase.BEFORE_CLASS);
    }

    @Override
    public void onFinish(ITestContext context) {
        LOGGER.debug("CarinaListener->onFinish(ITestContext context)");
        super.onFinish(context);

        // [SZ] it's still needed to close driver from BeforeClass stage.
        // Otherwise it could be potentially used in other test classes 
//        quitDrivers(Phase.BEFORE_CLASS); already exited in onAfterClass() method
        quitDrivers(Phase.BEFORE_TEST);

        LOGGER.debug("CarinaListener->onFinish(context): " + context.getName());
    }

    @Override
    public void onFinish(ISuite suite) {
        LOGGER.debug("CarinaListener->onFinish(ISuite suite)");
        try {
            String browser = getBrowser();
            // String suiteName = getSuiteName(context);
            String title = getTitle(suite.getXmlSuite());

            TestResult testResult = EmailReportGenerator.getSuiteResult(EmailReportItemCollector.getTestResults());
            String status = testResult.getTestResultType().getName();

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

            // EmailReportItemCollector.getTestResults());

            LOGGER.debug("Generating email report...");

            // Generate emailable html report using regular method
            EmailReportGenerator report = new EmailReportGenerator(title, env, Configuration.get(Parameter.APP_VERSION),
                    browser, DateUtils.now(), EmailReportItemCollector.getTestResults(),
                    EmailReportItemCollector.getCreatedItems());

            String emailContent = report.getEmailBody();
            // Store emailable report under emailable-report.html
            ReportContext.generateHtmlReport(emailContent);

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

    protected String getBrowser() {
        return Configuration.getBrowser();
    }

    protected String getTitle(XmlSuite suite) {
        String browser = getBrowser();
        if (!browser.isEmpty()) {
            browser = " " + browser; // insert the space before
        }
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

        title = String.format(SUITE_TITLE, app_version, suiteName, String.format(XML_SUITE_NAME, xmlFile), env, browser);

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
        Messager.INFORMATION.info("**************** Test execution summary ****************");
        int num = 1;
        for (TestResultItem tri : tris) {
            String failReason = tri.getFailReason();
            if (failReason == null) {
                failReason = "";
            }

            if (!tri.isConfig()) {
                String reportLinks = !StringUtils.isEmpty(tri.getLinkToScreenshots())
                        ? "screenshots=" + tri.getLinkToScreenshots() + " | " : "";
                reportLinks += !StringUtils.isEmpty(tri.getLinkToLog()) ? "log=" + tri.getLinkToLog() : "";
                Messager.TEST_RESULT.info(String.valueOf(num++), tri.getTest(), tri.getResult().toString(),
                        reportLinks);
            }
        }
    }

    @Deprecated
    protected void putS3Artifact(String key, String path) {
        AmazonS3Manager.getInstance().put(Configuration.get(Parameter.S3_BUCKET_NAME), key, path);
    }

    @Deprecated
    protected S3Object getS3Artifact(String bucket, String key) {
        return AmazonS3Manager.getInstance().get(Configuration.get(Parameter.S3_BUCKET_NAME), key);
    }

    @Deprecated
    protected S3Object getS3Artifact(String key) {
        return getS3Artifact(Configuration.get(Parameter.S3_BUCKET_NAME), key);
    }

    @Deprecated
    protected void putAzureArtifact(String remotePath, String localPath) {
        AzureManager.getInstance().put(Configuration.get(Parameter.AZURE_CONTAINER_NAME), remotePath, localPath);
    }

    @Deprecated
    protected void getAzureArtifact(String bucket, String remotePath, File localPath) {
        AzureManager.getInstance().download(bucket, remotePath, localPath);
    }

    private static void updateAppPath() {
        
        String mobileAppPath = Configuration.getMobileApp();
        Matcher matcher = S3_BUCKET_PATTERN.matcher(mobileAppPath);
        LOGGER.debug("Analyzing if mobile app is located on S3...");
        if (matcher.find()) {
            mobileAppPath = updateS3AppPath(mobileAppPath);
        }

        matcher = AZURE_CONTAINER_PATTERN.matcher(mobileAppPath);
        LOGGER.debug("Analyzing if mobile app is located on Azure...");
        if (matcher.find()) {
            mobileAppPath = updateAzureAppPath(mobileAppPath);
        }
        
        matcher = APPCENTER_PATTERN.matcher(mobileAppPath);
        LOGGER.debug("Analyzing if mobile_app is located on AppCenter...");
        if (matcher.find()) {
            mobileAppPath = updateAppCenterAppPath(mobileAppPath);
        }
        
        if (!mobileAppPath.isEmpty()) {
            Configuration.setMobileApp(mobileAppPath);
        }
    }

    /**
     * Method to update MOBILE_APP path in case if apk is located in Hockey App.
     */
    private static String updateAppCenterAppPath(String mobileAppPath) {
        Matcher matcher = APPCENTER_PATTERN.matcher(mobileAppPath);
        if (matcher.find()) {
            LOGGER.info("app artifact is located on AppCenter...");
            String appName = matcher.group(1);
            String platformName = matcher.group(2);
            String buildType = matcher.group(3);
            String version = matcher.group(4);

            //TODO: test if generated appcenter download url is valid
            mobileAppPath = AppCenterManager.getInstance().getDownloadUrl(appName, platformName, buildType,
                    version);

        } else {
            LOGGER.error("Unable to parse '{}' path using AppCenter pattern", mobileAppPath);
        }
        return mobileAppPath;
    }

    /**
     * Method to update MOBILE_APP path in case if apk is located in s3 bucket.
     */
    private static String updateS3AppPath(String mobileAppPath) {
        // get app path to be sure that we need(do not need) to download app
        // from s3 bucket
        Matcher matcher = S3_BUCKET_PATTERN.matcher(mobileAppPath);
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

            } else {
                key = AmazonS3Manager.getInstance().get(bucketName, key).getKey();
            }
            LOGGER.info("next s3 app key will be used: " + key);

            // generate presign url explicitly to register link as run artifact
            long hours = 72L*1000*60*60; // generate presigned url for nearest 3 days
            mobileAppPath = AmazonS3Manager.getInstance().generatePreSignUrl(bucketName, key, hours).toString();
        } else {
            LOGGER.error("Unable to parse '{}' path using S3 pattern", mobileAppPath);
        }
        
        return mobileAppPath;
    }

    /**
     * Method to update MOBILE_APP path in case if apk is located in Azure storage.
     */
    private static String updateAzureAppPath(String mobileAppPath) {
        Matcher matcher = AZURE_CONTAINER_PATTERN.matcher(mobileAppPath);
        if (matcher.find()) {
            LOGGER.info("app artifact is located on Azure...");
            String accountName = matcher.group(1);
            String containerName = matcher.group(2) == null ? "$root" : matcher.group(2);
            String remoteFilePath = matcher.group(3);

            LOGGER.info(
                    "Account: " + accountName + "\n" +
                    "Container: " + containerName + "\n" +
                    "RemotePath: " + remoteFilePath + "\n"
            );

            R.CONFIG.put(Parameter.AZURE_ACCOUNT_NAME.getKey(), accountName);

            BlobProperties blobProperties = AzureManager.getInstance().get(containerName, remoteFilePath);
            String azureLocalStorage = Configuration.get(Parameter.AZURE_LOCAL_STORAGE);
            String localFilePath = azureLocalStorage + File.separator + StringUtils.substringAfterLast(remoteFilePath, "/");

            File file = new File(localFilePath);

            try {
                // verify requested artifact by checking the checksum
                if (file.exists() && FileManager.getFileChecksum(FileManager.Checksum.MD5, file).equals(Base64.encodeBase64String(blobProperties.getContentMd5()))) {
                    LOGGER.info("build artifact with the same checksum already downloaded: " + file.getAbsolutePath());
                } else {
                    LOGGER.info(
                            String.format("Following data was extracted: container: %s, remotePath: %s, local file: %s",
                            containerName, remoteFilePath, file.getAbsolutePath())
                    );
                    AzureManager.getInstance().download(containerName, remoteFilePath, file);
                }

            } catch (Exception exception) {
                LOGGER.error("Azure app path update exception detected!", exception);
            }

            mobileAppPath = file.getAbsolutePath();

            // try to redefine app_version if it's value is latest or empty
            String appVersion = Configuration.get(Parameter.APP_VERSION);
            if (appVersion.equals("latest") || appVersion.isEmpty()) {
                Configuration.setBuild(file.getName());
            }
        } else {
            LOGGER.error("Unable to parse '{}' path using Azure pattern", mobileAppPath);
        }
        
        return mobileAppPath;
    }

    protected void skipExecution(String message) {
        CurrentTest.revertRegistration();
        throw new SkipException(message);
    }

    /*
     * Parse TestNG <suite ...> tag and return any attribute
     * @param ISuite suite
     * @param IString attribute
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
            LOGGER.warn("Unable to get attribute '" + attribute +"' from suite: " + suite.getXmlSuite().getFileName(), e);
        }

        return res;

    }
    private void setThreadCount(ISuite suite) {
        //Reuse default thread-count value from suite TestNG file if it is not overridden in _config.properties

        /*
         * WARNING! We coudn't override default thread-count="5" and data-provider-thread-count="10"!
         * suite.getXmlSuite().toXml() add those default values anyway even if the absent in suite xml file declaraton.
         * To make possible to parse correctly we had to reuse external parser and private getAttributeValue
        */
        
        if (Configuration.getThreadCount()>= 1) {
            // use thread-count from config.properties
            suite.getXmlSuite().setThreadCount(Configuration.getThreadCount());
            LOGGER.debug("Updated thread-count=" + suite.getXmlSuite().getThreadCount());
        } else {
            String suiteThreadCount = getAttributeValue(suite, "thread-count");
            LOGGER.debug("thread-count from suite: " + suiteThreadCount);
            if (suiteThreadCount.isEmpty()) {
                LOGGER.info("Set thread-count=1");
                R.CONFIG.put(Parameter.THREAD_COUNT.getKey(), "1");
                suite.getXmlSuite().setThreadCount(1);
            } else {
                // reuse value from suite xml file
                LOGGER.debug("Synching thread-count with values from suite xml file...");
                R.CONFIG.put(Parameter.THREAD_COUNT.getKey(), suiteThreadCount);
                LOGGER.info("Use thread-count='" + suite.getXmlSuite().getThreadCount() + "' from suite file.");
            }
        }

        if (Configuration.getDataProviderThreadCount() >= 1) {
            // use thread-count from config.properties
            suite.getXmlSuite().setDataProviderThreadCount(Configuration.getDataProviderThreadCount());
            LOGGER.debug("Updated data-provider-thread-count=" + suite.getXmlSuite().getDataProviderThreadCount());
        } else {
            String suiteDataProviderThreadCount = getAttributeValue(suite, "data-provider-thread-count");
            LOGGER.debug("data-provider-thread-count from suite: " + suiteDataProviderThreadCount);

            if (suiteDataProviderThreadCount.isEmpty()) {
                LOGGER.info("Set data-provider-thread-count=1");
                R.CONFIG.put(Parameter.DATA_PROVIDER_THREAD_COUNT.getKey(), "1");
                suite.getXmlSuite().setDataProviderThreadCount(1);
            } else {
                // reuse value from suite xml file
                LOGGER.debug("Synching data-provider-thread-count with values from suite xml file...");
                R.CONFIG.put(Parameter.DATA_PROVIDER_THREAD_COUNT.getKey(), suiteDataProviderThreadCount);
                LOGGER.info("Use data-provider-thread-count='" + suite.getXmlSuite().getDataProviderThreadCount() + "' from suite file.");
            }
        }
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
        if (qtestCases.size() > 0) {
            Label.attachToTest(SpecialKeywords.QTEST_TESTCASE_UUID, Arrays.copyOf(qtestCases.toArray(), qtestCases.size(), String[].class));
        }
    }

    private void attachTestRunLabels(ISuite suite) {
        String trSuite = getTestRailSuiteId(suite);

        if (!trSuite.isEmpty()) {
            TestRail.setSuiteId(trSuite);
        }
        
        // read command line argument to improve test rail integration capabilities.
        if (!Configuration.getBoolean(Parameter.TESTRAIL_ENABLED)) {
            LOGGER.debug("disable TestRail integration!");
            TestRail.disableSync();
        }
        
        if (Configuration.getBoolean(Parameter.INCLUDE_ALL)) {
            LOGGER.info("enable include_all for TestRail integration!");
            TestRail.includeAllTestCasesInNewRun();
        }
        
        String milestone = Configuration.get(Parameter.MILESTONE);
        if (!milestone.isEmpty()) {
            LOGGER.info("Set TestRail milestone name: " + milestone);
            TestRail.setMilestone(milestone);
        }
        
        String runName = Configuration.get(Parameter.RUN_NAME);
        if (!runName.isEmpty()) {
            LOGGER.info("Set TestRail run name: " + runName);
            TestRail.setRunName(runName);
        }
        
        String assignee = Configuration.get(Parameter.ASSIGNEE);
        if (!assignee.isEmpty()) {
            LOGGER.info("Set TestRail assignee: " + assignee);
            TestRail.setAssignee(assignee);
        }

        String qtestProject = getQTestProjectId(suite);
        if (!qtestProject.isEmpty()){
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
        ConcurrentHashMap<String, CarinaDriver> drivers = getDrivers();

        try {
            for (Map.Entry<String, CarinaDriver> entry : drivers.entrySet()) {
                WebDriver drv = entry.getValue().getDriver();

                if (drv instanceof Decorated<?>) {
                    drv = (WebDriver) ((Decorated<?>) drv).getOriginal();
                }

                R.CONFIG.put(Parameter.ERROR_SCREENSHOT.getKey(), "true", true);
                Screenshot.captureByRule(drv, "", true);
            }
        } catch (Throwable thr) {
            LOGGER.error("Failure detected on screenshot generation after failure: ", thr);
        } finally {
            R.CONFIG.put(Parameter.ERROR_SCREENSHOT.getKey(), "false", true);
        }
    }    

    public static class ShutdownHook extends Thread {

        private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownHook.class);

        private void quitAllDriversOnHook() {
            // as it is shutdown hook just try to quit all existing drivers one by one
            for (CarinaDriver carinaDriver : driversPool) {
                // it is expected that all drivers are killed in appropriate AfterMethod/Class/Suite blocks
                String name = carinaDriver.getName();
                LOGGER.warn("Trying to quit driver '" + name + "' on shutdown hook action!");
                carinaDriver.getDevice().disconnectRemote();
                ProxyPool.stopProxy();
                try {
                    LOGGER.debug("Driver closing..." + name);
                    carinaDriver.getDriver().close();
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
            if (!Configuration.getBoolean(Parameter.FORCIBLY_DISABLE_DRIVER_QUIT)) {
                quitAllDriversOnHook();
            }
        }

    }

    /**
     * Register agent properties from agent.properties file (if exists) as system properties.
     * Yaml configuration will be ignored.
     * If system property already have property(ies), we will no rewrite it
     */
    private void registerDecryptAgentProperties() {
        if (ClassLoader.getSystemResource("agent.properties") == null) {
            return;
        }

        if (ClassLoader.getSystemResource("agent.yaml") != null ||
                ClassLoader.getSystemResource("agent.yml") != null) {
            // use sout instead of logger because agent intercept call of logger
            System.out.println(
                    "[WARN] You have agent.properties and agent.yaml/agent.yml! Use only one type of config file for agent.\n"
                            + "Yaml files does not supported by Carina Framework. All properties in your agent.properties file will have"
                            + " more priority over yaml agent configuration."
                            + "If you want to support cryptography for agent, use agent.properties.");
        }

        Properties properties = R.AGENT.getProperties();
        Set<String> propertyNames = properties.stringPropertyNames();
        for (String name : propertyNames) {
            String value = R.AGENT.getDecrypted(name);
            String systemPropertyName = convertPropertyToSystemProperty(name);
            String systemValue = System.getProperty(systemPropertyName);
            if (systemValue == null) {
                System.setProperty(systemPropertyName, value);
            }
        }
    }

    /**
     * This method is a hotfix for naming difference between agent.properties and system properties
     */
    private String convertPropertyToSystemProperty(String propertyName) {
        String systemProperty = propertyName;
        if ("reporting.project-key".equals(propertyName)) {
            systemProperty = "reporting.projectKey";
        }
        if ("reporting.server.access-token".equals(propertyName)) {
            systemProperty = "reporting.server.accessToken";
        }

        if ("reporting.run.display-name".equals(propertyName)) {
            systemProperty = "reporting.run.displayName";
        }

        if ("reporting.run.retry-known-issues".equals(propertyName)) {
            systemProperty = "reporting.run.retryKnownIssues";
        }

        if ("reporting.run.substitute-remote-web-drivers".equals(propertyName)) {
            systemProperty = "reporting.run.substituteRemoteWebDrivers";
        }
        return systemProperty;
    }
}
