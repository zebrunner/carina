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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.xml.XmlTest;

import com.jayway.restassured.RestAssured;
import com.qaprosoft.carina.core.foundation.dataprovider.annotations.XlsDataSourceParameters;
import com.qaprosoft.carina.core.foundation.dataprovider.core.DataProviderFactory;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.DSBean;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.XLSParser;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.XLSTable;
import com.qaprosoft.carina.core.foundation.jira.Jira;
import com.qaprosoft.carina.core.foundation.log.ThreadLogAppender;
import com.qaprosoft.carina.core.foundation.report.HtmlReportGenerator;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.report.TestResultItem;
import com.qaprosoft.carina.core.foundation.report.TestResultType;
import com.qaprosoft.carina.core.foundation.report.email.EmailManager;
import com.qaprosoft.carina.core.foundation.report.email.EmailReportGenerator;
import com.qaprosoft.carina.core.foundation.report.email.EmailReportItemCollector;
import com.qaprosoft.carina.core.foundation.report.spira.SpiraTestIntegrator;
import com.qaprosoft.carina.core.foundation.report.zafira.ZafiraIntegrator;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.DateUtils;
import com.qaprosoft.carina.core.foundation.utils.L18n;
import com.qaprosoft.carina.core.foundation.utils.Messager;
import com.qaprosoft.carina.core.foundation.utils.ParameterGenerator;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.naming.TestNamingUtil;
import com.qaprosoft.carina.core.foundation.webdriver.DriverHelper;
import com.qaprosoft.zafira.client.model.TestType;


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

    //Jira ticket(s)
    protected List<String> jiraTickets = new ArrayList<String>();

    protected String browserVersion = "";
    
   
    @BeforeSuite(alwaysRun = true)
    public void executeBeforeTestSuite(ITestContext context) throws Throwable
    {
		try
		{
		    // Set log4j properties
		    PropertyConfigurator.configure(ClassLoader.getSystemResource("log4j.properties"));
		    // Set SoapUI log4j properties
		    System.setProperty("soapui.log4j.config", "./src/main/resources/soapui-log4j.xml");
	
		    LOGGER.info(Configuration.asString());
//		    Configuration.validateConfiguration();
	
		    ReportContext.removeOldReports();
		    context.getCurrentXmlTest().getSuite().setThreadCount(Configuration.getInt(Parameter.THREAD_COUNT));
	
		    if (!Configuration.isNull(Parameter.URL))
		    {
		    	RestAssured.baseURI = Configuration.get(Parameter.URL);
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

    
    @BeforeClass(alwaysRun = true)
    public void executeBeforeTestClass(ITestContext context) throws Throwable {
    	//do nothing for now
    	SpiraTestIntegrator.logTestCaseInfo(this.getClass().getName());
    }

    @AfterClass(alwaysRun = true)
    public void executeAfterTestClass(ITestContext context) throws Throwable {
    	//do nothing for now
    }
    
    @BeforeMethod(alwaysRun = true)
    public void executeBeforeTestMethod(XmlTest xmlTest, Method testMethod, ITestContext context) throws Throwable
    {
    	//do nothing for now
    }

    @AfterMethod(alwaysRun = true)
    public void executeAfterTestMethod(ITestResult result)
    {
		try
		{	    
			String test = TestNamingUtil.getCanonicalTestName(result);
		    // Populate JIRA ID
		    if (jiraTickets.size() == 0) { //it was not redefined in the test
		    	jiraTickets = Jira.getTickets(result);
		    }
		    
			result.setAttribute(SpecialKeywords.JIRA_TICKET, jiraTickets);	    
		    Jira.updateAfterTest(result);
		    
		    //TODO: implement zafira work items population
		    TestType testType = TestNamingUtil.getZafiraTest(test);
		    if (testType != null && jiraTickets.size() > 0) {
		    	ZafiraIntegrator.registerWorkItems(testType.getId(), jiraTickets);
		    }
		    

		    //clear jira tickets to be sure that next test is not affected.
		    jiraTickets.clear();

		    ThreadLogAppender tla = (ThreadLogAppender) Logger.getRootLogger().getAppender("ThreadLogAppender");
			if(tla != null)
			{
				tla.closeResource(test);
			}

		}
		catch (Exception e)
		{
		    LOGGER.error("Exception in AbstractTest->executeAfterTestMethod: " + e.getMessage());
		    e.printStackTrace();
		}

    }

    @AfterSuite(alwaysRun = true)
    public void executeAfterTestSuite(ITestContext context)
    {
		try
		{
		    HtmlReportGenerator.generate(ReportContext.getBaseDir().getAbsolutePath());
		    
		    String deviceName = "Desktop";
		    String browser = Configuration.get(Parameter.BROWSER);
		    if (!browserVersion.isEmpty()) {
		    	browser = browser + " " + browserVersion;
		    }
		    
		    String driverTitle = browser;
		    
		    if (Configuration.get(Parameter.BROWSER).equalsIgnoreCase("mobile") || Configuration.get(Parameter.BROWSER).equalsIgnoreCase("mobile_grid")) {
		    	deviceName = driverTitle = Configuration.get(Parameter.MOBILE_DEVICE_NAME) + " - " +
		    			Configuration.get(Parameter.MOBILE_PLATFORM_NAME) + " " +
		    			Configuration.get(Parameter.MOBILE_PLATFORM_VERSION);
		    	
		    	if (!Configuration.get(Parameter.MOBILE_BROWSER_NAME).isEmpty()) {
		    			browser = Configuration.get(Parameter.MOBILE_BROWSER_NAME);
		    			driverTitle = driverTitle + " - " + browser;
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
	
		    String emailContent = report.getEmailBody();
		    EmailManager.send(title, emailContent, Configuration.get(Parameter.EMAIL_LIST), Configuration.get(Parameter.SENDER_EMAIL), 
			    Configuration.get(Parameter.SENDER_PASSWORD));
		    
		    // Store emailable report under emailable-report.html
		    ReportContext.generateHtmlReport(emailContent);
	
		    printExecutionSummary(EmailReportItemCollector.getTestResults());
		    
		    if (EmailReportGenerator.getSuiteResult(EmailReportItemCollector.getTestResults()).equals(TestResultType.SKIP)) {
		    	Assert.fail("Skipped tests detected! Analyze logs to determine possible configuration issues.");
		    }
		    	
		}
		catch (Exception e)
		{
		    LOGGER.error("Exception in AbstractTest->executeAfterSuite: " + e.getMessage());
		    e.printStackTrace();
		}
    }


    //separate method to be able to retrieve information from different sheets in the same java test.
    @Deprecated
    public Object[][] createTestArgSets(String xlsFile, String xlsSheet, String dsArgs, String dsUids, ITestContext context, String executeColumn, String executeValue, String... staticArgs)
    {
		DSBean dsBean = new DSBean(xlsFile, xlsSheet, dsArgs, dsUids);
		
		XLSTable dsData = XLSParser.parseSpreadSheet(xlsFile, xlsSheet, executeColumn, executeValue);
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

		context.setAttribute(SpecialKeywords.TEST_NAME_ARGS_MAP, testNameMappedToArgs);
		context.setAttribute(SpecialKeywords.JIRA_ARGS_MAP, jiraTicketsMappedToArgs);
		return args;
    }
    
    @Deprecated
    public Object[][] createTestArgSets(String xlsSheet, String dsArgs, String dsUids, ITestContext context, String executeColumn, String executeValue, String... staticArgs)
    {
    	DSBean dsBean = new DSBean(context);
    	return createTestArgSets(dsBean.getDsFile(), xlsSheet, dsArgs, dsUids, context, executeColumn, executeValue, staticArgs);
    }
    
    
    //separate method to be able to retrieve information from different sheets in the same java test.
    @Deprecated
    public Object[][] createTestArgSets2(String sheet, ITestContext context, String executeColumn, String executeValue, String... staticArgs)
    {
		DSBean dsBean = new DSBean(context);
		XLSTable dsData = XLSParser.parseSpreadSheet(dsBean.getDsFile(), sheet, executeColumn, executeValue);
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

		context.setAttribute(SpecialKeywords.TEST_NAME_ARGS_MAP, testNameMappedToArgs);
		context.setAttribute(SpecialKeywords.JIRA_ARGS_MAP, jiraTicketsMappedToArgs);
		return args;
    }
    
    @Deprecated
    public Object[][] createTestArgSets2(ITestContext context, String executeColumn, String executeValue, String... staticArgs)
    {
		DSBean dsBean = new DSBean(context);
    	return createTestArgSets2(dsBean.getXlsSheet(), context, executeColumn, executeValue, staticArgs);

    }
    
   @Deprecated
		@DataProvider(name = "excel_ds2")
		public Object[][] readDataFromXLS2(ITestContext context)
		{
			return createTestArgSets2(context, "Execute", "Y");
		}

		@Deprecated
		public Object[][] createTestArgSets(ITestContext context, String executeColumn, String executeValue, String... staticArgs)
		{
			String[] argNames = ArrayUtils.addAll(context.getCurrentXmlTest().getParameter(SpecialKeywords.EXCEL_DS_ARGS).split(";"), staticArgs);
			DSBean dsBean = new DSBean(context);
			XLSTable dsData = XLSParser.parseSpreadSheet(dsBean.getDsFile(), dsBean.getXlsSheet(), executeColumn, executeValue);
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
		
		
		context.setAttribute(SpecialKeywords.TEST_NAME_ARGS_MAP, testNameMappedToArgs);
		context.setAttribute(SpecialKeywords.JIRA_ARGS_MAP, jiraTicketsMappedToArgs);

		return args;
    }

    @Deprecated
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
		if (!Configuration.isNull(Parameter.CI_URL) && !Configuration.isNull(Parameter.CI_BUILD)) {
			ciTestJob = Configuration.get(Parameter.CI_URL) + Configuration.get(Parameter.CI_BUILD);
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
	
	
	@DataProvider(name = "XLSDataProvider", parallel=true)
	public Object[][] getDataFromXlsFile(final Method testMethod,
			ITestContext context) {

		XlsDataSourceParameters parameters = testMethod
				.getAnnotation(XlsDataSourceParameters.class);
		DSBean dsBean = new DSBean(parameters, context
				.getCurrentXmlTest().getAllParameters());

		String executeColumn = "Execute";
		String executeValue = "y";

		if (!parameters.executeColumn().isEmpty())
			executeColumn = parameters.executeColumn();

		if (!parameters.executeValue().isEmpty())
			executeValue = parameters.executeValue();

		XLSTable dsData = XLSParser.parseSpreadSheet(dsBean.getDsFile(),
				dsBean.getXlsSheet(), executeColumn, executeValue);

		List<String> argsList = dsBean.getArgs();
		List<String> staticArgsList = dsBean.getStaticArgs();

		String jiraColumnName = context.getCurrentXmlTest().getParameter(
				SpecialKeywords.EXCEL_DS_JIRA);

		int width = 0;
		if (argsList.size() == 0) {
			width = staticArgsList.size() + 1;
		} else {
			width = argsList.size() + staticArgsList.size();
		}
		Object[][] args = new Object[dsData.getDataRows().size()][width];

		int rowIndex = 0;
		for (Map<String, String> xlsRow : dsData.getDataRows()) {
			String testName = context.getName();

			if (argsList.size() == 0) {
				args[rowIndex][0] = xlsRow;

				for (int i = 0; i < staticArgsList.size(); i++) {
					args[rowIndex][i + 1] = ParameterGenerator.process(dsBean
							.getTestParams().get(staticArgsList.get(i)),
							context.getAttribute(SpecialKeywords.UUID)
									.toString()); // zero element is a hashmap
				}
			} else {
				int i = 0;
				for (i = 0; i < argsList.size(); i++) {
					args[rowIndex][i] = ParameterGenerator.process(xlsRow
							.get(argsList.get(i)),
							context.getAttribute(SpecialKeywords.UUID)
									.toString());

				}
				//populate the rest of items by static parameters from testParams
				for (int j = 0; j < staticArgsList.size(); j++) {
					args[rowIndex][i + j] = ParameterGenerator.process(dsBean
							.getTestParams().get(staticArgsList.get(j)),
							context.getAttribute(SpecialKeywords.UUID)
									.toString());
				}
			}
			// update testName adding UID values from DataSource arguments if
			// any
			testName = dsBean.setDataSorceUUID(testName, xlsRow);

			testNameMappedToArgs.put(
					String.valueOf(Arrays.hashCode(args[rowIndex])), testName);

			// add jira ticket from xls datasource to special hashMap
			if (jiraColumnName != null) {
				if (!jiraColumnName.isEmpty()) {
					jiraTicketsMappedToArgs.put(
							String.valueOf(Arrays.hashCode(args[rowIndex])),
							xlsRow.get(jiraColumnName));
				}
			}

			rowIndex++;
		}

		context.setAttribute(SpecialKeywords.TEST_NAME_ARGS_MAP, testNameMappedToArgs);
		context.setAttribute(SpecialKeywords.JIRA_ARGS_MAP, jiraTicketsMappedToArgs);

		return args;
	}
	
	 @DataProvider(name = "DataProvider")
	    public Object[][] createData(final Method testMethod,
	                                  ITestContext context) {
	        Annotation[] annotations = testMethod.getDeclaredAnnotations();
	        Object[][] objects = DataProviderFactory.getDataProvider(annotations, context);
	        return objects;
	    }

}
