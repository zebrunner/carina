/*
 * Copyright 2013 QAPROSOFT (http://qaprosoft.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qaprosoft.carina.core.foundation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.xml.XmlTest;

import com.jayway.restassured.RestAssured;
import com.qaprosoft.carina.core.foundation.api.APIMethodBuilder;
import com.qaprosoft.carina.core.foundation.jenkins.JenkinsClient;
import com.qaprosoft.carina.core.foundation.jira.Jira;
import com.qaprosoft.carina.core.foundation.log.GlobalTestLog;
import com.qaprosoft.carina.core.foundation.log.GlobalTestLog.Type;
import com.qaprosoft.carina.core.foundation.log.TestLogHelper;
import com.qaprosoft.carina.core.foundation.report.HtmlReportGenerator;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.report.TestResultItem;
import com.qaprosoft.carina.core.foundation.report.TestResultType;
import com.qaprosoft.carina.core.foundation.report.email.EmailManager;
import com.qaprosoft.carina.core.foundation.report.email.EmailReportGenerator;
import com.qaprosoft.carina.core.foundation.report.email.EmailReportItemCollector;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.DateUtils;
import com.qaprosoft.carina.core.foundation.utils.L18n;
import com.qaprosoft.carina.core.foundation.utils.Messager;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;
import com.qaprosoft.carina.core.foundation.utils.parser.XLSDSBean;
import com.qaprosoft.carina.core.foundation.utils.parser.XLSParser;
import com.qaprosoft.carina.core.foundation.utils.parser.XLSTable;
import com.qaprosoft.carina.core.foundation.webdriver.DriverFactory;
import com.qaprosoft.carina.core.foundation.webdriver.DriverHelper;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import com.qaprosoft.testexecuter.client.ExecutionContext;
import com.qaprosoft.testexecuter.client.ITestExecuterClient;
import com.qaprosoft.testexecuter.client.TestDetailsBean;
import com.qaprosoft.testexecuter.client.TestDetailsBean.TestStatus;
import com.qaprosoft.testexecuter.client.TestExecuterClient;


/*
 * AbstractTest - base test for UI and API tests.
 * 
 * @author Alex Khursevich
 */
public abstract class AbstractTest extends DriverHelper
{
    protected static final Logger LOG = Logger.getLogger(AbstractTest.class);

    protected static final String CLASS_TITLE = "%s: %s - %s (%s)";
    protected static final String XML_TITLE = "%s: %s (%s) - %s (%s)";

	private static ThreadLocal<WebDriver> webDriver = new ThreadLocal<WebDriver>();
	//private static List<WebDriver> createdDrivers = Collections.synchronizedList(new ArrayList<WebDriver>());

	
    // Test-Executer integration items
    private static ExecutionContext executionContext;
    protected TestDetailsBean TEST_EXECUTER_LOG;

    protected APIMethodBuilder apiMethodBuilder;
    private String browserVersion = "";
    private String initializationFailure = "";
    
	

    @BeforeSuite(alwaysRun = true)
    public void executeBeforeSuite(ITestContext context)
    {
		try
		{
		    // Set log4j properties
		    PropertyConfigurator.configure(ClassLoader.getSystemResource("log4j.properties"));
		    // Set SoapUI log4j properties
		    System.setProperty("soapui.log4j.config", "./src/main/resources/soapui-log4j.xml");
	
		    LOG.info(Configuration.asString());
		    Configuration.validateConfiguration();
	
		    ReportContext.removeOldReports();
		    context.getCurrentXmlTest().getSuite().setThreadCount(Configuration.getInt(Parameter.THREAD_COUNT));
	
		    if (!Configuration.isNull(Parameter.URL))
		    {
		    	RestAssured.baseURI = Configuration.get(Parameter.URL);
		    }
	
		    if (Configuration.getBoolean(Parameter.IS_TESTEXECUTER))
		    {
				Assert.assertTrue(Configuration.getLong(Parameter.TEST_ID) > 0, "Parameter 'test_id' not specified");
				ITestExecuterClient client = new TestExecuterClient(Configuration.get(Parameter.TESTEXECUTER_URL));
				executionContext = ExecutionContext.INSTANCE.initBeforeSuite(Configuration.getLong(Parameter.TEST_ID), client);
		    }
		    else
		    {
		    	executionContext = ExecutionContext.INSTANCE.initBeforeSuite();
		    }
	
		    try
		    {
				String lang = Configuration.get(Parameter.LOCALE).split("_")[0];
				String country = Configuration.get(Parameter.LOCALE).split("_")[1];
				L18n.init("l18n.messages", new Locale(lang, country));
		    }
		    catch (Exception e)
		    {
		    	LOG.info("Localization bundle is not initialized, set locale configuration arg as 'lang_country' and create l18n/messages.properties file!");
		    }
		    
		    if (Configuration.getBoolean(Parameter.DRIVER_SINGLE_MODE))
		    {
		    	LOG.info("Driver is initializing in single mode.");
		    	driver = DriverFactory.create(context.getSuite().getName());
	    		setDriver(driver);
	    		//createdDrivers.add(driver);

		    }
		    
		}
		catch (Exception e)
		{
		    LOG.error("Exception in executeBeforeSuite");
		    initializationFailure = e.getMessage();
		    e.printStackTrace();
		}

    }

