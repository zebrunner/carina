/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation.api.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

public class SSLContextBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String TC_CONF_DIR_PATH = "keysecure/";

    public static final String TRUSTSTORE_FILE = "truststore.jks";
    public static final String TRUSTSTORE_PASSWORD = "changeit";
    public static final String KEYSTORE_FILE = "tls.jks";
    public static final String KEYSTORE_PASSWORD_FILE = "tls.properties";

    private final File tlsConfigDirectory;
    private final boolean isClientAuthEnabled;

    /**
     * Initializes builder with specific path to tls keysecure files
     *
     * @param path
     *            - relative path to keysecure folder
     * @param isClientAuthEnabled
     *            - is client auth enabled or not for required SSL context
     */
    public SSLContextBuilder(String path, boolean isClientAuthEnabled) {
        this.tlsConfigDirectory = getTlsConfigDirectoryByPath(path);
        this.isClientAuthEnabled = isClientAuthEnabled;
        LOGGER.info("Found tlsConfigDirectory=" + tlsConfigDirectory.getPath() + ", isClientAuthEnabled=" + isClientAuthEnabled);
    }

    /**
     * Initializes builder using classpath (priority 1) or Parameter.TLS_KEYSECURE_LOCATION value (priority 2) as source
     * for tls keysecure files
     *
     * @param isClientAuthEnabled
     *            - is client auth enabled or not for required SSL context
     */
    public SSLContextBuilder(boolean isClientAuthEnabled) {
        this.tlsConfigDirectory = findTlsConfigDirectory();
        this.isClientAuthEnabled = isClientAuthEnabled;
        LOGGER.info("Found tlsConfigDirectory=" + tlsConfigDirectory.getPath() + ", isClientAuthEnabled=" + isClientAuthEnabled);
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
    private File findTlsConfigDirectory() {
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
            // Get the client's public/private key pair
            KeyManagerFactory kmf = null;
            if (this.isClientAuthEnabled) {
                kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(createPrivateKeyStore(), readKeyStorePassword(tlsConfigDirectory));
            }
            // Get the client's trustStore for what server certificates the client will trust
            TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustFactory.init(createTrustStore());

            // Create SSL context with the client's keyStore and trustStore
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init((this.isClientAuthEnabled) ? kmf.getKeyManagers() : null, trustFactory.getTrustManagers(), null);
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
                return readKeyStore(new File(tlsConfigDirectory, TRUSTSTORE_FILE), TRUSTSTORE_PASSWORD.toCharArray());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Creates the client's keyStore with private and public keys; returns null if the tlsConfigDirectory was not found.
     *
     * @return KeyStore
     */
    public KeyStore createPrivateKeyStore() {
        if (tlsConfigDirectory == null) {
            return null;
        } else {
            try {
                return readKeyStore(new File(tlsConfigDirectory, KEYSTORE_FILE), readKeyStorePassword(tlsConfigDirectory));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private KeyStore readKeyStore(File keyStoreFile, char[] password) {
        try {
            KeyStore keyStore = KeyStore.getInstance("jks");
            keyStore.load(new FileInputStream(keyStoreFile), password);
            return keyStore;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private char[] readKeyStorePassword(File tlsDirectory) {
        File keyStorePasswordFile = new File(tlsDirectory, KEYSTORE_PASSWORD_FILE);
        try {
            return new String(Files.readAllBytes(keyStorePasswordFile.toPath()), "UTF-8").toCharArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
