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
package com.qaprosoft.carina.browserupproxy;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.BrowserUpProxyServer;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.NetworkUtil;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.android.recorder.utils.AdbExecutor;
import com.qaprosoft.carina.core.foundation.utils.common.CommonUtils;

public final class ProxyPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final ConcurrentHashMap<Long, Integer> PROXY_PORTS_BY_THREAD = new ConcurrentHashMap<>();
    // map for storing of available ports range and their availability
    private static final ConcurrentHashMap<Integer, Boolean> PROXY_PORTS_FROM_RANGE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, BrowserUpProxy> PROXIES = new ConcurrentHashMap<>();

	static {
        initProxyPortsRange();
    }

	public static void initProxyPortsRange() {
		if (!Configuration.get(Parameter.BROWSERUP_PORTS_RANGE).isEmpty()) {
			try {
				String[] ports = Configuration.get(Parameter.BROWSERUP_PORTS_RANGE).split(":");
				for (int i = Integer.valueOf(ports[0]); i <= Integer.valueOf(ports[1]); i++) {
					PROXY_PORTS_FROM_RANGE.put(i, true);
				}
			} catch (Exception e) {
				throw new RuntimeException("Please specify BROWSERUP_PORTS_RANGE in format 'port_from:port_to'");
			}
		}
	}
    
    // ------------------------- BROWSERUP PROXY ---------------------
    // TODO: investigate possibility to return interface to support JettyProxy
    /**
     * create BrowserUpProxy Server object
     * @return BrowserUpProxy
     * 
     */
    public static BrowserUpProxy createProxy() {
        BrowserUpProxy proxy = new BrowserUpProxyServer();
        proxy.setTrustAllServers(true);
        //System.setProperty("jsse.enableSNIExtension", "false");
        // disable MITM in case we do not need it
        proxy.setMitmDisabled(Configuration.getBoolean(Parameter.BROWSERUP_MITM));
        return proxy;
    }
    
    public static void setupBrowserUpProxy()
    {
        if (Configuration.getBoolean(Parameter.BROWSERUP_PROXY)) {
            long threadId = Thread.currentThread().getId();
            BrowserUpProxy proxy = startProxy();

            Integer port = proxy.getPort();
            PROXY_PORTS_BY_THREAD.put(threadId, port);
            
            // reuse "proxy_host" to be able to share valid publicly available host. 
            // it is useful when java and web tests are executed absolutely in different containers/networks. 
            if (Configuration.get(Parameter.PROXY_HOST).isEmpty()) {
            	String currentIP = NetworkUtil.getIpAddress();
            	R.CONFIG.put(Parameter.PROXY_HOST.getKey(), currentIP);
            }

            LOGGER.warn("Set http/https proxy settings only to use with BrowserUpProxy host: {}; port: {}", Configuration.get(Parameter.PROXY_HOST),
                    +PROXY_PORTS_BY_THREAD.get(threadId));

            R.CONFIG.put(Parameter.PROXY_PORT.getKey(), port.toString());
            R.CONFIG.put("proxy_protocols", "http,https");

            // follow steps to configure https traffic sniffering: https://github.com/browserup/browserup-proxy?#ssl-support
            // the most important are:
            // download - https://github.com/browserup/browserup-proxy/blob/master/browserup-proxy-core/src/main/resources/sslSupport/ca-certificate-rsa.cer
            // import to system (for android we should use certificate installer in system settings->security...)
        }
    }

    // https://github.com/lightbody/browsermob-proxy/issues/264 'started' flag is not set to false after stopping BrowserMobProxyServer
    // Due to the above issue we can't control BrowserMob isRunning state and shouldn't stop it
    // TODO: investigate possibility to clean HAR files if necessary
    // todo investigate is it work in browserup proxy


    /**
     * stop BrowserUpProxy Server
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
    

    /**
     * Checking whether BROWSERUP_PORT is declared. then it will be used as port for browserup proxy
     * Otherwise first available port from BROWSERUP_PORTS_RANGE will be used
     * 
     * @return port
     */
	public static int getProxyPortFromConfig() {
		if (!Configuration.get(Parameter.BROWSERUP_PORT).isEmpty())
			return Configuration.getInt(Parameter.BROWSERUP_PORT);
		else if (!Configuration.get(Parameter.BROWSERUP_PORTS_RANGE).isEmpty()) {
			for (Map.Entry<Integer, Boolean> pair : PROXY_PORTS_FROM_RANGE.entrySet()) {
				if (pair.getValue()) {
					LOGGER.info("Making BrowserUp proxy port busy: {}", pair.getKey());
					pair.setValue(false);
					return pair.getKey();
				}
			}
			throw new RuntimeException("All ports from Parameter.BROWSERUP_PORTS_RANGE are currently busy. Please change execution thread count");
		}
		throw new RuntimeException("Neither Parameter.BROWSERUP_PORT nor Parameter.BROWSERUP_PORTS_RANGE are specified!");
	}

    // TODO: investigate possibility to return interface to support JettyProxy
    /**
     * start BrowserUpProxy Server
     * 
     * @return BrowserUpProxy
     * 
     */
    public static synchronized BrowserUpProxy startProxy() {
        return startProxy(getProxyPortFromConfig());
    }
    
    public static synchronized BrowserUpProxy startProxy(int proxyPort) {
        if (!Configuration.getBoolean(Parameter.BROWSERUP_PROXY)) {
            LOGGER.debug("Proxy is disabled.");
            return null;
        }
        // integrate browserUp proxy if required here
        BrowserUpProxy proxy = null;
        long threadId = Thread.currentThread().getId();
        if (PROXIES.containsKey(threadId)) {
            proxy = PROXIES.get(threadId);
        }

        if (PROXY_PORTS_BY_THREAD.containsKey(threadId)) {
            proxyPort = PROXY_PORTS_BY_THREAD.get(threadId);
        }

        // case when proxy was already instantiated but port doesn't correspond to current device
        if (null == proxy || proxy.getPort() != proxyPort) {
            proxy = ProxyPool.createProxy();
            PROXIES.put(Thread.currentThread().getId(), proxy);
        }
        
        if (!proxy.isStarted()) {
            LOGGER.info("Starting BrowserUp proxy...");
        	// TODO: [VD] confirmed with MB that restart was added just in case. Maybe comment/remove?
            killProcessByPort(proxyPort);
            proxy.start(proxyPort);
        } else {
            LOGGER.info("BrowserUp proxy is already started on port {}", proxy.getPort());
        }

        return proxy;
    }
    
    private static void setProxyPortToAvailable(long threadId) {
		if (PROXY_PORTS_BY_THREAD.get(threadId) != null) {
			if (PROXY_PORTS_FROM_RANGE.get(PROXY_PORTS_BY_THREAD.get(threadId)) != null) {
				LOGGER.info("Setting BrowserUp proxy port {} to available state", PROXY_PORTS_BY_THREAD.get(threadId));
				PROXY_PORTS_FROM_RANGE.put(PROXY_PORTS_BY_THREAD.get(threadId), true);
				PROXY_PORTS_BY_THREAD.remove(threadId);
			}
		}
    }

    // https://github.com/lightbody/browsermob-proxy/issues/264 'started' flag is not set to false after stopping BrowserMobProxyServer
    // Due to the above issue we can't control BrowserMob isRunning state and shouldn't stop it
    // TODO: investigate possibility to clean HAR files if necessary
    
    /**
     * stop BrowserUpProxy Server
     * 
     */
    public static void stopProxy() {
        stopProxyByThread(Thread.currentThread().getId());
    }
    
    /**
     * Stop all proxies if possible
     */
    public static void stopAllProxies() {
        for (Long threadId : Collections.list(PROXIES.keys())) {
            stopProxyByThread(threadId);
        }
    }
    
    /**
     * Stop single proxy instance by id
     * @param threadId long
     */
    private static void stopProxyByThread(long threadId) {
        if (PROXIES.containsKey(threadId)) {
            setProxyPortToAvailable(threadId);
            BrowserUpProxy proxy = PROXIES.get(threadId);
            if (proxy != null) {
                LOGGER.debug("Found registered proxy by thread: {}", threadId);

                // isStarted returns true even if proxy was already stopped
                if (proxy.isStarted()) {
                    try {
                        LOGGER.debug("stopProxy starting...");                        
                        proxy.stop();
                    } catch (IllegalStateException e) {
                        LOGGER.info("Seems like proxy was already stopped.");
                        LOGGER.info(e.getMessage());
                    } finally {
                        LOGGER.debug("stopProxy finished...");
                    }
                }
            }
            PROXIES.remove(threadId);
        }
    }

    /**
     * get registered BrowserUpProxy Server
     * 
     * @return BrowserMobProxy
     * 
     */
    public static BrowserUpProxy getProxy() {
        BrowserUpProxy proxy = null;
        long threadId = Thread.currentThread().getId();
        if (PROXIES.containsKey(threadId)) {
            proxy = PROXIES.get(threadId);
        } else {
            Assert.fail("There is not a registered BrowserUpProxy for thread: " + threadId);
        }
        return proxy;
    }

    public static int getProxyPortFromThread() {
        int port = 0;
        long threadId = Thread.currentThread().getId();
        if (PROXY_PORTS_BY_THREAD.containsKey(threadId)) {
            port = PROXY_PORTS_BY_THREAD.get(threadId);
        } else {
            Assert.fail("This is not a register BrowserUpProxy Port for thread: " + threadId);
        }
        return port;
    }
    
    /**
     * return true if proxy is already registered
     * 
     * @return boolean
     * 
     */
    public static boolean isProxyRegistered() {
        long threadId = Thread.currentThread().getId();
        return PROXIES.containsKey(threadId);
    }

    /**
     * register custom BrowserUpProxy Server
     * 
     * @param proxy custom BrowserMobProxy
     * 
     */
    public static void registerProxy(BrowserUpProxy proxy) {
        long threadId = Thread.currentThread().getId();
        if (PROXIES.containsKey(threadId)) {
            LOGGER.warn("Existing proxy is detected and will be overwritten");
            // No sense to stop as it is not supported
            PROXIES.remove(threadId);
        }
        if (PROXY_PORTS_BY_THREAD.containsKey(threadId)) {
            LOGGER.warn("Existing proxyPort is detected and will be overwritten");
            PROXY_PORTS_BY_THREAD.remove(threadId);
        }
        
        LOGGER.info("Register custom proxy with thread: {}", threadId);
        PROXIES.put(threadId, proxy);
    }
    
    /**
     * Method to kill process by port. It is used before start of new proxy instance
     * 
     * @param port int
     */
    private static void killProcessByPort(int port) {
        if (port == 0) {
            //do nothing as it is default dynamic browsermob proxy
            return;
        }
        LOGGER.info(String.format("Process on port %d will be closed.", port));

        //TODO: make OS independent or remove completely
        try {
            List<?> output = new AdbExecutor().execute(String.format("lsof -ti :%d", port).split(" "));
            LOGGER.debug("proxy process before kill: " + StringUtils.join(output, ""));
            
            output = new AdbExecutor().execute(String.format("lsof -ti :%d | xargs kill -9", port).split(" "));
            LOGGER.debug("proxy process kill output: " + StringUtils.join(output, ""));
            
            output = new AdbExecutor().execute(String.format("lsof -ti :%d", port).split(" "));
            LOGGER.debug("proxy process after kill: " + StringUtils.join(output, ""));
            
            CommonUtils.pause(1);
            
            output = new AdbExecutor().execute(String.format("lsof -ti :%d", port).split(" "));
            LOGGER.debug("proxy process after kill and 2 sec pause: " + StringUtils.join(output, ""));
            
        } catch (Exception e) {
            LOGGER.error("Unable to kill process by lsof utility!", e);
        }
    }
}