    @BeforeMethod(alwaysRun = true)
    public void executeBeforeTestMethod(XmlTest xmlTest, Method testMethod, ITestContext context) throws Exception
    {
		try
		{
		    xmlTest.addParameter(SpecialKeywords.TEST_LOG_ID, UUID.randomUUID().toString());

		    if (isUITest())
		    {
		    	if (!Configuration.getBoolean(Parameter.DRIVER_SINGLE_MODE)) {
		    		driver = DriverFactory.create(TestNamingUtil.getCanonicalTestNameBeforeTest(xmlTest, testMethod));
		    		setDriver(driver);
		    	}
				xmlTest.addParameter("sessionId", DriverPool.registerDriverSession(driver));
				initSummary(driver);
				browserVersion = DriverFactory.getBrowserVersion(driver);
		    }
		    TEST_EXECUTER_LOG = executionContext.initBeforeTest(xmlTest.getName());
		    apiMethodBuilder = new APIMethodBuilder();
		}
		catch (Exception e)
		{
		    LOG.error("Exception in executeBeforeTestMethod");
		    initializationFailure = e.getMessage();
			
            StackTraceElement[] elems = e.getStackTrace();
	        for (StackTraceElement elem : elems) {
	        	initializationFailure = initializationFailure + "\n" + elem.toString();
            }
		    
		    e.printStackTrace();
		}
    }

    @AfterMethod(alwaysRun = true)
    public void executeAfterTestMethod(ITestResult result) throws IOException
    {
	try
	{
	    GlobalTestLog glblLog = ((GlobalTestLog) result.getAttribute(GlobalTestLog.KEY));

	    
	    String test = TestNamingUtil.getCanonicalTestName(result);
	    File testLogFile = new File(ReportContext.getTestDir(test) + "/test.log");
	    // File soapuiLogFile = new File(ReportContext.getTestDir(test) +
	    // "/soapui.log");
	    if (!testLogFile.exists()) testLogFile.createNewFile();
	    FileWriter fw = new FileWriter(testLogFile);

	    if (driver == null && !initializationFailure.isEmpty()) {
			fw.append("\r\n************************** Initialization logs **************************\r\n\r\n");
			fw.append(initializationFailure);	    	
	    }
	    if (isUITest() && driver != null)
	    {
			fw.append("\r\n**************************** UI logs ****************************\r\n\r\n");
			
			try
			{
				fw.append(TestLogHelper.getSessionLogs(test));
			}
			catch (Exception e)
			{
			    LOG.error(e.getMessage());
			}

			try
			{
				if (!Configuration.getBoolean(Parameter.DRIVER_SINGLE_MODE)) {
					quitDriver();
					//driver.quit();
				}
			}
			catch (Exception e)
			{
			    LOG.error(e.getMessage());
			}
	    }

	    if (!StringUtils.isEmpty(glblLog.readLog(Type.SOAP)))
	    {
			fw.append("\r\n************************** SoapUI logs **************************\r\n\r\n");
			fw.append(glblLog.readLog(Type.SOAP));
	    }

	    if (apiMethodBuilder != null)
	    {
	    	if (apiMethodBuilder.getTempFile().exists())
	    	{
				String tempLog = FileUtils.readFileToString(apiMethodBuilder.getTempFile());
				if (!StringUtils.isEmpty(glblLog.readLog(Type.REST)) || !StringUtils.isEmpty(tempLog))
				{
				    fw.append("\r\n*********************** Rest-Assured logs ***********************\r\n\r\n");
				    fw.append(tempLog);
				    fw.append(glblLog.readLog(Type.REST));
				}
	    	}
	    }

	    if (!StringUtils.isEmpty(glblLog.readLog(Type.COMMON)))
	    {
			fw.append("\r\n************************** Common logs **************************\r\n\r\n");
			fw.append(glblLog.readLog(Type.COMMON));
	    }

	    try
	    {
			fw.close();
			if (apiMethodBuilder != null)
				apiMethodBuilder.close();
	    }
	    catch (Exception e)
	    {
	    	LOG.error(e.getMessage());
	    }

	    if (Configuration.getBoolean(Parameter.IS_TESTEXECUTER))
	    {
		    TestResultItem testResult = EmailReportItemCollector.pull(result);
			if (TestResultType.PASS.equals(testResult.getResult()))
			{
			    TEST_EXECUTER_LOG.setTestStatus(TestStatus.PASS);
			}
			else
			{
			    TEST_EXECUTER_LOG.setTestStatus(TestStatus.FAIL);
			    TEST_EXECUTER_LOG.setFailure(testResult.getFailReason());
			}
			TEST_EXECUTER_LOG.setScreenshotLink(isUITest() ? testResult.getLinkToScreenshots() : "#");
			TEST_EXECUTER_LOG.setLogLink(testResult.getLinkToLog());
	    }
	}
	catch (Exception e)
	{
	    LOG.error("Exception in executeAfterTestMethod");
	    e.printStackTrace();
	}
    }

