package com.qaprosoft.carina.core.foundation.report.spira;

import java.lang.annotation.*;

/**
 * This defines the 'SpiraTestCase' annotation used to specify the Spira TestCase id,
 * project and release information for the test being executed
 * 
 */
@Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value=java.lang.annotation.ElementType.TYPE)
public @interface SpiraTestCase
{
	int testCaseId ();
	int projectId ();
}
