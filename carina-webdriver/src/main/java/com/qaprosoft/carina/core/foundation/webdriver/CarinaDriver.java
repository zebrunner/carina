package com.qaprosoft.carina.core.foundation.webdriver;

import org.openqa.selenium.WebDriver;

import com.qaprosoft.carina.core.foundation.webdriver.TestPhase.Phase;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;

public class CarinaDriver {
	private String name;
	private WebDriver driver;
	private Device device;
	private Phase phase;
	private long threadId;
	
    public CarinaDriver(String name, WebDriver driver, Device device, Phase phase, long threadId) {
		super();
		this.name = name;
		this.driver = driver;
		this.device = device;
		this.phase = phase;
		this.threadId = threadId;
	}

	public WebDriver getDriver() {
		return driver;
	}

    public Device getDevice() {
        return device;
    }

	public String getName() {
		return name;
	}

	public long getThreadId() {
		return threadId;
	}

	public Phase getPhase() {
		return phase;
	}

    protected void setThreadId(long threadId) {
        this.threadId = threadId;
    }
}