    @AfterSuite(alwaysRun = true)
    public void executeAfterSuite(ITestContext context)
    {
		try
		{
			if (Configuration.getBoolean(Parameter.DRIVER_SINGLE_MODE) && isUITest() && driver != null) {
				try
				{
					quitDriver();
					//quitDrivers();
				}
				catch (Exception e)
				{
				    LOG.error(e.getMessage());
				}
			}
		    executionContext.finilizeAfterSuite();
		    HtmlReportGenerator.generate(ReportContext.getBaseDir().getAbsolutePath());
	
		    String env = !Configuration.isNull(Parameter.ENV) ? Configuration.get(Parameter.ENV) : Configuration.get(Parameter.URL);
	
		    String eTitle = null;
		    if (context.getSuite().getXmlSuite() != null && !"Default suite".equals(context.getSuite().getXmlSuite().getName()))
		    {
				String suiteName = Configuration.isNull(Parameter.SUITE_NAME) ? context.getSuite().getXmlSuite().getName() : Configuration
					.get(Parameter.SUITE_NAME);
				String xmlFile = !StringUtils.isEmpty(System.getProperty("suite")) ? System.getProperty("suite") + ".xml" : StringUtils
					.substringAfterLast(context.getSuite().getXmlSuite().getFileName(), "\\");
				eTitle = String.format(XML_TITLE, EmailReportGenerator.getSuiteResult(EmailReportItemCollector.getTestResults()).name(), suiteName,
						xmlFile, env, Configuration.get(Parameter.BROWSER) + " " + browserVersion);			
		    }
		    else
		    {
				String suiteName = Configuration.isNull(Parameter.SUITE_NAME) ? R.EMAIL.get("title") : Configuration.get(Parameter.SUITE_NAME);
				eTitle = String.format(CLASS_TITLE, EmailReportGenerator.getSuiteResult(EmailReportItemCollector.getTestResults()).name(), suiteName,
						env, Configuration.get(Parameter.BROWSER) + " " + browserVersion);			
		    }
		    ReportContext.getTempDir().delete();
	
		    // Update JIRA
		    Jira.updateAfterSuite(context, EmailReportItemCollector.getTestResults());
	
		    // Generate email report
		    EmailReportGenerator report = new EmailReportGenerator(eTitle, env, Configuration.get(Parameter.APP_VERSION),
				    Configuration.get(Parameter.BROWSER) + " " + browserVersion, DateUtils.now(), getCIJobReference(), EmailReportItemCollector.getTestResults(),
				    EmailReportItemCollector.getCreatedItems());	    
	
		    // Send report for specified emails
		    EmailManager.send(eTitle, report.getEmailBody(), Configuration.get(Parameter.EMAIL_LIST), Configuration.get(Parameter.SENDER_EMAIL), 
			    Configuration.get(Parameter.SENDER_PASSWORD));
	
		    printExecutionSummary(EmailReportItemCollector.getTestResults());
		}
		catch (Exception e)
		{
		    LOG.error("Exception in executeAfterSuite");
		    e.printStackTrace();
		}
    }


