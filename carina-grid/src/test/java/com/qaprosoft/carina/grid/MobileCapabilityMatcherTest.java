package com.qaprosoft.carina.grid;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import io.appium.java_client.remote.MobileCapabilityType;

public class MobileCapabilityMatcherTest
{
	private MobileCapabilityMatcher matcher = new MobileCapabilityMatcher();
	
	@Test
	public void testPlatformName()
	{
		Map<String, Object> nodeCapability = new HashMap<>();
		nodeCapability.put(MobileCapabilityType.PLATFORM_NAME, "ANDROID");
		
		Map<String, Object> requestedCapability = new HashMap<>();
		requestedCapability.put(MobileCapabilityType.PLATFORM_NAME, "Android");
		
		Assert.assertTrue(matcher.matches(nodeCapability, requestedCapability));
		nodeCapability.put(MobileCapabilityType.PLATFORM_NAME, "iOS");
		Assert.assertFalse(matcher.matches(nodeCapability, requestedCapability));
	}
	
	@Test
	public void testMultipleVersions()
	{
		Map<String, Object> nodeCapability = new HashMap<>();
		nodeCapability.put(MobileCapabilityType.PLATFORM_VERSION, "6.1.1");
		
		Map<String, Object> requestedCapability = new HashMap<>();
		requestedCapability.put(MobileCapabilityType.PLATFORM_VERSION, "6.1.1,7.0");
		
		Assert.assertTrue(matcher.matches(nodeCapability, requestedCapability));
		nodeCapability.put(MobileCapabilityType.PLATFORM_VERSION, "4.3");
		Assert.assertFalse(matcher.matches(nodeCapability, requestedCapability));
	}
	
	@Test
	public void testMinBorderVersion()
	{
		Map<String, Object> nodeCapability = new HashMap<>();
		nodeCapability.put(MobileCapabilityType.PLATFORM_VERSION, "6.1.1");
		
		Map<String, Object> requestedCapability = new HashMap<>();
		requestedCapability.put(MobileCapabilityType.PLATFORM_VERSION, "6.0+");
		
		Assert.assertTrue(matcher.matches(nodeCapability, requestedCapability));
		nodeCapability.put(MobileCapabilityType.PLATFORM_VERSION, "4.3");
		Assert.assertFalse(matcher.matches(nodeCapability, requestedCapability));
	}
	
	@Test
	public void testRangeVersion()
	{
		Map<String, Object> nodeCapability = new HashMap<>();
		nodeCapability.put(MobileCapabilityType.PLATFORM_VERSION, "6.1.1");
		
		Map<String, Object> requestedCapability = new HashMap<>();
		requestedCapability.put(MobileCapabilityType.PLATFORM_VERSION, "6.1.1-7.0");
		
		Assert.assertTrue(matcher.matches(nodeCapability, requestedCapability));
		nodeCapability.put(MobileCapabilityType.PLATFORM_VERSION, "7.1");
		Assert.assertFalse(matcher.matches(nodeCapability, requestedCapability));
	}
	
	@Test
	public void testExactVersion()
	{
		Map<String, Object> nodeCapability = new HashMap<>();
		nodeCapability.put(MobileCapabilityType.PLATFORM_VERSION, "7.0");
		
		Map<String, Object> requestedCapability = new HashMap<>();
		requestedCapability.put(MobileCapabilityType.PLATFORM_VERSION, "7.0");
		
		Assert.assertTrue(matcher.matches(nodeCapability, requestedCapability));
		nodeCapability.put(MobileCapabilityType.PLATFORM_VERSION, "7.1.1");
		Assert.assertFalse(matcher.matches(nodeCapability, requestedCapability));
	}
	
	@Test
	public void testUDID()
	{
		Map<String, Object> nodeCapability = new HashMap<>();
		nodeCapability.put(MobileCapabilityType.UDID, "sdf44242ggsd");
		
		Map<String, Object> requestedCapability = new HashMap<>();
		requestedCapability.put(MobileCapabilityType.UDID, "sdf44242ggsd");
		
		Assert.assertTrue(matcher.matches(nodeCapability, requestedCapability));
		nodeCapability.put(MobileCapabilityType.UDID, "tt64fdfdfgdf");
		Assert.assertFalse(matcher.matches(nodeCapability, requestedCapability));
	}
	
	@Test
	public void testUDIDs()
	{
		Map<String, Object> nodeCapability = new HashMap<>();
		nodeCapability.put(MobileCapabilityType.UDID, "sdf44242ggsd");
		
		Map<String, Object> requestedCapability = new HashMap<>();
		requestedCapability.put(MobileCapabilityType.UDID, "sdf44242ggsd,seee4242ggsd");
		
		Assert.assertTrue(matcher.matches(nodeCapability, requestedCapability));
	}
	
	@Test
	public void testDeviceName()
	{
		Map<String, Object> nodeCapability = new HashMap<>();
		nodeCapability.put(MobileCapabilityType.DEVICE_NAME, "Samsung_Galaxy_S6");
		
		Map<String, Object> requestedCapability = new HashMap<>();
		requestedCapability.put(MobileCapabilityType.DEVICE_NAME, "Samsung_Galaxy_S6");
		
		Assert.assertTrue(matcher.matches(nodeCapability, requestedCapability));
		nodeCapability.put(MobileCapabilityType.DEVICE_NAME, "Samsung_Galaxy_S7");
		Assert.assertFalse(matcher.matches(nodeCapability, requestedCapability));
	}
	
	@Test
	public void testDeviceNames()
	{
		Map<String, Object> nodeCapability = new HashMap<>();
		nodeCapability.put(MobileCapabilityType.DEVICE_NAME, "Samsung_Galaxy_S6");
		
		Map<String, Object> requestedCapability = new HashMap<>();
		requestedCapability.put(MobileCapabilityType.DEVICE_NAME, "Samsung_Galaxy_S6,Samsung_Galaxy_S7");
		
		Assert.assertTrue(matcher.matches(nodeCapability, requestedCapability));
	}
	
	@Test
	public void testCombination()
	{
		Map<String, Object> nodeCapability = new HashMap<>();
		nodeCapability.put(MobileCapabilityType.PLATFORM_NAME, "Android");
		nodeCapability.put(MobileCapabilityType.PLATFORM_VERSION, "7.0");
		nodeCapability.put(MobileCapabilityType.DEVICE_NAME, "Samsung_Galaxy_S6");
		nodeCapability.put(MobileCapabilityType.UDID, "HJER23423423FF");
		
		Map<String, Object> requestedCapability = new HashMap<>();
		requestedCapability.put(MobileCapabilityType.PLATFORM_NAME, "Android");
		requestedCapability.put(MobileCapabilityType.PLATFORM_VERSION, "6.1+");
		
		Assert.assertTrue(matcher.matches(nodeCapability, requestedCapability));
	}
}