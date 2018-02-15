/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.grid;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MobileCapabilityMatcherTest
{
	private MobileCapabilityMatcher matcher = new MobileCapabilityMatcher();
	
	private static final String PLATFORM_NAME = "platformName";
    private static final String PLATFORM_VERSION = "platformVersion";
    private static final String DEVICE_NAME = "deviceName";
    private static final String UDID = "udid";
	
	@Test
	public void testPlatformName()
	{
		Map<String, Object> nodeCapability = new HashMap<>();
		nodeCapability.put(PLATFORM_NAME, "ANDROID");
		
		Map<String, Object> requestedCapability = new HashMap<>();
		requestedCapability.put(PLATFORM_NAME, "Android");
		
		Assert.assertTrue(matcher.matches(nodeCapability, requestedCapability));
		nodeCapability.put(PLATFORM_NAME, "iOS");
		Assert.assertFalse(matcher.matches(nodeCapability, requestedCapability));
	}
	
	@Test
	public void testMultipleVersions()
	{
		Map<String, Object> nodeCapability = new HashMap<>();
		nodeCapability.put(PLATFORM_VERSION, "6.1.1");
		
		Map<String, Object> requestedCapability = new HashMap<>();
		requestedCapability.put(PLATFORM_VERSION, "6.1.1,7.0");
		
		Assert.assertTrue(matcher.matches(nodeCapability, requestedCapability));
		nodeCapability.put(PLATFORM_VERSION, "4.3");
		Assert.assertFalse(matcher.matches(nodeCapability, requestedCapability));
	}
	
	@Test
	public void testMinBorderVersion()
	{
		Map<String, Object> nodeCapability = new HashMap<>();
		nodeCapability.put(PLATFORM_VERSION, "6.1.1");
		
		Map<String, Object> requestedCapability = new HashMap<>();
		requestedCapability.put(PLATFORM_VERSION, "6.0+");
		
		Assert.assertTrue(matcher.matches(nodeCapability, requestedCapability));
		nodeCapability.put(PLATFORM_VERSION, "4.3");
		Assert.assertFalse(matcher.matches(nodeCapability, requestedCapability));
	}
	
	@Test
	public void testRangeVersion()
	{
		Map<String, Object> nodeCapability = new HashMap<>();
		nodeCapability.put(PLATFORM_VERSION, "6.1.1");
		
		Map<String, Object> requestedCapability = new HashMap<>();
		requestedCapability.put(PLATFORM_VERSION, "6.1.1-7.0");
		
		Assert.assertTrue(matcher.matches(nodeCapability, requestedCapability));
		nodeCapability.put(PLATFORM_VERSION, "7.1");
		Assert.assertFalse(matcher.matches(nodeCapability, requestedCapability));
	}
	
	@Test
	public void testExactVersion()
	{
		Map<String, Object> nodeCapability = new HashMap<>();
		nodeCapability.put(PLATFORM_VERSION, "7.0");
		
		Map<String, Object> requestedCapability = new HashMap<>();
		requestedCapability.put(PLATFORM_VERSION, "7.0");
		
		Assert.assertTrue(matcher.matches(nodeCapability, requestedCapability));
		nodeCapability.put(PLATFORM_VERSION, "7.1.1");
		Assert.assertFalse(matcher.matches(nodeCapability, requestedCapability));
	}
	
	@Test
	public void testUDID()
	{
		Map<String, Object> nodeCapability = new HashMap<>();
		nodeCapability.put(UDID, "sdf44242ggsd");
		
		Map<String, Object> requestedCapability = new HashMap<>();
		requestedCapability.put(UDID, "sdf44242ggsd");
		
		Assert.assertTrue(matcher.matches(nodeCapability, requestedCapability));
		nodeCapability.put(UDID, "tt64fdfdfgdf");
		Assert.assertFalse(matcher.matches(nodeCapability, requestedCapability));
	}
	
	@Test
	public void testUDIDs()
	{
		Map<String, Object> nodeCapability = new HashMap<>();
		nodeCapability.put(UDID, "sdf44242ggsd");
		
		Map<String, Object> requestedCapability = new HashMap<>();
		requestedCapability.put(UDID, "sdf44242ggsd,seee4242ggsd");
		
		Assert.assertTrue(matcher.matches(nodeCapability, requestedCapability));
	}
	
	@Test
	public void testDeviceName()
	{
		Map<String, Object> nodeCapability = new HashMap<>();
		nodeCapability.put(DEVICE_NAME, "Samsung_Galaxy_S6");
		
		Map<String, Object> requestedCapability = new HashMap<>();
		requestedCapability.put(DEVICE_NAME, "Samsung_Galaxy_S6");
		
		Assert.assertTrue(matcher.matches(nodeCapability, requestedCapability));
		nodeCapability.put(DEVICE_NAME, "Samsung_Galaxy_S7");
		Assert.assertFalse(matcher.matches(nodeCapability, requestedCapability));
	}
	
	@Test
	public void testDeviceNames()
	{
		Map<String, Object> nodeCapability = new HashMap<>();
		nodeCapability.put(DEVICE_NAME, "Samsung_Galaxy_S6");
		
		Map<String, Object> requestedCapability = new HashMap<>();
		requestedCapability.put(DEVICE_NAME, "Samsung_Galaxy_S6,Samsung_Galaxy_S7");
		
		Assert.assertTrue(matcher.matches(nodeCapability, requestedCapability));
	}
	
	@Test
	public void testCombination()
	{
		Map<String, Object> nodeCapability = new HashMap<>();
		nodeCapability.put(PLATFORM_NAME, "Android");
		nodeCapability.put(PLATFORM_VERSION, "7.0");
		nodeCapability.put(DEVICE_NAME, "Samsung_Galaxy_S6");
		nodeCapability.put(UDID, "HJER23423423FF");
		
		Map<String, Object> requestedCapability = new HashMap<>();
		requestedCapability.put(PLATFORM_NAME, "Android");
		requestedCapability.put(PLATFORM_VERSION, "6.1+");
		
		Assert.assertTrue(matcher.matches(nodeCapability, requestedCapability));
	}
}