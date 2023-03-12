/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
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
package com.zebrunner.carina.webdriver;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.zebrunner.carina.webdriver.device.Device;

public class DevicePoolTest implements IDriverPool {

    private final static Device nullDevice = IDriverPool.getNullDevice();
    
    @Test()
    public void getNullDevice() {
        Assert.assertEquals(getDevice(), nullDevice, "Incorrect nullDevice has been returned");
    }
    
    @Test()
    public void getNotExistDevice() {
        Assert.assertEquals(getDevice("not-exist"), nullDevice, "Incorrect nullDevice has been returned");
    }
    
    @Test()
    public void registerDevice() {
        Assert.assertFalse(isDeviceRegistered(), "device is registered incorrectly");
        Device device = new Device("name", "type", "os", "osVersion", "udid", "remoteUrl", "vnc", "proxyPort");
        IDriverPool.registerDevice(device);
        
        Assert.assertTrue(isDeviceRegistered(), "device is registered incorrectly");
    }

    @Test()
    public void getDeviceTypePhoneAndroidTest() {
        String type = "phone";
        String os = "android";
        Device device = new Device("name", type, os, "10", "udid", "remoteUrl", "vnc", "proxyPort");
        Assert.assertTrue(device.isPhone(), "Type parameter is not phone");
        Assert.assertEquals(device.getOs(), os, "Os parameter is not valid");
    }

    @Test()
    public void getDeviceTypeTabletAndroidTest() {
        String type = "tablet";
        String os = "android";
        Device device = new Device("name", type, os, "10", "udid", "remoteUrl", "vnc", "proxyPort");
        Assert.assertTrue(device.isTablet(), "Type parameter is not tablet");
        Assert.assertEquals(device.getOs(), os, "Os parameter is not valid");
    }

    @Test()
    public void getDeviceTypeTvAndroidTest() {
        String type = "tv";
        String os = "android";
        Device device = new Device("name", type, os, "10", "udid", "remoteUrl", "vnc", "proxyPort");
        Assert.assertTrue(device.isTv(), "Type parameter is not tv");
        Assert.assertEquals(device.getOs(), os, "Os parameter is not valid");
    }

    @Test()
    public void getDeviceTypePhoneIosTest() {
        String type = "phone";
        String os = "ios";
        Device device = new Device("name", type, os, "10", "udid", "remoteUrl", "vnc", "proxyPort");
        Assert.assertTrue(device.isPhone(), "Type parameter is not phone");
        Assert.assertEquals(device.getOs(), os, "Os parameter is not valid");
    }

    @Test()
    public void getDeviceTypeTabletIosTest() {
        String type = "tablet";
        String os = "ios";
        Device device = new Device("name", type, os, "10", "udid", "remoteUrl", "vnc", "proxyPort");
        Assert.assertTrue(device.isTablet(), "Type parameter is not tablet");
        Assert.assertEquals(device.getOs(), os, "Os parameter is not valid");
    }

    @Test()
    public void getDeviceTypeTvIosTest() {
        String type = "tv";
        String os = "ios";
        Device device = new Device("name", type, os, "10", "udid", "remoteUrl", "vnc", "proxyPort");
        Assert.assertTrue(device.isTv(), "Type parameter is not tv");
        Assert.assertEquals(device.getOs(), os, "Os parameter is not valid");
    }

    @Test()
    public void getDeviceNullTest() {
        Device device = new Device("", "mobile", "android", "10", "udid", "remoteUrl", "vnc", "proxyPort");

        Assert.assertTrue(device.isNull(), "Device is not null");
    }

}
