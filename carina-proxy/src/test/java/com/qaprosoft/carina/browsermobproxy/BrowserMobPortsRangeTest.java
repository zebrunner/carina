/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.browsermobproxy;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.proxy.SystemProxy;

import net.lightbody.bmp.BrowserMobProxy;

public class BrowserMobPortsRangeTest {
    private static final Logger LOGGER = Logger.getLogger(BrowserMobPortsRangeTest.class);
    private static String header = "my_header";
    private static String headerValue = "my_value";

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        R.CONFIG.put("core_log_level", "DEBUG");
        R.CONFIG.put("browsermob_proxy", "true");
        R.CONFIG.put("proxy_set_to_system", "false");
        R.CONFIG.put("browsermob_port", "NULL");
        R.CONFIG.put("browsermob_ports_range", "0:0");
        R.CONFIG.put("browsermob_disabled_mitm", "false");
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        ProxyPool.stopAllProxies();
    }

    @Test
    public void testPortsRange() {
        initialize();
        Assert.assertTrue(ProxyPool.getProxy().isStarted(), "BrowserMobProxy is not started!");
    }

    private void initialize() {
        ProxyPool.setupBrowserMobProxy();
        SystemProxy.setupProxy();

        BrowserMobProxy proxy = ProxyPool.getProxy();
        proxy.addHeader(header, headerValue);
    }

}
