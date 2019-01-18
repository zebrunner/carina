/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.browsermobproxy;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.testng.Assert;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.NetworkUtil;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.android.recorder.utils.AdbExecutor;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;

public final class ProxyPool {
    protected static final Logger LOGGER = Logger.getLogger(ProxyPool.class);
    
    // ------------------------- BOWSERMOB PROXY ---------------------
    // TODO: investigate possibility to return interface to support JettyProxy
    /**
     * create BrowserMobProxy Server object
     * @return BrowserMobProxy
     * 
     */
    public static BrowserMobProxy createProxy() {
        BrowserMobProxyServer proxy = new BrowserMobProxyServer();
        proxy.setTrustAllServers(true);
        //System.setProperty("jsse.enableSNIExtension", "false");
        
        // disable MITM in case we do not need it
        proxy.setMitmDisabled(Configuration.getBoolean(Parameter.BROWSERMOB_MITM));
        
        return proxy;
    }
    
    public static void setupBrowserMobProxy()
    {
        if (Configuration.getBoolean(Parameter.BROWSERMOB_PROXY)) {
            BrowserMobProxy proxy = startProxy();
            
            
            Integer port = proxy.getPort();
            
            String currentIP = "";
            if (!Configuration.get(Parameter.BROWSERMOB_HOST).isEmpty()) {
            	// reuse "browsermob_host" to be able to share valid publicly available host. 
            	// it is useful when java and web tests are executed absolutely in different containers/networks. 
            	currentIP = Configuration.get(Parameter.BROWSERMOB_HOST);
            } else {
            	currentIP = NetworkUtil.getIpAddress();
            }
            
            LOGGER.warn("Set http/https proxy settings only to use with BrowserMobProxy host: " + currentIP + "; port: " + port);
            
            R.CONFIG.put("proxy_host", currentIP);
            R.CONFIG.put("proxy_port", port.toString());
            
            R.CONFIG.put("proxy_protocols", "http,https");

            // follow steps to configure https traffic sniffering: https://github.com/lightbody/browsermob-proxy#ssl-support
            // the most important are:
            // download - https://github.com/lightbody/browsermob-proxy/blob/master/browsermob-core/src/main/resources/sslSupport/ca-certificate-rsa.cer
            // import to system (for android we should use certifiate installer in system settings->security...)
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
        return startProxy(Configuration.getInt(Parameter.BROWSERMOB_PORT));
    }
    
    public static BrowserMobProxy startProxy(int proxyPort) {
        if (!Configuration.getBoolean(Parameter.BROWSERMOB_PROXY)) {
            LOGGER.debug("Proxy is disabled.");
            return null;
        }
        // integrate browserMob proxy if required here
        BrowserMobProxy proxy = null;
        long threadId = Thread.currentThread().getId();
        if (proxies.containsKey(threadId)) {
            proxy = proxies.get(threadId);
        } 
        
        // case when proxy was already instantiated but port doesn't correspond to current device
        if (null == proxy || proxy.getPort() != proxyPort) {
            proxy = ProxyPool.createProxy();
            proxies.put(Thread.currentThread().getId(), proxy);
        }
        
        if (!proxy.isStarted()) {
            LOGGER.info("Starting BrowserMob proxy...");
        	// TODO: [VD] confirmed with MB that restart was added just in case. Maybe comment/remove?
            killProcessByPort(proxyPort);
            proxy.start(proxyPort);
        } else {
            LOGGER.info("BrowserMob proxy is already started on port " + proxy.getPort());
        }

        return proxy;
    }

    // https://github.com/lightbody/browsermob-proxy/issues/264 'started' flag is not set to false after stopping BrowserMobProxyServer
    // Due to the above issue we can't control BrowserMob isRunning state and shouldn't stop it
    // TODO: investigate possibility to clean HAR files if necessary
    
    /**
     * stop BrowserMobProxy Server
     * 
     */
    public static void stopProxy() {
        long threadId = Thread.currentThread().getId();
        stopProxyByThread(threadId);
    }
    
    /**
     * Stop all proxies if possible
     */
    public static void stopAllProxies() {
        for (Long threadId : Collections.list(proxies.keys())) {
            stopProxyByThread(threadId);
        }
    }
    
    /**
     * Stop single proxy instance by id
     * @param threadId
     */
    private static void stopProxyByThread(long threadId) {
        LOGGER.debug("stopProxy starting...");
        if (proxies.containsKey(threadId)) {
            BrowserMobProxy proxy = proxies.get(threadId);
            if (proxy != null) {
                LOGGER.debug("Found registered proxy by thread: " + threadId);

                // isStarted returns true even if proxy was already stopped
                if (proxy.isStarted()) {
                    LOGGER.info("Stopping BrowserMob proxy...");
                    try {
                        proxy.stop();
                    } catch (IllegalStateException e) {
                        LOGGER.info("Seems like proxy was already stopped.");
                        LOGGER.info(e.getMessage());
                    }
                    
                } else {
                    LOGGER.info("Stopping BrowserMob proxy skipped as it is not started.");
                }
            }
            proxies.remove(threadId);
        }
        LOGGER.debug("stopProxy finished...");
    }

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
    
    /**
     * Method to kill process by port. It is used before start of new proxy instance
     * 
     * @param port
     */
    private static void killProcessByPort(int port) {
    	if (port == 0) {
    		//do nothing as it is default dynamic browsermob proxy
    		return;
    	}
        LOGGER.info(String.format("Process on port %d will be closed.", port));
        //TODO: make OS independent
        try {
        	LOGGER.info(new AdbExecutor().execute(String.format("lsof -ti :%d | xargs kill -9", port).split(" ")));
        } catch (Exception e) {
        	//do nothing
        }
    }
    
}
