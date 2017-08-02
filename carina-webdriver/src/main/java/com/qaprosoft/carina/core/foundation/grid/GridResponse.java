package com.qaprosoft.carina.core.foundation.grid;

public class GridResponse
{
	private String testId;
	private String serial;
	private String remoteConnectURL;
	private String platform;
	private String version;
	private String model;
	private boolean connected;

	public String getTestId()
	{
		return testId;
	}

	public void setTestId(String testId)
	{
		this.testId = testId;
	}

	public String getSerial()
	{
		return serial;
	}

	public void setSerial(String serial)
	{
		this.serial = serial;
	}

	public String getRemoteConnectURL()
	{
		return remoteConnectURL;
	}

	public void setRemoteConnectURL(String remoteConnectURL)
	{
		this.remoteConnectURL = remoteConnectURL;
	}

	public boolean isConnected()
	{
		return connected;
	}

	public void setConnected(boolean connected)
	{
		this.connected = connected;
	}

	public String getPlatform()
	{
		return platform;
	}

	public void setPlatform(String platform)
	{
		this.platform = platform;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public String getModel()
	{
		return model;
	}

	public void setModel(String model)
	{
		this.model = model;
	}
}
