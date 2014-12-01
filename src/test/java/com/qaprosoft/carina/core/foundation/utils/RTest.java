package com.qaprosoft.carina.core.foundation.utils;


import org.testng.Assert;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.R;

/**
 * Tests for {@link R}
 */
public class RTest 
{
	@Test
	public void testConfigOverrride()
	{
		System.setProperty("env", "UNITTEST");
		Assert.assertEquals(Configuration.getEnvArg("override"), "override_me");
		System.setProperty("UNITTEST.override", "i_am_overriden");
		Assert.assertEquals(Configuration.getEnvArg("override"), "i_am_overriden");
	}
}
