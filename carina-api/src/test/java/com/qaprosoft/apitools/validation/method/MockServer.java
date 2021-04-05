package com.qaprosoft.apitools.validation.method;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.qaprosoft.carina.core.foundation.utils.Configuration;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class MockServer {

    private WireMockServer wireMockServer;

    public MockServer() {
        WireMock.configureFor(Integer.parseInt(Configuration.getEnvArg("mockServer.port")));
        wireMockServer = new WireMockServer(options().port(Integer.parseInt(Configuration.getEnvArg("mockServer.port"))));
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
