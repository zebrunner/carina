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

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.conn.ssl.X509HostnameVerifier;

@SuppressWarnings("deprecation")
public class NullHostnameVerifier implements X509HostnameVerifier {
    @Override
    public boolean verify(String hostname, SSLSession session) {
        // do nothing
        return true;
    }

    @Override
    public void verify(String arg0, SSLSocket arg1) throws IOException {
        // do nothing
    }

    @Override
    public void verify(String arg0, X509Certificate arg1) throws SSLException {
        // do nothing
    }

    @Override
    public void verify(String arg0, String[] arg1, String[] arg2) throws SSLException {
        // do nothing
    }
}
