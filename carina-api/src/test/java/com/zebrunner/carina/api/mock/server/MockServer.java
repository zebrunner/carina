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
package com.zebrunner.carina.api.mock.server;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

public class MockServer {

    private WireMockServer wireMockServer;

    public MockServer() {
        WireMockConfiguration config = options().dynamicPort();
        wireMockServer = new WireMockServer(config.portNumber());
    }

    public void start() {
            wireMockServer.start();
    }

    public void stop() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    /**
     * The following code will configure a response with a status of 200 to be returned
     * when the relative URL exactly matches testUrl with response body response
     *
     * @param testUrl String
     *
     * @param response String
     */
    public void createResponse(String testUrl, String response) {
        stubFor(get(urlEqualTo(testUrl))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody(response)));
    }
    
    public String getBaseUrl() {
        return wireMockServer.baseUrl();
    }
    
    public int getPort() {
        return wireMockServer.port();
    }

}
