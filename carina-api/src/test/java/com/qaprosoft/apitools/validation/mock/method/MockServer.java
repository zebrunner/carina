package com.qaprosoft.apitools.validation.mock.method;

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
