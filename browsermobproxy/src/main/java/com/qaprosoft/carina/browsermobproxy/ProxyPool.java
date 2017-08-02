/*
 * Copyright 2013-2015 QAPROSOFT (http://qaprosoft.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qaprosoft.carina.browsermobproxy;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;

public final class ProxyPool {

	// ------------------------- BOWSERMOB PROXY ---------------------
	// TODO: investigate possibility to return interface to support JettyProxy
	/**
	 * create BrowserMobProxy Server object
	 * 
	 * @return BrowserMobProxy
	 * 
	 */
	public static BrowserMobProxy createProxy(int port) {
		BrowserMobProxyServer proxy = new BrowserMobProxyServer();
		proxy.setTrustAllServers(true);
		//System.setProperty("jsse.enableSNIExtension", "false");
		return proxy;
	}

	// https://github.com/lightbody/browsermob-proxy/issues/264 'started' flag is not set to false after stopping BrowserMobProxyServer
	// Due to the above issue we can't control BrowserMob isRunning state and shouldn't stop it
	// TODO: investigate possibility to clean HAR files if necessary
	
	/**
	 * stop BrowserMobProxy Server
	 * 
	 */
	/*
	public static void stopProxy() {
		long threadId = Thread.currentThread().getId();

		LOGGER.debug("stopProxy starting...");
		if (proxies.containsKey(threadId)) {
			BrowserMobProxy proxy = proxies.get(threadId);
			if (proxy != null) {
				LOGGER.debug("Found registered proxy by thread: " + threadId);

				if (proxy.isStarted()) {
					LOGGER.info("Stopping BrowserMob proxy...");
					proxy.stop();
				} else {
					LOGGER.info("Stopping BrowserMob proxy skipped as it is not started.");
				}
			}
			proxies.remove(threadId);
		}
		LOGGER.debug("stopProxy finished...");
	}*/


}
