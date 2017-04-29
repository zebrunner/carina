package com.qaprosoft.carina.core.foundation.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.SkipException;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Listeners;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

@Listeners(HealthCheckListener.class)
public class HealthCheckListener implements ISuiteListener {
	private static final Logger LOGGER = Logger.getLogger(HealthCheckListener.class);

	@Override
	public void onStart(ISuite suite) {

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

	@SuppressWarnings("deprecation")
	private void checkHealth(ISuite suite, String className, String[] methods) {

		if (className.isEmpty()) {
			return;
		}

		// create runtime XML suite for health check
		XmlSuite xmlSuite = new XmlSuite();
		xmlSuite.setName("HealthCheck XmlSuite - " + className);

		XmlTest xmltest = new XmlTest(xmlSuite);
		xmltest.setName("HealthCheck TestCase");
		XmlClass healthCheckClass = new XmlClass();
		healthCheckClass.setName(className);
		
		// TestNG do not execute missed methods so we have to calulate expected methods count to handle potential mistakes in methods naming  
		int expectedMethodsCount = -1; 
		if (methods != null) {
			// declare particular methods if they are provided
			List<XmlInclude> methodsToRun = constructIncludes(methods);
			expectedMethodsCount = methodsToRun.size();
			healthCheckClass.setIncludedMethods(methodsToRun);
		}

		xmltest.setXmlClasses(Arrays.asList(new XmlClass[] { healthCheckClass }));
		xmlSuite.setTests(Arrays.asList(new XmlTest[] { xmltest }));
		

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

	@Override
	public void onFinish(ISuite suite) {
		// do nothing
	}
}
