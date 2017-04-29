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

		String[] healthCheckMethods = Configuration.get(Parameter.HEALTH_CHECK_METHODS).split(",");
		checkHealth(suite, healthCheckClass, healthCheckMethods);
	}

	private void checkHealth(ISuite suite, String className, String[] methods) {

		if (className.isEmpty()) {
			return;
		}

		// hc suite class should be available
		// hc method name should be available

		// create runtime XML suite for health check
		XmlSuite xmlSuite = new XmlSuite();
		xmlSuite.setName("HealthCheck XmlSuite - " + className);

		XmlTest xmltest = new XmlTest(xmlSuite);
		xmltest.setName("HealthCheck TestCase");
		XmlClass healthCheckClass = new XmlClass();
		healthCheckClass.setName(className);

		if (methods.length > 0) {
			// declare particular methods if they are provided
			List<XmlInclude> methodsToRun = constructIncludes(methods);
			healthCheckClass.setIncludedMethods(methodsToRun);
		}

		xmltest.setXmlClasses(Arrays.asList(new XmlClass[] { healthCheckClass }));

		LOGGER.info("HealthCheck '" + className + "' is started.");
		LOGGER.debug("HealthCheck suite content:" + xmlSuite.toXml());

		// Second TestNG process to run HealthCheck
		TestNG testng = new TestNG();
		testng.setXmlSuites(Arrays.asList(xmlSuite));

		TestListenerAdapter tla = new TestListenerAdapter();
		testng.addListener(tla);

		testng.run();
		synchronized (this) {
			if (tla.getPassedTests().size() > 0 && tla.getFailedTests().size() == 0
					&& tla.getSkippedTests().size() == 0) {
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
