package com.qaprosoft.carina.core.foundation.report.spira;

import java.lang.annotation.*;

/**
 * This defines the 'SpiraTestSteps' annotation used to specify the
 * SpiraTest test step ides that the TestNG test maps to
 * 
 */
@Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value=java.lang.annotation.ElementType.METHOD)
public @interface SpiraTestSteps
{
	String testStepsId() default "";
}