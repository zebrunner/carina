package com.qaprosoft.carina.core.foundation.utils.android;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.android.recorder.utils.AdbExecutor;
import com.qaprosoft.carina.core.foundation.utils.android.recorder.utils.CmdLine;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppManager {

	private static final Logger LOGGER = Logger.getLogger(AppManager.class);

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
			Device device = DevicePool.getDevice();
			String deviceUdid = device.getUdid();
			LOGGER.info("Device udid: ".concat(deviceUdid));
			AdbExecutor executor = new AdbExecutor(
					Configuration.get(Configuration.Parameter.ADB_HOST),
					Configuration.get(Configuration.Parameter.ADB_PORT));
			String[] cmd = CmdLine.createPlatformDependentCommandLine("adb", "-s",
					deviceUdid, "shell", "pm", "list", "packages");
			LOGGER.info("Following cmd will be executed: " + Arrays.toString(cmd));
			List<String> packagesList = executor.execute(cmd);
			LOGGER.info("Found packages: ".concat(packagesList.toString()));
			String resultPackage = null;
			for (String packageStr : packagesList) {
				if (packageStr.matches(String.format(".*%s.*", name))) {
					LOGGER.info("Package was found: ".concat(packageStr));
					resultPackage = packageStr;
					break;
				}
			}
			if (null == resultPackage) {
				LOGGER.info("Package wasn't found using following name: "
						.concat(name));
				resultPackage = "not found";
			}
			packagesByName.put(name, resultPackage.replace("package:", ""));
		}
		return packagesByName.get(name);
	}

}
