package com.qaprosoft.carina.core.foundation.webdriver.device;

//Motorola|ANDROID|4.4|T01130FJAD|http://localhost:4725/wd/hub;Samsung_S4|ANDROID|4.4.2|5ece160b|http://localhost:4729/wd/hub;
public class Device {
	
	private String name;
	private String os;
	private String osVersion;
	private String udid;
	private String seleniumServer;
	
	public Device() {
		this.name = null;
		this.os = null;
		this.osVersion = null;
		this.udid = null;
		this.seleniumServer = null;
	}
	
	public Device(String name, String os, String osVersion, String udid, String seleniumServer) {
		this.name = name;
		this.os = os;
		this.osVersion = osVersion;
		this.udid = udid;
		this.seleniumServer = seleniumServer;
	}

	public Device(String args) {
		//Samsung_S4|ANDROID|4.4.2|5ece160b|4729|4730|http://localhost:4725/wd/hub
		String[] params = args.split("\\|");
		
		//TODO: organize verification onto the params  count
		this.name = params[0];
		this.os = params[1];
		this.osVersion = params[2];
		this.udid = params[3];
		this.seleniumServer = params[4];
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public String getUdid() {
		return udid;
	}

	public void setUdid(String udid) {
		this.udid = udid;
	}

	public String getSeleniumServer() {
		return seleniumServer;
	}

	public void setSeleniumServer(String seleniumServer) {
		this.seleniumServer = seleniumServer;
	}

}
