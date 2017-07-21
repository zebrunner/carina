package com.qaprosoft.carina.core.utils;


import org.testng.Assert;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.R;

/**
 * Tests for {@link R}
 */
public class RTest 
{
	@Test
	public void testDefaultValue()
	{
		Assert.assertEquals(R.CONFIG.get("browser"), "chrome");
	}
	
	@Test(dependsOnMethods="testDefaultValue")
	public void testOverrideBySystemParam()
	{
		final String BROWSER = "firefox";
		R.CONFIG.put("browser", BROWSER);
		Assert.assertEquals(R.CONFIG.get("browser"), BROWSER);
	}
	
	@Test
	public void testOverrideInProperties()
	{
		Assert.assertEquals(R.CONFIG.get("port"), "8081");
	}
	
	@Test
	public void testPlaceholders()
	{
		Assert.assertEquals(R.CONFIG.get("url"), "http://localhost:8081");
	}
	
/*	@Test
	public void testEncryption()
	{
		Assert.assertEquals(R.CONFIG.get("password"), "EncryptMe");
		Assert.assertEquals(R.CONFIG.getSecured("password"), "{crypt:8O9iA4+f3nMzz85szmvKmQ==}");
	}
	
	@Test
	public void testPlaceholdersWithEncryption()
	{
		Assert.assertEquals(R.CONFIG.get("credentials"), "test@gmail.com/EncryptMe");
	}
*/
	
	@Test
	public void testPlaceholdersInEmail()
	{
		Assert.assertEquals(R.EMAIL.get("title"), "${test}");
	}
	
	@Test
	public void testOverride()
	{
		Assert.assertEquals(R.TESTDATA.get("key1"), "3");
		Assert.assertEquals(R.TESTDATA.get("key2"), "2");
		Assert.assertEquals(R.TESTDATA.get("key3"), "1");
		Assert.assertEquals(R.TESTDATA.get("key4"), "3");
		Assert.assertEquals(R.TESTDATA.get("key5"), "2");
	}
}
