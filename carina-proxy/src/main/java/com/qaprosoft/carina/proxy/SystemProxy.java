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
package com.qaprosoft.carina.proxy;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

public class SystemProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void setupProxy() {
        String proxyHost = Configuration.get(Parameter.PROXY_HOST);
        String proxyPort = Configuration.get(Parameter.PROXY_PORT);
        String noProxy = Configuration.get(Parameter.NO_PROXY);

        List<String> protocols = Arrays.asList(Configuration.get(Parameter.PROXY_PROTOCOLS).split("[\\s,]+"));

        //TODO: test removal comparing with null
        if (proxyHost != null && !proxyHost.isEmpty() && proxyPort != null && !proxyPort.isEmpty()
                && Configuration.getBoolean(Parameter.PROXY_SET_TO_SYSTEM)) {
            if (protocols.contains("http")) {
                LOGGER.info(String.format("HTTP client will use http: %s:%s", proxyHost, proxyPort));

                System.setProperty("http.proxyHost", proxyHost);
                System.setProperty("http.proxyPort", proxyPort);
                
                if (!noProxy.isEmpty()) {
                    System.setProperty("http.nonProxyHosts", proxyPort);
                }
            }

            if (protocols.contains("https")) {
                LOGGER.info(String.format("HTTP client will use https proxies: %s:%s", proxyHost, proxyPort));

                System.setProperty("https.proxyHost", proxyHost);
                System.setProperty("https.proxyPort", proxyPort);
                
                if (!noProxy.isEmpty()) {
                    System.setProperty("https.nonProxyHosts", proxyPort);
                }
            }

            if (protocols.contains("ftp")) {
                LOGGER.info(String.format("HTTP client will use ftp proxies: %s:%s", proxyHost, proxyPort));

                System.setProperty("ftp.proxyHost", proxyHost);
                System.setProperty("ftp.proxyPort", proxyPort);
                
                if (!noProxy.isEmpty()) {
                    System.setProperty("ftp.nonProxyHosts", proxyPort);
                }
            }

            if (protocols.contains("socks")) {
                LOGGER.info(String.format("HTTP client will use socks proxies: %s:%s", proxyHost, proxyPort));
                System.setProperty("socksProxyHost", proxyHost);
                System.setProperty("socksProxyPort", proxyPort);
                
                /*
                 * http://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html
                 * Once a SOCKS proxy is specified in this manner, all TCP connections will be attempted through the proxy.
                 * i.e. There is no provision for setting non-proxy hosts via the socks properties.
                 */
            }
            
        }
    }

    public static boolean isProxyAlive(String host, Integer port, Integer timeoutMillis) {
        boolean isHostReachable = false;

        if (port == null || port < 0) {
            throw new RuntimeException("Port is not valid - cannot be less than 0 or be null");
        }

        if (timeoutMillis == null || timeoutMillis < 0) {
            throw new RuntimeException("Timeout is not valid -  cannot be less than 0 or be null");
        }

        try {
            InetAddress proxyHostAddress = InetAddress.getByName(String.format("%s:%d", host, port));
            isHostReachable = proxyHostAddress.isReachable(timeoutMillis);

        } catch (UnknownHostException e) {
            throw new RuntimeException("Invalid host", e);
        } catch (IOException e) {
            // do nothing
        }
        return isHostReachable;
    }

    public static boolean isProxyAvailable(String proxyHost, Integer proxyPort, Integer timeoutMillis, Proxy.Type protocol,
            URL hostCheck) {
        if (!isProxyAlive(proxyHost, proxyPort, timeoutMillis)) {
            LOGGER.warn("Proxy is not alive");
            return false;
        }
        boolean isProxyAvailable = false;
        SocketAddress socketAddress = new InetSocketAddress(proxyHost, proxyPort);
        Proxy proxy = new Proxy(protocol, socketAddress);

        try {
            URLConnection conn = hostCheck.openConnection(proxy);
            conn.connect();
            isProxyAvailable = true;
        } catch (IOException e) {
            isProxyAvailable = false;
        }
        return isProxyAvailable;
    }

}