    public Object[][] createTestArgSets2(ITestContext context, String executeColumn, String executeValue, String... staticArgs)
    {
		XLSDSBean dsBean = new XLSDSBean(context);
		XLSTable dsData = XLSParser.parseSpreadSheet(dsBean.getXlsFile(), dsBean.getXlsSheet(), executeColumn, executeValue);

		Object[][] args = new Object[dsData.getDataRows().size()][staticArgs.length + 1];
		int rowIndex = 0;
		for (Map<String, String> xlsRow : dsData.getDataRows())
		{
			args[rowIndex][0] = xlsRow;

		    for (int i=0; i<staticArgs.length; i++){
		    	args[rowIndex][i + 1] = dsBean.getTestParams().get(staticArgs[i]); //zero element is a hashmap 
		    }
		    rowIndex++;
		}

		return args;
    }

    @DataProvider(name = "excel_ds2")
    public Object[][] readDataFromXLS2(ITestContext context)
    {
	return createTestArgSets2(context, "Execute", "Y");
    }
    
    public Object[][] createTestArgSets(ITestContext context, String executeColumn, String executeValue, String... staticArgs)
    {
	String[] argNames = ArrayUtils.addAll(context.getCurrentXmlTest().getParameter(SpecialKeywords.EXCEL_DS_ARGS).split(";"), staticArgs);
	XLSDSBean dsBean = new XLSDSBean(context);
	XLSTable dsData = XLSParser.parseSpreadSheet(dsBean.getXlsFile(), dsBean.getXlsSheet(), executeColumn, executeValue);
	Object[][] args = new Object[dsData.getDataRows().size()][argNames.length];
	int rowIndex = 0;
	for (Map<String, String> xlsRow : dsData.getDataRows())
	{
	    for (int i = 0; i < argNames.length; i++)
	    {
			if (dsBean.getArgs().contains(argNames[i]))
			{
			    args[rowIndex][i] = xlsRow.get(argNames[i]);
			}
			else
			{
			    args[rowIndex][i] = dsBean.getTestParams().get(argNames[i]);
			}
	    }
	    rowIndex++;
	}
	return args;
    }

    @DataProvider(name = "excel_ds")
    public Object[][] readDataFromXLS(ITestContext context)
    {
    	return createTestArgSets(context, "Execute", "Y");
    }

    private void printExecutionSummary(List<TestResultItem> tris)
    {
		Messager.INROMATION.info("**************** Test execution summary ****************");
		int num = 1;
		for (TestResultItem tri : tris)
		{
		    String reportLinks = !StringUtils.isEmpty(tri.getLinkToScreenshots()) ? "screenshots=" + tri.getLinkToScreenshots() + " | " : "";
		    reportLinks += !StringUtils.isEmpty(tri.getLinkToLog()) ? "log=" + tri.getLinkToLog() : "";
		    Messager.TEST_RESULT.info(String.valueOf(num++), tri.getTest(), tri.getResult().toString(), reportLinks);
		}
    }

    private String getCIJobReference()
    {
		String ciTestJob = null;
		if (!Configuration.isNull(Parameter.JENKINS_URL) && !Configuration.isNull(Parameter.JENKINS_JOB))
		{
		    JenkinsClient jc = new JenkinsClient(Configuration.get(Parameter.JENKINS_URL));
		    ciTestJob = jc.getCurrentJobURL(Configuration.get(Parameter.JENKINS_JOB));
		    if (StringUtils.isEmpty(ciTestJob))
		    {
		    	LOG.info("Could not connect to Jenkins!");
		    }
		}
		else
		{
		    	LOG.info("Specify 'jenkins_url' and 'jenkins_job' in CONFIG to have reference to test job!");
		}
		return ciTestJob;
    }

    protected abstract boolean isUITest();
    
	public static WebDriver getDriver() {
		return webDriver.get();
	}
	 
	static void setDriver(WebDriver driver) {
		webDriver.set(driver);
	}

    public static void closeDriver() {
    	webDriver.get().close();
    	webDriver.remove();
    }
    
    public static void quitDriver() {
    	webDriver.get().quit();
    	webDriver.remove();
    }    
    
    /*
	public static synchronized void quitDrivers() {

		LOGGER.info("Drivers are: " + createdDrivers);
		if (!createdDrivers.isEmpty()) {
			for (WebDriver driverItem : createdDrivers) {
				try {
					driverItem.quit();
				} catch (Exception e) {
					// Do nothing
				}

			}
		}

	}
	*/
    
}
