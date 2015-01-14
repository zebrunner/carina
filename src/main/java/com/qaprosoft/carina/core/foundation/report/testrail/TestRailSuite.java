package com.qaprosoft.carina.core.foundation.report.testrail;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This defines the 'SpiraTestCase' annotation used to specify the Spira TestCase id,
 * project and release information for the test being executed
 * 
 */
@Retention(value= RetentionPolicy.RUNTIME)
@Target(value= ElementType.TYPE)
public @interface TestRailSuite
{
	int testSuiteId();
	int projectId();
}
