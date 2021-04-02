package com.qaprosoft.apitools.validation.method;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class MockServer {

    private WireMockServer wireMockServer;

    public MockServer() {
        wireMockServer = new WireMockServer(wireMockConfig().port(8080));
    }

    public void start() {
            wireMockServer.start();
    }

    public void stop() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    public void createResponse(String testUrl, String response) {
        stubFor(get(urlEqualTo(testUrl))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody(response)));
    }
}
