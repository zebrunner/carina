package com.qaprosoft.carina.core.foundation.webdriver;

import org.openqa.selenium.WebDriver;

import com.qaprosoft.carina.core.foundation.webdriver.TestPhase.Phase;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import net.lightbody.bmp.BrowserMobProxy;

public class CarinaDriver {
	private String name;
	private WebDriver driver;
	private Device device;
	private Phase phase;
	private long threadId;
	private BrowserMobProxy proxy;
	
    public CarinaDriver(String name, WebDriver driver, Device device, Phase phase, long threadId, BrowserMobProxy proxy) {
		super();
		this.name = name;
		this.driver = driver;
		this.device = device;
		this.phase = phase;
		this.threadId = threadId;
		this.proxy = proxy;
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
	
	public BrowserMobProxy getProxy() {
		return proxy;
	}

    protected void setThreadId(long threadId) {
        this.threadId = threadId;
    }
}
