package com.qaprosoft.carina.core.foundation.utils.android;

import java.util.HashMap;
import java.util.Map;

import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;

public class AppManager {

	private Map<String, String> packagesByName = new HashMap<String, String>();
	
	private static AppManager instance;

	private AppManager() {
	}

	public static AppManager getInstance() {
		if (instance == null) {
			synchronized (AppManager.class) {
				if (instance == null) {
					instance = new AppManager();
				}
			}
		}
		return instance;
	}

	public String getFullPackageByName(final String name) {
		if (!packagesByName.containsKey(name)) {
			String resultPackage = DevicePool.getDevice().getFullPackageByName(name);
			packagesByName.put(name, resultPackage.replace("package:", ""));
		}
		return packagesByName.get(name);
	}

}
