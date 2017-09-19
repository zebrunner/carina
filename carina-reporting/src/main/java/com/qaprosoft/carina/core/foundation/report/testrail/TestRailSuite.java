package com.qaprosoft.carina.core.foundation.report.testrail;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This defines the 'TestRailSuite' annotation used to specify the TestRail Suite id and project for the test being executed
 */
@Retention(value= RetentionPolicy.RUNTIME)
@Target(value= ElementType.TYPE)
public @interface TestRailSuite
{
	int testSuiteId();
	int projectId();
}
