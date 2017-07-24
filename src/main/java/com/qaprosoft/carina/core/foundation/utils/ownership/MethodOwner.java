package com.qaprosoft.carina.core.foundation.utils.ownership;

import java.lang.annotation.*;

/**
 * This defines the 'MethodOwner' annotation used to specify the
 * TestNG methods owners.
 * 
 */
@Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value=java.lang.annotation.ElementType.METHOD)
public @interface MethodOwner 
{
	String owner() default "";
	String secondaryOwner() default "";
}