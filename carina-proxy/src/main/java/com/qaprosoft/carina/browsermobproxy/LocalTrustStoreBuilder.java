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
package com.qaprosoft.carina.browsermobproxy;

import java.io.File;
import java.io.FileInputStream;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

public class LocalTrustStoreBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String TC_CONF_DIR_PATH = "keysecure/";

    public static final String TRUSTSTORE_FILE = "truststore.jks";
    public static final String TRUSTSTORE_PASSWORD = "changeit";

    private final File tlsConfigDirectory;

    /**
     * Initializes builder with specific path to tls keysecure files
     *
     * @param path
     *            - relative path to keysecure folder
     */
    public LocalTrustStoreBuilder(String path) {
        this.tlsConfigDirectory = getTlsConfigDirectoryByPath(path);
        LOGGER.info("Found tlsConfigDirectory=" + tlsConfigDirectory.getPath());
    }

    /**
     * Initializes builder using classpath (priority 1) or Parameter.TLS_KEYSECURE_LOCATION value (priority 2) as source
     * for tls keysecure files
     *
     */
    public LocalTrustStoreBuilder() {
        this.tlsConfigDirectory = findTlsConfigDirectory();
        LOGGER.info("Found tlsConfigDirectory=" + tlsConfigDirectory.getPath());
    }

    private File getTlsConfigDirectoryByPath(String path) {
        File directory = new File(path);
        if (directory != null && directory.exists()) {
            LOGGER.info("Directory exists: " + directory.getAbsolutePath());
            return directory;
        } else {
            throw new RuntimeException("Directory doesn't exist: " + directory.getAbsolutePath());
        }
    }

    /*
     * Do note that we only check for one file, and assume that if we find that file, rest of the TLS files will be in
     * the same directory. This may break in corner cases and throw runtime exception which is acceptable as of now.
     */
    public File findTlsConfigDirectory() {
        // Priority 1: Searching in classpath
        URL url = ClassLoader.getSystemResource(TC_CONF_DIR_PATH);
        if (url != null) {
            try {
                return new File(url.toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        // Priority 2: Searching by Parameter.TLS_KEYSECURE_LOCATION path
        if (!Configuration.isNull(Parameter.TLS_KEYSECURE_LOCATION)) {
            return new File(Configuration.get(Parameter.TLS_KEYSECURE_LOCATION));
        }

        throw new RuntimeException("TLS files directory does not exist anywhere. Please check your configuration");
    }

    /**
     * Create an SSLContext with mutual TLS authentication enabled; returns null if the
     * tlsConfigDirectory was not found.
     *
     * @return SSLContext
     */
    public SSLContext createSSLContext() {
        if (tlsConfigDirectory == null) {
            return null;
        }

        try {
            // Get the client's trustStore for what server certificates the client will trust
            TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore trustStore = createTrustStore();
            trustFactory.init(trustStore);

            // Create SSL context with the client's keyStore and trustStore
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, trustFactory.getTrustManagers(), null);
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates the client's trustStore; returns null if the tlsConfigDirectory was not found.
     *
     * @return KeyStore
     */
    public KeyStore createTrustStore() {
        if (tlsConfigDirectory == null) {
            return null;
        } else {
            try {
                return readTrustStore(new File(tlsConfigDirectory, TRUSTSTORE_FILE), TRUSTSTORE_PASSWORD.toCharArray());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private KeyStore readTrustStore(File trustStoreFile, char[] password) {
        try {
            KeyStore keyStore = KeyStore.getInstance("jks");
            keyStore.load(new FileInputStream(trustStoreFile), password);
            return keyStore;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
