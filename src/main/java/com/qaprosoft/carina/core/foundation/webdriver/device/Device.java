package com.qaprosoft.carina.core.foundation.webdriver.device;

import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.factory.DeviceType.Type;

//Motorola|ANDROID|4.4|T01130FJAD|http://localhost:4725/wd/hub;Samsung_S4|ANDROID|4.4.2|5ece160b|http://localhost:4729/wd/hub;
public class Device {
	
	private String name;
	private String type;

	private String os;
	private String osVersion;
	private String udid;
	private String seleniumServer;
	
	private String testId;
	
	public Device() {
		this.name = null;
		this.type = null;
		this.os = null;
		this.osVersion = null;
		this.udid = null;
		this.seleniumServer = null;
	}
	
	public Device(String name, String type, String os, String osVersion, String udid, String seleniumServer) {
		this.name = name;
		this.type = type;
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
		this.type = params[1];
		this.os = params[2];
		this.osVersion = params[3];
		this.udid = params[4];
		this.seleniumServer = params[5];
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

	public boolean isPhone() {
		return type.equalsIgnoreCase(SpecialKeywords.PHONE);
	}
	
	public boolean isTablet() {
		return type.equalsIgnoreCase(SpecialKeywords.TABLET);
	}
	
	public boolean isTv() {
		return type.equalsIgnoreCase(SpecialKeywords.TV);
	}
	
	public String getTestId() {
		return testId;
	}

	public void setTestId(String testId) {
		this.testId = testId;
	}

	public Type getType() {
		if (os.equalsIgnoreCase(SpecialKeywords.ANDROID)) {
			if (isTablet()) {
				return Type.ANDROID_TABLET;
			}
			if (isTv()) {
				return Type.ANDROID_TV;
			}
			return Type.ANDROID_PHONE;
		} else if (os.equalsIgnoreCase(SpecialKeywords.IOS)) {
			if (isTablet()) {
				return Type.IOS_TABLET;
			}
			return Type.IOS_PHONE;
		}
		throw new RuntimeException(
				"Incorrect driver type. Please, check config file.");
	}
}
