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

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.testng.Assert;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.NetworkUtil;
import com.qaprosoft.carina.core.foundation.utils.R;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;

public final class ProxyPool {

	protected static final Logger LOGGER = Logger.getLogger(ProxyPool.class);
	
	// ------------------------- BOWSERMOB PROXY ---------------------
	// TODO: investigate possibility to return interface to support JettyProxy
	/**
	 * create BrowserMobProxy Server object
	 * @param port number 
	 * @return BrowserMobProxy
	 * 
	 */
	public static BrowserMobProxy createProxy(int port) {
		BrowserMobProxyServer proxy = new BrowserMobProxyServer();
		proxy.setTrustAllServers(true);
		//System.setProperty("jsse.enableSNIExtension", "false");
		return proxy;
	}
	
	public static void setupBrowserMobProxy()
	{
		if (Configuration.getBoolean(Parameter.BROWSERMOB_PROXY)) {
			BrowserMobProxy proxy = startProxy();
			Integer port = proxy.getPort();

			String currentIP = NetworkUtil.getIpAddress();
			LOGGER.debug("Set http proxy settings to use BrowserMobProxy host: " + currentIP + "; port: " + port);
			
			R.CONFIG.put("proxy_host", currentIP);
			R.CONFIG.put("proxy_port", port.toString());
			R.CONFIG.put("proxy_protocols", "http");
			
		}
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
	
	// ------------------------- BOWSERMOB PROXY ---------------------
	
	private static final ConcurrentHashMap<Long, BrowserMobProxy> proxies = new ConcurrentHashMap<Long, BrowserMobProxy>();
	
	// TODO: investigate possibility to return interface to support JettyProxy
	/**
	 * start BrowserMobProxy Server
	 * 
	 * @return BrowserMobProxy
	 * 
	 */
	public static BrowserMobProxy startProxy() {
		if (!Configuration.getBoolean(Parameter.BROWSERMOB_PROXY)) {
			return null;
		}
		// integrate browserMob proxy if required here
		BrowserMobProxy proxy = null;
		long threadId = Thread.currentThread().getId();
		if (proxies.containsKey(threadId)) {
			proxy = proxies.get(threadId);
		} else {
			proxy = ProxyPool.createProxy(Configuration.getInt(Parameter.BROWSERMOB_PORT));
			proxies.put(Thread.currentThread().getId(), proxy);
		}
		
		if (!proxy.isStarted()) {
			LOGGER.info("Starting BrowserMob proxy...");
			proxy.start(Configuration.getInt(Parameter.BROWSERMOB_PORT));
		} else {
			LOGGER.info("BrowserMob proxy is already started on port " + proxy.getPort());
		}

		Integer port = proxy.getPort();

		String currentIP = NetworkUtil.getIpAddress();
		LOGGER.warn("Set http/https proxy settings ONLY to use with BrowserMobProxy host: " + currentIP + "; port: " + port);

		//TODO: double check mobile proxy support
		R.CONFIG.put("proxy_host", currentIP);
		R.CONFIG.put("proxy_port", port.toString());
		R.CONFIG.put("proxy_protocols", "http,https");

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

	/**
	 * get registered BrowserMobProxy Server
	 * 
	 * @return BrowserMobProxy
	 * 
	 */
	public static BrowserMobProxy getProxy() {
		BrowserMobProxy proxy = null;
		long threadId = Thread.currentThread().getId();
		if (proxies.containsKey(threadId)) {
			proxy = proxies.get(threadId);
		} else {
			Assert.fail("There is not registered BrowserMobProxy for thread: " + threadId);
		}
		return proxy;
	}
	
	/**
	 * return true if proxy is already registered
	 * 
	 * @return boolean
	 * 
	 */
	public static boolean isProxyRegistered() {
		long threadId = Thread.currentThread().getId();
		return proxies.containsKey(threadId);
	}

	/**
	 * register custom BrowserMobProxy Server
	 * 
	 * @param proxy
	 *            custom BrowserMobProxy
	 * 
	 */
	public static void registerProxy(BrowserMobProxy proxy) {
		long threadId = Thread.currentThread().getId();
		if (proxies.containsKey(threadId)) {
			LOGGER.warn("Existing proxy is detected and will be overriten");
			// No sense to stop as it is not supported
			proxies.remove(threadId);
		}
		
		LOGGER.info("Register custom proxy with thread: " + threadId);
		proxies.put(threadId, proxy);
	}


}
