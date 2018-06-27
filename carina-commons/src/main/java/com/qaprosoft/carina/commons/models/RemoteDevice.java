/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.commons.models;

import java.util.Map;

/**
 * RemoteDevice represents base device info used in Selenium Hub.
 * 
 * @author akhursevich
 */
public class RemoteDevice {
    private String name;
    private String type;
    private String os;
    private String osVersion;
    private String udid;
    private String remoteURL;
    private String vnc;
    private String proxyPort;

    public RemoteDevice(Map<String, Object> caps) {
		/*
		 * {deviceType=Phone, proxy_port=9000,
		 * server:CONFIG_UUID=24130dde-59d4-4310-95ba-6f57b9d265c3,
		 * seleniumProtocol=WebDriver, adb_port=5038,
		 * vnc=wss://stage.qaprosoft.com:7410/websockify, deviceName=Nokia_6_1,
		 * version=8.1.0, platform=ANDROID, platformVersion=8.1.0,
		 * automationName=uiautomator2, browserName=Nokia_6_1, maxInstances=1,
		 * platformName=ANDROID, udid=PL2GAR9822804910}
		 */
    	setName(caps.get("deviceName").toString());
    	setType(caps.get("deviceType").toString());
    	setOs(caps.get("platform").toString());
    	setOsVersion(caps.get("platformVersion").toString());
    	setUdid(caps.get("udid").toString());
    	
    	setVnc(caps.get("vnc").toString());
    	setProxyPort(caps.get("proxy_port").toString());
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

    public String getRemoteURL() {
        return remoteURL;
    }

    public void setRemoteURL(String remoteURL) {
        this.remoteURL = remoteURL;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getVnc() {
        return vnc;
    }

    public void setVnc(String vnc) {
        this.vnc = vnc;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }
}