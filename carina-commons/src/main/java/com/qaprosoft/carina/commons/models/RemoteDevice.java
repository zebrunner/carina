/*
 * Copyright 2013-2017 QAPROSOFT (http://qaprosoft.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qaprosoft.carina.commons.models;

/**
 * RemoteDevice represents base device info used in Selenium Hub.
 * 
 * @author akhursevich
 */
public class RemoteDevice
{
	private String name;
	private String type;
	private String os;
	private String osVersion;
	private String udid;
	private String remoteURL;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getOs()
	{
		return os;
	}

	public void setOs(String os)
	{
		this.os = os;
	}

	public String getOsVersion()
	{
		return osVersion;
	}

	public void setOsVersion(String osVersion)
	{
		this.osVersion = osVersion;
	}

	public String getUdid()
	{
		return udid;
	}

	public void setUdid(String udid)
	{
		this.udid = udid;
	}

	public String getRemoteURL()
	{
		return remoteURL;
	}

	public void setRemoteURL(String remoteURL)
	{
		this.remoteURL = remoteURL;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getType()
	{
		return type;
	}
}