package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;

public class MobilePoolCapabilities extends MobileCapabilies {


    public DesiredCapabilities getCapability(String testName) {
        return getCapability(false, null);
    }

    public DesiredCapabilities getCapability(String testName, Device device) {
        return getCapability(false, device);
    }

    public DesiredCapabilities getCapability(boolean isGrid, Device device) {
    	
        // 1. parse mobile_devices
        // 2. verify status for each selenium/appium server
        // 3. !adjust thread count if possible or organize delays when all devices are busy
        // 4. create driver using any free device
    	String mobileAppPath = Configuration.get(Parameter.MOBILE_APP);
    	String platform = Configuration.get(Configuration.Parameter.MOBILE_PLATFORM_NAME);
    	String platformVersion = Configuration.get(Configuration.Parameter.MOBILE_PLATFORM_VERSION);
    	String deviceName = Configuration.get(Configuration.Parameter.MOBILE_DEVICE_NAME);
        
    	if (device != null) {
    		platform = device.getOs();
    		platformVersion = device.getOsVersion();
    		deviceName = device.getName();
    	}
		if (!mobileAppPath.isEmpty()
				&& (Configuration.getInt(Parameter.THREAD_COUNT) > 1
						|| Configuration.getInt(Parameter.DATA_PROVIDER_THREAD_COUNT) > 1)
				&& Configuration.get(Parameter.MOBILE_PLATFORM_NAME).equalsIgnoreCase(SpecialKeywords.ANDROID)) {
            //[VD] workaround to the issue with multiply calls to the single apk files
            File mobileAppFile = new File(Configuration.get(Parameter.MOBILE_APP));
            File appTempFile = new File(ReportContext.getTempDir().getAbsolutePath() + File.separator + device.getUdid() + "-" + mobileAppFile.getName());
            
            if (!appTempFile.exists()) {
            	LOGGER.info("Temporary copy of the mobile app '" + appTempFile.getAbsolutePath() + "' file doesn't exist and will be created...");
            	try {
            		
                    if (mobileAppFile.isDirectory()) {
                    	LOGGER.info(appTempFile.getName() + " will be copied as directory...");
							FileUtils.copyDirectory(mobileAppFile, appTempFile);
                    } else {
                    	LOGGER.info(appTempFile.getName() + " will be copied as file...");
                    	FileUtils.copyFile(mobileAppFile, appTempFile);
                    }
				} catch (IOException e) {
					LOGGER.error("IoException during apk copying...", e);
				}
            }
            mobileAppPath = appTempFile.getAbsolutePath();
        }
        
		DesiredCapabilities capabilities = getMobileCapabilities(isGrid, platform, platformVersion, deviceName,
				Configuration.get(Configuration.Parameter.MOBILE_AUTOMATION_NAME),
				Configuration.get(Configuration.Parameter.MOBILE_NEW_COMMAND_TIMEOUT), null, mobileAppPath,
				Configuration.get(Configuration.Parameter.MOBILE_APP_ACTIVITY),
				Configuration.get(Configuration.Parameter.MOBILE_APP_PACKAGE));
        return capabilities;
    }
}
