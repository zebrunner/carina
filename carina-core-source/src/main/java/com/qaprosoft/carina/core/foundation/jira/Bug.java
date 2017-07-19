package com.qaprosoft.carina.core.foundation.jira;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Bug
{
	public String id();
}
