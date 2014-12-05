package com.qaprosoft.carina.core.foundation.utils;


import org.testng.Assert;
import org.testng.annotations.Test;

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
		System.setProperty("url", "{must_override}");
		Configuration.validateConfiguration();
	}
}
