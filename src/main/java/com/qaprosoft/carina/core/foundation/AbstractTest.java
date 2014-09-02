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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
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
import com.qaprosoft.carina.core.foundation.spira.SpiraTestIntegrator;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.DateUtils;
import com.qaprosoft.carina.core.foundation.utils.L18n;
import com.qaprosoft.carina.core.foundation.utils.Messager;
import com.qaprosoft.carina.core.foundation.utils.ParameterGenerator;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.android.recorder.utils.AdbExecutor;
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
	private Map<String, String> testNameMappedToArgs = Collections.synchronizedMap(new HashMap<String, String>());
	private Map<String, String> jiraTicketsMappedToArgs = Collections.synchronizedMap(new HashMap<String, String>());
    protected static final Logger LOGGER = Logger.getLogger(AbstractTest.class);

    protected static final String CLASS_TITLE = "%s: %s - %s (%s)";
    protected static final String XML_TITLE = "%s: %s (%s) - %s (%s)";

	
    // Test-Executer integration items
    private static ExecutionContext executionContext;
    protected TestDetailsBean TEST_EXECUTER_LOG;
    
    //Jira ticket(s)
    protected List<String> jiraTickets = new ArrayList<String>();

    protected APIMethodBuilder apiMethodBuilder;
    private String browserVersion = "";
    
    private static AdbExecutor executor = new AdbExecutor(Configuration.get(Parameter.ADB_HOST), Configuration.get(Parameter.ADB_PORT));
    private int adb_pid;
    
    @BeforeClass(alwaysRun = true)
    public void executeBeforeClass(ITestContext context) throws Throwable
    {
	    //spira logging
    	SpiraTestIntegrator.logTestCaseInfo(this.getClass().getName());
    }    
    
    @BeforeSuite(alwaysRun = true)
    public void executeBeforeSuite(ITestContext context) throws Throwable
    {
		try
		{
		    // Set log4j properties
		    PropertyConfigurator.configure(ClassLoader.getSystemResource("log4j.properties"));
		    // Set SoapUI log4j properties
		    System.setProperty("soapui.log4j.config", "./src/main/resources/soapui-log4j.xml");
	
		    LOGGER.info(Configuration.asString());
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
		    	LOGGER.info("Localization bundle is not initialized, set locale configuration arg as 'lang_country' and create l18n/messages.properties file!");
		    }
		}
		catch (Throwable thr)
		{
			context.setAttribute(SpecialKeywords.INITIALIZATION_FAILURE, thr);
			throw thr;
		}

    }

    @BeforeMethod(alwaysRun = true)
    public void executeBeforeTestMethod(XmlTest xmlTest, Method testMethod, ITestContext context) throws Throwable
    {
		try
		{
		    String test = TestNamingUtil.getCanonicalTestNameBeforeTest(xmlTest, testMethod);

		    if (isUITest())
		    {
		    	if (getDriver() == null) {
			    	if (Configuration.getBoolean(Parameter.TESTS_DEPENDENT_MODE)) {	
			    		// TODO Implement logic to Skip Test if secondary etc test hasn't driver 
				    	LOGGER.warn("Driver is initializing in scenario tests mode.");
				    	
			    	}
			    	LOGGER.info("-------------------------------------- Driver Factory start ----------------------------------");
/*			    	Object[] arguments = new Object[2];
			    	arguments[0] = new URL(Configuration.get(Parameter.SELENIUM_HOST));
			    	arguments[1] = DriverFactory.getFirefoxCapabilities(test);
					DriverFactory.createObject("org.openqa.selenium.remote.RemoteWebDriver", arguments);*/
			    	driver = DriverFactory.create(test);
		    		setDriver(driver);		    		
			    	LOGGER.info("-------------------------------------- Driver Factory finish ---------------------------------");		    		
		    		
		    		
			    	String sessionId = DriverPool.registerDriverSession(driver);
			    	xmlTest.addParameter(SpecialKeywords.SESSION_ID, sessionId);
					initSummary(driver);
					
			    	//enable recording if needed
					if (Configuration.getBoolean(Parameter.VIDEO_RECORDING)) {
						executor.dropFile(SpecialKeywords.VIDEO_FILE_NAME);
						adb_pid = executor.startRecording(SpecialKeywords.VIDEO_FILE_NAME);
					}
					
		    	} else {
		    		if (!Configuration.getBoolean(Parameter.TESTS_DEPENDENT_MODE)) {
		    			LOGGER.error("Driver still exists in atomic test mode!");
		    		}
		    	}
		    	if (browserVersion.isEmpty())
		    		browserVersion = DriverFactory.getBrowserVersion(getDriver());		    	
		    }
		    TEST_EXECUTER_LOG = executionContext.initBeforeTest(xmlTest.getName());
		    apiMethodBuilder = new APIMethodBuilder();
		}
		catch (Throwable thr)
		{
			LOGGER.error(thr.getMessage());
			thr.printStackTrace();
		    context.setAttribute(SpecialKeywords.INITIALIZATION_FAILURE, thr);
		    throw thr;
		}
    }

    @AfterMethod(alwaysRun = true)
    public void executeAfterTestMethod(ITestResult result) throws IOException
    {
	try
	{
		String testName = TestNamingUtil.getCanonicalTestName(result);

		GlobalTestLog glblLog = ((GlobalTestLog) result.getAttribute(GlobalTestLog.KEY));
    
	    File testLogFile = new File(ReportContext.getTestDir(testName) + "/test.log");
	    if (!testLogFile.exists()) testLogFile.createNewFile();
	    FileWriter fw = new FileWriter(testLogFile);
	    
/*	    //Spira test steps integration
	    SpiraTestIntegrator.logTestStepsInfo(this.getClass().getName(), result);
		
*/	    // Populate JIRA ID
	    if (jiraTickets.size() == 0) { //it was not redefined in the test
			if(result.getTestContext().getCurrentXmlTest().getParameter(SpecialKeywords.JIRA_TICKET) != null) {
				jiraTickets.add(result.getTestContext().getCurrentXmlTest().getParameter(SpecialKeywords.JIRA_TICKET));
			}
			if(result.getMethod().getDescription() != null && result.getMethod().getDescription().contains(SpecialKeywords.JIRA_TICKET)) {
				jiraTickets.add(result.getMethod().getDescription().split("#")[1]); 
			}

			@SuppressWarnings("unchecked")
			Map<Object[], String> testnameJiraMap = (Map<Object[], String>) result.getTestContext().getAttribute("jiraTicketsMappedToArgs");		
			if (testnameJiraMap != null) {
				String testHash = String.valueOf(Arrays.hashCode(result.getParameters()));					
				if (testnameJiraMap.containsKey(testHash)) {
					jiraTickets.add(testnameJiraMap.get(testHash));
				}
			}	    	
	    }
		result.setAttribute(SpecialKeywords.JIRA_TICKET, jiraTickets);	    
	    Jira.updateAfterTest(result);
	    
	    WebDriver drv = getDriver();

	    if (isUITest() && drv != null)
	    {
			fw.append("\r\n**************************** UI logs ****************************\r\n\r\n");
			
			try
			{
				//fw.append(TestLogHelper.getSessionLogs(testName));
				fw.append(TestLogHelper.getSessionLogs(drv));
			}
			catch (Exception e)
			{
			    LOGGER.error("AfterTest - unable to get test logs. " + e.getMessage());
			}

			if (!Configuration.getBoolean(Parameter.TESTS_DEPENDENT_MODE)) {
				quitDriver();

				if (Configuration.getBoolean(Parameter.VIDEO_RECORDING)) {
					executor.stopRecording(adb_pid); //stop recording
					pause(3); //very often video from device is black. trying to wait before pulling the file
					executor.pullFile(SpecialKeywords.VIDEO_FILE_NAME, ReportContext.getTestDir(testName) + "/video.mp4");
				}
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
	    	LOGGER.error("Error during FileWriter close. " + e.getMessage());
	    	e.printStackTrace();
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
	    LOGGER.error("Exception in executeAfterTestMethod");
	    e.printStackTrace();
	}
    }

    @AfterSuite(alwaysRun = true)
    public void executeAfterSuite(ITestContext context)
    {
		try
		{
			if (Configuration.getBoolean(Parameter.TESTS_DEPENDENT_MODE) && isUITest() && getDriver() != null) {
				quitDriver();

				if (Configuration.getBoolean(Parameter.VIDEO_RECORDING)) {
					executor.stopRecording(adb_pid); //stop recording
					pause(3); //very often video from device is black. trying to wait before pulling the file
					executor.pullFile(SpecialKeywords.VIDEO_FILE_NAME, ReportContext.getBaseDir() + "/video.mp4");
				}				
			}
			
		    executionContext.finilizeAfterSuite();
		    HtmlReportGenerator.generate(ReportContext.getBaseDir().getAbsolutePath());

		    String deviceName = "Desktop";
		    String browser = Configuration.get(Parameter.BROWSER);
		    if (!browserVersion.isEmpty()) {
		    	browser = browser + " " + browserVersion;
		    }
		    
		    String driverTitle = browser;
		    
		    if (Configuration.get(Parameter.BROWSER).equalsIgnoreCase("mobile") || Configuration.get(Parameter.BROWSER).equalsIgnoreCase("mobile_grid")) {
		    	deviceName = driverTitle = Configuration.get(Parameter.MOBILE_DEVICE_NAME);
		    	if (!Configuration.get(Parameter.MOBILE_BROWSER_NAME).equalsIgnoreCase("null")) {
		    			browser = Configuration.get(Parameter.MOBILE_BROWSER_NAME);
		    			driverTitle = driverTitle + "/" + browser;
		    	}
		    	else {
		    		browser = "";
		    	}		    		
		    }
		    
		    String env = !Configuration.isNull(Parameter.ENV) ? Configuration.get(Parameter.ENV) : Configuration.get(Parameter.URL);
	
		    String title = null;
		    if (context.getSuite().getXmlSuite() != null && !"Default suite".equals(context.getSuite().getXmlSuite().getName()))
		    {
				String suiteName = Configuration.isNull(Parameter.SUITE_NAME) ? context.getSuite().getXmlSuite().getName() : Configuration
					.get(Parameter.SUITE_NAME);
				String xmlFile = !StringUtils.isEmpty(System.getProperty("suite")) ? System.getProperty("suite") + ".xml" : StringUtils
					.substringAfterLast(context.getSuite().getXmlSuite().getFileName(), "\\");
				title = String.format(XML_TITLE, EmailReportGenerator.getSuiteResult(EmailReportItemCollector.getTestResults()).name(), suiteName,
						xmlFile, env, driverTitle);			
		    }
		    else
		    {
				String suiteName = Configuration.isNull(Parameter.SUITE_NAME) ? R.EMAIL.get("title") : Configuration.get(Parameter.SUITE_NAME);
				title = String.format(CLASS_TITLE, EmailReportGenerator.getSuiteResult(EmailReportItemCollector.getTestResults()).name(), suiteName,
						env, driverTitle);			
		    }
		    
		    ReportContext.getTempDir().delete();
	
		    // Update JIRA
		    Jira.updateAfterSuite(context, EmailReportItemCollector.getTestResults());
	
		    // Generate email report
		    EmailReportGenerator report = new EmailReportGenerator(title, env, Configuration.get(Parameter.APP_VERSION),
		    		deviceName, browser, DateUtils.now(), getCIJobReference(), EmailReportItemCollector.getTestResults(),
				    EmailReportItemCollector.getCreatedItems());	    
	
		    // Send report for specified emails
		    EmailManager.send(title, report.getEmailBody(), Configuration.get(Parameter.EMAIL_LIST), Configuration.get(Parameter.SENDER_EMAIL), 
			    Configuration.get(Parameter.SENDER_PASSWORD));
	
		    printExecutionSummary(EmailReportItemCollector.getTestResults());
		    
		    if (EmailReportGenerator.getSuiteResult(EmailReportItemCollector.getTestResults()).equals(TestResultType.SKIP)) {
		    	Assert.fail("Skipped tests detected! Analyze logs to determine possible configuration issues.");
		    }
		    	
		}
		catch (Exception e)
		{
		    LOGGER.error("Exception in executeAfterSuite");
		    e.printStackTrace();
		}
    }


    public Object[][] createTestArgSets2(ITestContext context, String executeColumn, String executeValue, String... staticArgs)
    {
		XLSDSBean dsBean = new XLSDSBean(context);
		XLSTable dsData = XLSParser.parseSpreadSheet(dsBean.getXlsFile(), dsBean.getXlsSheet(), executeColumn, executeValue);
		Object[][] args = new Object[dsData.getDataRows().size()][staticArgs.length + 1];
		
		String jiraColumnName = context.getCurrentXmlTest().getParameter(SpecialKeywords.EXCEL_DS_JIRA);
		
		int rowIndex = 0;
		for (Map<String, String> xlsRow : dsData.getDataRows())
		{
			String testName = context.getName();
			
			args[rowIndex][0] = xlsRow;

		    for (int i=0; i<staticArgs.length; i++){
		    	args[rowIndex][i + 1] = ParameterGenerator.process(dsBean.getTestParams().get(staticArgs[i]), context.getAttribute(SpecialKeywords.UUID).toString()); //zero element is a hashmap 
		    }
		    //update testName adding UID values from DataSource arguments if any
			testName = dsBean.setDataSorceUUID(testName, xlsRow);

			testNameMappedToArgs.put(String.valueOf(Arrays.hashCode(args[rowIndex])), testName);
			
			//add jira ticket from xls datasource to special hashMap
			if (jiraColumnName != null) {
				if (!jiraColumnName.isEmpty()) {
					jiraTicketsMappedToArgs.put(String.valueOf(Arrays.hashCode(args[rowIndex])), xlsRow.get(jiraColumnName));
				}
			}
			
		    rowIndex++;
		}

		context.setAttribute("testNameMappedToArgs", testNameMappedToArgs);
		context.setAttribute("jiraTicketsMappedToArgs", jiraTicketsMappedToArgs);
		
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
		
		String jiraColumnName = context.getCurrentXmlTest().getParameter(SpecialKeywords.EXCEL_DS_JIRA);
		
		int rowIndex = 0;
		for (Map<String, String> xlsRow : dsData.getDataRows())
		{
			String testName = context.getName();
			
		    for (int i = 0; i < argNames.length; i++)
		    {
		    	//read one line from xls and set to arguments from DataSource
				if (dsBean.getArgs().contains(argNames[i]))
				{
				    args[rowIndex][i] = ParameterGenerator.process(xlsRow.get(argNames[i]), context.getAttribute(SpecialKeywords.UUID).toString());
				}
				else
				{
				    args[rowIndex][i] = ParameterGenerator.process(dsBean.getTestParams().get(argNames[i]), context.getAttribute(SpecialKeywords.UUID).toString());
				}
		    }
		    //update testName adding UID values from DataSource arguments if any
			testName = dsBean.setDataSorceUUID(testName, xlsRow);
			testNameMappedToArgs.put(String.valueOf(Arrays.hashCode(args[rowIndex])), testName);
			
			//add jira ticket from xls datasource to special hashMap
			if (jiraColumnName != null) {
				if (!jiraColumnName.isEmpty()) {			
					jiraTicketsMappedToArgs.put(String.valueOf(Arrays.hashCode(args[rowIndex])), xlsRow.get(jiraColumnName));
				}
			}

			
		    rowIndex++;
		}
		
		
		context.setAttribute("testNameMappedToArgs", testNameMappedToArgs);
		context.setAttribute("jiraTicketsMappedToArgs", jiraTicketsMappedToArgs);

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
		    	LOGGER.info("Could not connect to Jenkins!");
		    }
		}
		else
		{
		    	LOGGER.info("Specify 'jenkins_url' and 'jenkins_job' in CONFIG to have reference to test job!");
		}
		return ciTestJob;
    }

    protected abstract boolean isUITest();
    
	protected void setJiraTicket(String ticket) {
		jiraTickets = new ArrayList<String>();
		jiraTickets.add(ticket);
	}
	protected void setJiraTicket(String[] tickets) {
		jiraTickets = new ArrayList<String>();
		for (String ticket : tickets) {
			jiraTickets.add(ticket);
		}
	}
}
