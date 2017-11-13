package com.qaprosoft.carina.core.foundation.report.testrail;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This defines the 'TestRailCases' annotation used to specify the TestRail test
 * case ides that the TestNG test maps to
 * 
 */
@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = java.lang.annotation.ElementType.METHOD)
public @interface TestRailCases {
    String testCasesId();
}