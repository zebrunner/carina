package com.qaprosoft.carina.core.foundation.utils;


import org.testng.Assert;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

/**
 * Tests for {@link Configuration}
 */
public class ConfugurationTest 
{
	@Test
	public void testConfigOverrride()
	{
		System.setProperty("env", "UNITTEST");
		Assert.assertEquals(Configuration.getEnvArg("override"), "override_me");
		System.setProperty("UNITTEST.override", "i_am_overriden");
		Assert.assertEquals(Configuration.getEnvArg("override"), "i_am_overriden");
	}
	
	@Test
	public void testGetEnvArg()
	{
		System.setProperty("env", "QA");
		Assert.assertEquals(Configuration.getEnvArg("url"), "local");
		System.setProperty("env", "PROD");
		Assert.assertEquals(Configuration.getEnvArg("url"), "remote");
	}
	
	@Test(expectedExceptions={RuntimeException.class})
	public void testInvalidConfigValidation()
	{
		System.setProperty("platform", "{must_override}");
		Configuration.validateConfiguration();
	}
	
	@Test
	public void testConfigurationPlacehodler()
	{
		System.setProperty("env", "STG");
		Assert.assertEquals(Configuration.getEnvArg("url"), "http://localhost:8081");
		Assert.assertEquals(Configuration.get(Parameter.URL), "http://localhost:8081");
	}
}
