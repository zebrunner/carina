package com.qaprosoft.carina.core.foundation.webdriver;

import org.openqa.selenium.WebDriver;

import com.qaprosoft.carina.core.foundation.webdriver.TestPhase.Phase;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;

public class CarinaDriver {
	private String name;
	private WebDriver driver;
	private Phase phase;
	private long threadId;
	private Device device;
	private boolean isExpired;
	
	public CarinaDriver(String name, WebDriver driver, Phase phase, long threadId, Device device) {
		super();
		this.name = name;
		this.driver = driver;
		this.phase = phase;
		this.threadId = threadId;
		this.device = device;
		this.isExpired = false;
	}

	public WebDriver getDriver() {
		return driver;
	}

	public void setDriver(WebDriver driver) {
		this.driver = driver;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getThreadId() {
		return threadId;
	}

	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}

	public Phase getPhase() {
		return phase;
	}

	public void setPhase(Phase phase) {
		this.phase = phase;
	}
	
	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public boolean isExpired() {
		return isExpired;
	}

	public void setExpired(boolean isExpired) {
		this.isExpired = isExpired;
	}
}
