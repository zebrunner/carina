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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.R;

import net.lightbody.bmp.BrowserMobProxy;

public class BrowserMobTest {
	protected static final Logger LOGGER = Logger.getLogger(BrowserMobTest.class);

	@BeforeClass(alwaysRun = true)
	public void beforeClass() {
		R.CONFIG.put("core_log_level", "DEBUG");
		R.CONFIG.put("browsermob_proxy", "true");
	}

	@Test
	public void testCreateProxy() {
		BrowserMobProxy proxy = ProxyPool.createProxy();
		Assert.assertTrue(proxy != null, "BrowserMobProxy is not created!");
	}

	@Test
	public void testSetupBrowserMobProxy() {
		ProxyPool.setupBrowserMobProxy();

		Assert.assertTrue(Integer.parseInt(R.CONFIG.get("proxy_port")) > 0, "BrowserMobProxy port is incorrect!");
		Assert.assertTrue(R.CONFIG.get("proxy_host").length() > 0, "BrowserMobProxy host is incorrect!");
		Assert.assertTrue(R.CONFIG.get("proxy_protocols").equals("http,https"),
				"BrowserMobProxy protocol is incorrect!");
	}
	
	@Test
	public void testStartBrowserMobProxy() {
		BrowserMobProxy proxy = ProxyPool.startProxy();
		Assert.assertTrue(proxy.isStarted(), "BrowserMobProxy is not started!");
	}
	
	@Test
	public void testStartBrowserMobProxyOnPort() {
		BrowserMobProxy proxy = ProxyPool.startProxy(7777);
		Assert.assertTrue(proxy.isStarted(), "BrowserMobProxy is not started!");
		Assert.assertTrue(proxy.getPort() == 7777, "BrowserMobProxy port is incorrect!");
		proxy.stop();
	}
	
	// TODO: Uncomment this test when stopProxy functionality will be fixed
	@Test
	public void testStopBrowserMobProxy() {
		BrowserMobProxy proxy= ProxyPool.startProxy();
		ProxyPool.stopProxy(proxy);
		Assert.assertTrue(!proxy.isStarted(), "BrowserMobProxy is not stopped!!");
	}
	
}
