package com.qaprosoft.carina.core.utils;


import org.testng.Assert;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;

/**
 * Tests for {@link Configuration}
 */
public class ConfugurationTest 
{
	@Test
	public void testConfigOverrride()
	{
		R.CONFIG.put("env", "UNITTEST");
		Assert.assertEquals(Configuration.getEnvArg("override"), "override_me");
		R.CONFIG.put("UNITTEST.override", "i_am_overriden");
		Assert.assertEquals(Configuration.getEnvArg("override"), "i_am_overriden");
	}
	
	@Test
	public void testGetEnvArg()
	{
		R.CONFIG.put("env", "QA");
		Assert.assertEquals(Configuration.getEnvArg("url"), "local");
		R.CONFIG.put("env", "PROD");
		Assert.assertEquals(Configuration.getEnvArg("url"), "remote");
	}
	
	@Test(expectedExceptions={RuntimeException.class})
	public void testInvalidConfigValidation()
	{
		R.CONFIG.put("platform", "{must_override}");
		Configuration.validateConfiguration();
	}
	
	@Test
	public void testConfigurationPlacehodler()
	{
		R.CONFIG.put("env", "STG");
		Assert.assertEquals(Configuration.getEnvArg("url"), "http://localhost:8081");
		Assert.assertEquals(Configuration.get(Parameter.URL), "http://localhost:8081");
	}
}
