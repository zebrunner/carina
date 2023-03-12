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

/**
 * Created by Quang Le (quangltp) on 11/12/20
 */

public class ScreenshotTest {

    @Test
    public void testIsCaptured() {
        String message = "An unknown server-side error occurred while processing the command. Original error: Could not proxy. Proxy error: Could not proxy command to the remote server. Original error: read ECONNRESET\n" +
                "Build info: version: '3.141.59', revision: 'e82be7d358', time: '2018-11-14T08:17:03'\n" +
                "System info: host: 'Defaultstring', ip: '127.0.0.1', os.name: 'Linux', os.arch: 'amd64', os.version: '5.4.0-52-generic', java.version: '1.8.0_272'\n" +
                "Driver info: io.appium.java_client.android.AndroidDriver\n" +
                "Capabilities {adbExecTimeout: 60000, appActivity: com.ltpquang.myapp.ui.activ..., appPackage: com.ltpquang.myapp, automationName: uiAutomator2, databaseEnabled: false, desired: {adbExecTimeout: 60000, appActivity: com.ltpquang.myapp.ui.activ..., appPackage: com.ltpquang.myapp, automationName: uiAutomator2, fullReset: false, newCommandTimeout: 0, noReset: true, platformName: android, udid: 271cb07cbf217ece}, deviceApiLevel: 28, deviceManufacturer: samsung, deviceModel: SM-N960N, deviceName: 271cb07ck3nfh8ce, deviceScreenDensity: 420, deviceScreenSize: 1080x2220, deviceUDID: 271cb07ck3nfh8ce, fullReset: false, javascriptEnabled: true, locationContextEnabled: false, networkConnectionEnabled: true, newCommandTimeout: 0, noReset: true, pixelRatio: 2.625, platform: LINUX, platformName: Android, platformVersion: 9, statBarHeight: 63, takesScreenshot: true, udid: 271cb07cbf217ece, viewportRect: {height: 2118, left: 0, top: 63, width: 1080}, warnings: {}, webStorageEnabled: false}\n" +
                "Session ID: 7ec50037-37b7-41a6-a6f2-57c56ecfc425";
        Assert.assertFalse(Screenshot.isCaptured(message));
    }
}