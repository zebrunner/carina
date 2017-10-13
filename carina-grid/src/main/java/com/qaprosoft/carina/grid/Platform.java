package com.qaprosoft.carina.grid;

import java.util.Map;

public enum Platform
{
	ANY, ANDROID, IOS, WINDOWS, MAC, LINUX;
	
	public static Platform fromCapabilities(Map<String, Object> cap)
	{
		Platform platform = Platform.ANY;
		
		if(cap != null && cap.containsKey("platform") && cap.get("platform") != null)
		{
			platform = Platform.valueOf(cap.get("platform").toString().toUpperCase());
		}
		if(cap != null && cap.containsKey("platformName") && cap.get("platformName") != null)
		{
			platform = Platform.valueOf(cap.get("platformName").toString().toUpperCase());
		}
		
		return platform;
	}
}
